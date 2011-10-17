/*
 * Copyright (C) 2011 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.fourthline.konto.server.importer.moneydance;

import org.seamless.xml.SAXParser;
import org.fourthline.konto.shared.entity.Entry;
import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.fourthline.konto.shared.entity.Split;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Bauer
 */
public class MoneyDanceParser {

    public MoneyDanceData parse(String xml) throws Exception {
        SAXParser parser = new SAXParser();
        MoneyDanceData data = new MoneyDanceData();
        new MoneyDanceDataHandler(data, parser);
        parser.parse(
                new InputSource(
                        new StringReader(xml)
                )
        );
        return data;
    }

    protected static class MoneyDanceDataHandler extends SAXParser.Handler<MoneyDanceData> {

        public MoneyDanceDataHandler(MoneyDanceData instance, SAXParser parser) {
            super(instance, parser);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if ("CURRENCYLIST".equals(localName)) {
                new CurrencyListHandler(getInstance().getCurrencies(), this);
            } else if ("ACCOUNT".equals(localName)) {
                AccountTree tree = new AccountTree();
                getInstance().setAccountTree(tree);
                new AccountHandler(tree.getRoot(), this);
            } else if ("TXNS".equals(localName)) {
                new TransactionListHandler(getInstance().getEntries(), this);
            }
        }
    }

    protected static class CurrencyListHandler extends SAXParser.Handler<List<MonetaryUnit>> {

        public CurrencyListHandler(List<MonetaryUnit> instance, SAXParser.Handler parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if ("CURRENCY".equals(localName)) {
                MonetaryUnit currency = new MonetaryUnit();
                getInstance().add(currency);
                new CurrencyHandler(currency, this);
            }

        }

        @Override
        protected boolean isLastElement(String uri, String localName, String qName) {
            return "CURRENCYLIST".equals(localName);
        }
    }

    protected static class CurrencyHandler extends SAXParser.Handler<MonetaryUnit> {

        public CurrencyHandler(MonetaryUnit instance, SAXParser.Handler parent) {
            super(instance, parent);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            if ("CURRID".equals(localName)) {
                getInstance().setId(Long.valueOf(getCharacters()));
            } else if ("CURRCODE".equals(localName)) {
                getInstance().setCurrencyCode(getCharacters());
            } else if ("PREFIX".equals(localName)) {
                getInstance().setPrefix(getCharacters());
            }
        }

        @Override
        protected boolean isLastElement(String uri, String localName, String qName) {
            return "CURRENCY".equals(localName);
        }
    }

    protected static class AccountListHandler extends SAXParser.Handler<AccountTree.Item> {

        public AccountListHandler(AccountTree.Item instance, SAXParser.Handler parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (localName.equals("ACCOUNT")) {

                AccountTree.Item accountItem = new AccountTree.Item();
                getInstance().addChild(accountItem);

                new AccountHandler(accountItem, this);

            }
        }

        @Override
        protected boolean isLastElement(String uri, String localName, String qName) {
            return localName.equals("SUBACCOUNTS");
        }
    }

    protected static class AccountHandler extends SAXParser.Handler<AccountTree.Item> {

        public AccountHandler(AccountTree.Item instance, SAXParser.Handler parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if ("SUBACCOUNTS".equals(localName)) {
                new AccountListHandler(getInstance(), this);
            } else if ("ACCTPARAMS".equals(localName)) {
                new AccountParamsHandler(getInstance().getParams(), this);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            if ("NAME".equals(localName)) {
                getInstance().setName(getCharacters());
            } else if ("TYPE".equals(localName)) {
                getInstance().setType(getCharacters());
            } else if ("ACCTID".equals(localName)) {
                getInstance().setId(getCharacters());
            } else if ("CURRID".equals(localName)) {
                getInstance().setCurrencyId(getCharacters());
            } else if ("STARTBAL".equals(localName)) {
                getInstance().setBalance(getCharacters());
            }
        }

        @Override
        protected boolean isLastElement(String uri, String localName, String qName) {
            return localName.equals("ACCOUNT");
        }
    }

    protected static class AccountParamsHandler extends SAXParser.Handler<Map<String, String>> {

        public AccountParamsHandler(Map<String, String> instance, SAXParser.Handler parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if ("PARAM".equals(localName)) {
                new AccountParamHandler(getInstance(), this);
            }
        }

        @Override
        protected boolean isLastElement(String uri, String localName, String qName) {
            return localName.equals("ACCTPARAMS");
        }
    }

    protected static class AccountParamHandler extends SAXParser.Handler<Map<String, String>> {

        String[] param = new String[2];

        public AccountParamHandler(Map<String, String> instance, SAXParser.Handler parent) {
            super(instance, parent);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            if ("KEY".equals(localName)) {
                param[0] = getCharacters();
            } else if ("VAL".equals(localName)) {
                param[1] = getCharacters();
            }
        }

        @Override
        protected boolean isLastElement(String uri, String localName, String qName) {
            if ("PARAM".equals(localName)) {
                getInstance().put(param[0], param[1]);
                return true;
            }
            return false;
        }
    }

    protected static class TransactionListHandler extends SAXParser.Handler<List<Entry>> {

        public TransactionListHandler(List<Entry> instance, SAXParser.Handler parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if ("PTXN".equals(localName)) {
                Entry entry = new Entry();
                getInstance().add(entry);
                new ParentTransactionHandler(entry, this);
            }
        }

        @Override
        protected boolean isLastElement(String uri, String localName, String qName) {
            return "TXNS".equals(localName);
        }
    }

    protected static class ParentTransactionHandler extends SAXParser.Handler<Entry> {

        public ParentTransactionHandler(Entry instance, SAXParser.Handler parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if ("SPLITS".equals(localName)) {
                new SplitListHandler(getInstance().getSplits(), this);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd");
            SimpleDateFormat dateTimeFmt = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss:SSS");

            if ("TXNID".equals(localName)) {
                getInstance().setId(Long.valueOf(getCharacters()));
            } else if ("ACCTID".equals(localName)) {
                getInstance().setAccountId(Long.valueOf(getCharacters()));
            } else if ("DESC".equals(localName)) {
                getInstance().setDescription(getCharacters());
            } else if ("DATE".equals(localName)) {
                try {
                    getInstance().setEffectiveOn(dateFmt.parse(getCharacters()));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } else if ("DTENTERED".equals(localName)) {
                try {
                    getInstance().setEnteredOn(dateTimeFmt.parse(getCharacters()));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        @Override
        protected boolean isLastElement(String uri, String localName, String qName) {
            if ("PTXN".equals(localName)) {

                if (getInstance().getDescription() == null ||
                        getInstance().getDescription().length() == 0) {
                    getInstance().setDescription("UNKNOWN");
                }

                for (Split split : getInstance().getSplits()) {
                    split.setEntryId(getInstance().getId());
                    split.setEnteredOn(getInstance().getEnteredOn());
                    if (split.getDescription() == null || split.getDescription().length() == 0) {
                        split.setDescription(getInstance().getDescription());
                    }
                }
                return true;
            }
            return false;
        }
    }

    protected static class SplitListHandler extends SAXParser.Handler<List<Split>> {

        public SplitListHandler(List<Split> instance, SAXParser.Handler parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if ("STXN".equals(localName)) {
                Split split = new Split(new MonetaryUnit("XXX"), new MonetaryUnit("XXX"));
                getInstance().add(split);
                new SplitHandler(split, this);
            }
        }

        @Override
        protected boolean isLastElement(String uri, String localName, String qName) {
            return "SPLITS".equals(localName);
        }
    }

    protected static class SplitHandler extends SAXParser.Handler<Split> {

        public SplitHandler(Split instance, SAXParser.Handler parent) {
            super(instance, parent);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            if ("TXNID".equals(localName)) {
                getInstance().setId(Long.valueOf(getCharacters()));
            } else if ("ACCTID".equals(localName)) {
                getInstance().setAccountId(Long.valueOf(getCharacters()));
            } else if ("DESC".equals(localName)) {
                getInstance().setDescription(getCharacters());
            } else if ("PARENTAMT".equals(localName)) {
                getInstance().setEntryAmount(
                        new MonetaryAmount(
                                new MonetaryUnit("XXX"), // Irrelevant for storing
                                new BigDecimal(getCharacters())
                        )
                );
            } else if ("SPLITAMT".equals(localName)) {
                getInstance().setAmount(
                        new MonetaryAmount(
                                new MonetaryUnit("XXX"), // Irrelevant for storing
                                new BigDecimal(getCharacters())
                        )
                );
            } else if ("RAWRATE".equals(localName)) {
                // TODO: We don't store rates! Why does moneydance store rates?
            }
        }

        @Override
        protected boolean isLastElement(String uri, String localName, String qName) {
            return "STXN".equals(localName);
        }
    }
}
