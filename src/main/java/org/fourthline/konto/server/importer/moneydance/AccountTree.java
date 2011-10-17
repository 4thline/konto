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

import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.AssetAccount;
import org.fourthline.konto.shared.entity.BankAccount;
import org.fourthline.konto.shared.entity.ExpenseAccount;
import org.fourthline.konto.shared.entity.IncomeAccount;
import org.fourthline.konto.shared.entity.LiabilityAccount;
import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.entity.MonetaryUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Bauer
 */
public class AccountTree {

    Item root = new Item("ROOT", "ROOT");

    public Item getRoot() {
        return root;
    }

    public void setRoot(Item root) {
        this.root = root;
    }

    protected interface Visitor {
        void visit(Item item);
    }

    static public class Item {

        String type;
        String name;
        String id;
        String currencyId;
        String balance;
        Map<String, String> params = new HashMap();

        Item parent;
        List<Item> children = new ArrayList();

        public Item() {
        }

        public Item(String type, String name) {
            this.type = type;
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCurrencyId() {
            return currencyId;
        }

        public void setCurrencyId(String currencyId) {
            this.currencyId = currencyId;
        }

        public String getBalance() {
            return balance;
        }

        public void setBalance(String balance) {
            this.balance = balance;
        }

        public Map<String, String> getParams() {
            return params;
        }

        public void setParams(Map<String, String> params) {
            this.params = params;
        }

        public Item getParent() {
            return parent;
        }

        public void setParent(Item parent) {
            this.parent = parent;
        }

        public List<Item> getChildren() {
            return children;
        }

        public void setChildren(List<Item> children) {
            this.children = children;
        }

        public void addChild(Item child) {
            getChildren().add(child);
            child.setParent(this);
        }

        public void print(int level) {
            for (int i = 0; i < level; i++) {
                System.out.print(" ");
            }
            System.out.println(toString());
            for (Item item : getChildren()) {
                item.print(level + 1);
            }
        }

        public void visit(Visitor visitor) {
            visitor.visit(this);
            for (Item item : getChildren()) {
                item.visit(visitor);
            }
        }

        @Override
        public String toString() {
            return getType() + ", " + getName() + ", " + getId() + ", PARAMS: " + getParams().size();
        }
    }

    public List<Account> createAccounts() {

        final List<Account> accounts = new ArrayList();

        getRoot().visit(new AccountVisitor() {
            @Override
            public void visit(Item item) {

                Account account = null;

                if ("B".equals(item.getType())) {

                    account = new BankAccount();

                    BankAccount ba = (BankAccount)account;
                    if (item.getParams().get("bank_name") != null)
                        ba.setBankName(item.getParams().get("bank_name"));
                    if (item.getParams().get("bank_account_number") != null)
                        ba.setNumber(item.getParams().get("bank_account_number"));

                } else if ("A".equals(item.getType())) {

                    account = new AssetAccount();

                } else if ("Y".equals(item.getType())) {

                    account = new LiabilityAccount();

                } else if ("I".equals(item.getType())) {

                    account = new IncomeAccount();

                } else if ("E".equals(item.getType())) {

                    account = new ExpenseAccount();

                }

                if (account != null) {

                    String dateString = item.getParams().get("creation_date");
                    Long date = Long.valueOf(dateString != null ? dateString : "0");
                    account.setEffectiveOn(new Date(date));

                    setProperties(account, item);
                    accounts.add(account);
                }
            }
        });

        return accounts;
    }

    abstract class AccountVisitor implements Visitor {

        protected void setProperties(Account account, Item item) {

            account.setId(Long.valueOf(item.getId()));

            List<String> names = new ArrayList();
            Item current = item;
            while (current != null) {
                if (current.getParent() == null) break; // Skip the root of MD
                names.add(current.getName());
                current = current.getParent();
            }
            StringBuilder sb = new StringBuilder();
            Collections.reverse(names);
            for (String name : names) {
                sb.append(name).append(": ");
            }
            if (sb.length() > 2)
                sb.delete(sb.length() - 2, sb.length());
            account.setName(sb.toString());

            account.setMonetaryUnitId(Long.valueOf(item.getCurrencyId()));

            if (item.getBalance() != null && item.getBalance().length() > 0)
                account.setInitialBalance(
                        new MonetaryAmount(
                                new MonetaryUnit("XXX"), // Irrelevant for storing
                                (item.getBalance())
                        )
                );
        }

    }


}
