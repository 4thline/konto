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

import org.fourthline.konto.shared.entity.Entry;
import org.fourthline.konto.shared.entity.MonetaryUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class MoneyDanceData {

    List<MonetaryUnit> currencies = new ArrayList();
    AccountTree accountTree;
    List<Entry> entries = new ArrayList();

    public MoneyDanceData() {
    }

    public List<MonetaryUnit> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<MonetaryUnit> currencies) {
        this.currencies = currencies;
    }

    public AccountTree getAccountTree() {
        return accountTree;
    }

    public void setAccountTree(AccountTree accountTree) {
        this.accountTree = accountTree;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }
}
