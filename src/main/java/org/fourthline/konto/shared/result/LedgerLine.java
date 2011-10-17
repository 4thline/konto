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

import org.fourthline.konto.shared.DebitCreditHolder;
import org.fourthline.konto.shared.LedgerEntry;
import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.MonetaryUnit;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Christian Bauer
 */
public class LedgerLine<LE extends LedgerEntry>
        implements Serializable, DebitCreditHolder {

    Account account;
    LE ledgerEntry;

    Date date;
    String description;
    Account fromToAccount;
    MonetaryUnit unit;
    MonetaryAmount debit;
    MonetaryAmount credit;
    MonetaryAmount balance;

    public LedgerLine() {
    }

    public LedgerLine(Account account) {
        this.account = account;
    }

    public LedgerLine(Account account, LE ledgerEntry) {
        this.account = account;
        this.ledgerEntry = ledgerEntry;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public LE getLedgerEntry() {
        return ledgerEntry;
    }

    public void setLedgerEntry(LE ledgerEntry) {
        this.ledgerEntry = ledgerEntry;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Account getFromToAccount() {
        return fromToAccount;
    }

    public void setFromToAccount(Account fromToAccount) {
        this.fromToAccount = fromToAccount;
    }

    @Override
    public MonetaryUnit getMonetaryUnit() {
        return unit;
    }

    @Override
    public void setMonetaryUnit(MonetaryUnit unit) {
        this.unit = unit;
    }

    @Override
    public MonetaryAmount getDebit() {
        return debit;
    }

    @Override
    public void setDebit(MonetaryAmount debit) {
        this.debit = debit;
    }

    @Override
    public MonetaryAmount getCredit() {
        return credit;
    }

    @Override
    public void setCredit(MonetaryAmount credit) {
        this.credit = credit;
    }

    public MonetaryAmount getBalance() {
        return balance;
    }

    public String getBalanceString() {
        if (getBalance().signum() == 0) {
            return "-";
        }
        switch (getAccount().getType()) {
            case Liability:
            case Income:
                return getBalance().negate().getReportString(false, false);
            default:
                return getBalance().getReportString(false, false);
        }
    }

    public void setBalance(MonetaryAmount balance) {
        this.balance = balance;
    }

    public MonetaryAmount updateBalance(MonetaryAmount currentBalance) {
        currentBalance = currentBalance.subtract(getDebit());
        currentBalance = currentBalance.add(getCredit());
        setBalance(currentBalance);
        return currentBalance;
    }

    public boolean isDateInFuture() {
        return getDate() != null && getDate().getTime() > new Date().getTime();
    }

    @Override
    public String toString() {
        return getLedgerEntry()
                + " => " + getDate()
                + ", " + getDescription()
                + ", " + getFromToAccount()
                + ", " + getDebit()
                + ", " + getCredit()
                + ", " + getBalance();
    }
}