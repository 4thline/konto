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

import org.fourthline.konto.server.dao.AccountDAO;
import org.fourthline.konto.server.dao.CurrencyDAO;
import org.fourthline.konto.server.dao.EntryDAO;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.Entry;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.fourthline.konto.shared.entity.Split;
import org.seamless.gwt.validation.shared.Validatable;
import org.seamless.gwt.validation.shared.ValidationError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Bauer
 */
public class MoneyDanceImporter {

    final protected CurrencyDAO currencyDAO;
    final protected AccountDAO accountDAO;
    final protected EntryDAO entryDAO;

    public MoneyDanceImporter(CurrencyDAO currencyDAO, AccountDAO accountDAO, EntryDAO entryDAO) {
        this.currencyDAO = currencyDAO;
        this.accountDAO = accountDAO;
        this.entryDAO = entryDAO;
    }

    public CurrencyDAO getCurrencyDAO() {
        return currencyDAO;
    }

    public AccountDAO getAccountDAO() {
        return accountDAO;
    }

    public EntryDAO getEntryDAO() {
        return entryDAO;
    }

    public List<ValidationError> importXML(String xml) throws Exception {

        List<ValidationError> errors = new ArrayList();

        MoneyDanceParser parser = new MoneyDanceParser();
        MoneyDanceData data = parser.parse(xml);

        List<MonetaryUnit> currencies = data.getCurrencies();
        Map<Long, Long> currencyIdMap = storeCurrencies(currencies);

        Map<Long, Long> accountIdMap = storeAccounts(errors, currencyIdMap, data.getAccountTree().createAccounts());

        if (errors.size() > 0) return errors;

        storeEntries(errors, accountIdMap, data.getEntries());

        return errors;
    }

    protected Map<Long, Long> storeCurrencies(List<MonetaryUnit> currencies) {
        Map<Long, Long> idMap = new HashMap();

        for (MonetaryUnit currency : currencies) {

            Long oldId = currency.getId();

            MonetaryUnit existing = getCurrencyDAO().getMonetaryUnit(currency.getCurrencyCode());
            if (existing != null) {
                idMap.put(oldId, existing.getId());
            } else {
                currency.setId(null);
                getCurrencyDAO().persist(currency);
                idMap.put(oldId, currency.getId());
            }
        }

        return idMap;
    }

    protected Map<Long, Long> storeAccounts(List<ValidationError> errors,
                                            Map<Long, Long> currencyIdMap,
                                            List<Account> accounts) {

        Map<Long, Long> idMap = new HashMap();

        for (Account account : accounts) {

            Long newCurrencyId;
            if ((newCurrencyId = currencyIdMap.get(account.getMonetaryUnitId())) == null) {
                errors.add(new ValidationError(
                        account.getId().toString(),
                        Account.class.getName(),
                        Account.Property.monetaryUnitId,
                        "Account references missing currency: " + account.getMonetaryUnitId()
                ));
                continue;
            }
            account.setMonetaryUnitId(newCurrencyId);

            if (errors.addAll(account.validate(Validatable.GROUP_SERVER))) break;

            Long oldId = account.getId();

            account.setId(null);
            getAccountDAO().persist(account);
            idMap.put(oldId, account.getId());

        }

        return idMap;
    }

    protected void storeEntries(List<ValidationError> errors,
                                Map<Long, Long> accountIdMap,
                                List<Entry> entries) {

        for (Entry entry : entries) {

            Long newAccountId;
            if ((newAccountId = accountIdMap.get(entry.getAccountId())) == null) {
                errors.add(new ValidationError(
                        entry.getId().toString(),
                        Entry.class.getName(),
                        Entry.Property.accountId,
                        "Entry references missing account: " + entry
                ));
                continue;
            }
            entry.setAccountId(newAccountId);

            for (Split split : entry.getSplits()) {
                if ((newAccountId = accountIdMap.get(split.getAccountId())) == null) {
                    errors.add(new ValidationError(
                            split.getId().toString(),
                            Split.class.getName(),
                            Split.Property.accountId,
                            "Split references missing account: " + split
                    ));
                    continue;
                }
                split.setId(null);
                split.setAccountId(newAccountId);
            }

            if (errors.addAll(entry.validate(Validatable.GROUP_SERVER))) {
                break;
            }

            entry.setId(null);
            getEntryDAO().persist(entry);

        }
    }
}
