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

package org.fourthline.konto.shared.result;

import org.seamless.util.time.DateRange;
import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.Entry;
import org.fourthline.konto.shared.entity.Split;

import java.util.ArrayList;

/**
 * @author Christian Bauer
 */
public class LedgerLines extends ArrayList<LedgerLine> {

    protected Account account;
    protected DateRange effectiveOn;
    protected MonetaryAmount startingBalance;

    public LedgerLines() {
    }

    public LedgerLines(Account account, DateRange effectiveOn, MonetaryAmount startingBalance) {
        this.effectiveOn = effectiveOn;
        this.account = account;
        this.startingBalance = startingBalance;
    }

    public Account getAccount() {
        return account;
    }

    public DateRange getEffectiveOn() {
        return effectiveOn;
    }

    public void setStartingBalance(MonetaryAmount startingBalance) {
        this.startingBalance = startingBalance;
    }

    public MonetaryAmount getStartingBalance() {
        return startingBalance;
    }

    public void addEntry(Entry entry) {

        if (entry.getAccountId().equals(getAccount().getId())) {
            // The entry was made on the account we are viewing

            LedgerLine<Entry> line = new LedgerLine<Entry>(account, entry);
            line.setDate(entry.getEffectiveOn());

            // For each split add its parent entry amount
            for (Split split : entry.getSplits()) {
                LedgerLine.Accessor.updateDebitOrCredit(
                        line, split.getEntryAmount()
                );
            }

            line.setDescription(entry.getDescription());
            if (entry.getSplits().size() == 1) {
                line.setFromToAccount(entry.getSplits().get(0).getAccount());
            }
            add(line);

        } else {
            // The entry was NOT made on the account we are viewing

            for (Split split : entry.getSplits()) {
                LedgerLine<Split> line = new LedgerLine<Split>(account, split);
                line.setDate(entry.getEffectiveOn());

                // For each split add its own amount
                LedgerLine.Accessor.updateDebitOrCredit(
                        line,
                        split.getAmount()
                );

                line.setDescription(split.getDescription());
                line.setFromToAccount(entry.getAccount());
                add(line);
            }
        }
    }

    public void updateBalances(boolean ascending) {
        updateBalances(getStartingBalance(), ascending);
    }

    protected void updateBalances(MonetaryAmount currentBalance, boolean ascending) {
        if (startingBalance == null)
            startingBalance = currentBalance;
        
        if (ascending) {
            for (int i = size() - 1; i >= 0; i--) {
                LedgerLine line = get(i);
                currentBalance = line.updateBalance(currentBalance);
            }
        } else {
            for (LedgerLine line : this) {
                currentBalance = line.updateBalance(currentBalance);
            }
        }
    }

}
