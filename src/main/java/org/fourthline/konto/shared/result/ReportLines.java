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

import org.fourthline.konto.shared.CurrencyProvider;
import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.AccountGroup;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.fourthline.konto.shared.query.AccountsQueryCriteria;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Bauer
 */
public class ReportLines extends ArrayList<GroupReportLine> {

    public static final AccountGroup DEFAULT_GROUP = new AccountGroup(Long.MAX_VALUE, "DEFAULT GROUP");

    public static boolean isDefaultGroup(AccountGroup group) {
        return group.getId() != null && Long.MAX_VALUE == group.getId();
    }

    // This type we need for map lookups
    protected class GroupIdName {
        Long id;
        String name;

        GroupIdName(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupIdName that = (GroupIdName) o;
            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    protected AccountsQueryCriteria criteria;
    protected MonetaryUnit unit;
    protected MonetaryAmount total;

    public ReportLines() {

    }

    public ReportLines(AccountsQueryCriteria criteria,
                       MonetaryUnit unit,
                       List<AccountReportLine> accountLines,
                       CurrencyProvider currencyProvider, Date dateOfExchangeRate) {

        this.criteria = criteria;
        this.unit = unit;
        this.total = new MonetaryAmount(unit);

        updateExchangedAmounts(accountLines, unit, currencyProvider, dateOfExchangeRate);
        addAll(createGroupLines(accountLines));
    }

    public ReportLines(AccountsQueryCriteria criteria,
                       MonetaryUnit unit,
                       Map<Account, List<EntryReportLine>> accountEntryLines,
                       boolean useInitialBalance,
                       CurrencyProvider currencyProvider, Date dateOfExchangeRate) {

        this.criteria = criteria;
        this.unit = unit;
        this.total = new MonetaryAmount(unit);

        Collection<AccountReportLine> accountLines =
                createAccountLines(accountEntryLines, useInitialBalance);
        updateExchangedAmounts(accountLines, unit, currencyProvider, dateOfExchangeRate);
        addAll(createGroupLines(accountLines));
    }

    public AccountsQueryCriteria getCriteria() {
        return criteria;
    }

    public MonetaryUnit getUnit() {
        return unit;
    }

    public MonetaryAmount getTotal() {
        return total;
    }

    @Override
    public void clear() {
        super.clear();
        total = new MonetaryAmount(getUnit());
    }

    protected Collection<AccountReportLine> createAccountLines(Map<Account, List<EntryReportLine>> accountEntryLines,
                                                               boolean useInitialBalance) {

        // Build and populate account lines
        Map<Long, AccountReportLine> accountLines = new LinkedHashMap();

        for (Map.Entry<Account, List<EntryReportLine>> me : accountEntryLines.entrySet()) {

            Account account = me.getKey();

            AccountReportLine accountLine;
            if ((accountLine = accountLines.get(account.getId())) == null) {
                MonetaryAmount amount =
                        useInitialBalance
                                ? account.getInitialBalance()
                                : new MonetaryAmount(account.getMonetaryUnit());
                accountLine = new AccountReportLine(account, amount);
                accountLines.put(account.getId(), accountLine);
            }

            for (EntryReportLine entryLine : me.getValue()) {
                accountLine.addSubLine(entryLine);
            }
        }
        return accountLines.values();
    }

    protected Collection<GroupReportLine> createGroupLines(Collection<AccountReportLine> accountLines) {
        Map<GroupIdName, GroupReportLine> groupLines = new LinkedHashMap();

        for (AccountReportLine accountLine : accountLines) {

            Account account = accountLine.getAccount();

            GroupIdName group;
            if (account.getGroupId() != null) {
                group = new GroupIdName(account.getGroupId(), account.getGroupName());
            } else {
                group = new GroupIdName(DEFAULT_GROUP.getId(), DEFAULT_GROUP.getName());
            }

            GroupReportLine groupLine;
            if ((groupLine = groupLines.get(group)) == null) {
                AccountGroup ag = new AccountGroup(group.id, group.name);
                MonetaryAmount am = new MonetaryAmount(getUnit());
                groupLine = new GroupReportLine(ag, am);
                groupLines.put(group, groupLine);
            }
            groupLine.addSubLine((accountLine));
        }
        return groupLines.values();
    }

    @Override
    public boolean addAll(Collection<? extends GroupReportLine> groupReportLines) {
        for (GroupReportLine groupLine : groupReportLines) {
            total = total.add(groupLine.getAmount());
        }
        return super.addAll(groupReportLines);
    }

    protected void updateExchangedAmounts(Collection<? extends ReportLine> lines,
                                          MonetaryUnit targetUnit,
                                          CurrencyProvider currencyProvider,
                                          Date dayOfExchangeRate) {
        for (ReportLine line : lines) {
            MonetaryAmount amount = line.getAmount();
            if (amount.requiresCurrencyExchange(targetUnit)) {
                line.setAmount(
                        getExchangedAmount(amount, targetUnit, currencyProvider, dayOfExchangeRate)
                );
            }
        }
    }

    protected MonetaryAmount getExchangedAmount(MonetaryAmount originalAmount,
                                                MonetaryUnit targetUnit,
                                                CurrencyProvider currencyProvider,
                                                Date dayOfExchangeRate) {

        CurrencyPair cp = currencyProvider.getCurrencyPair(
                originalAmount.getUnit(), targetUnit, dayOfExchangeRate
        );

        if (cp == null) {
            cp = new CurrencyPair(originalAmount.getUnit(), targetUnit);
        }

        return new ExchangedMonetaryAmount(originalAmount, cp);
    }

    public void print(PrintStream ps) {
        for (GroupReportLine reportLine : this) {
            ps.println(reportLine);
            for (AccountReportLine accountLine : reportLine.getSubLines()) {
                ps.println(" - " + accountLine);
                for (EntryReportLine entryLine : accountLine.getSubLines()) {
                    ps.println("    * " + entryLine);
                }
            }
        }
    }
}
