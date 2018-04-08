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

package org.fourthline.konto.server.dao;

import org.fourthline.konto.shared.AccountType;
import org.fourthline.konto.shared.query.ChartCriteria;
import org.fourthline.konto.shared.result.ChartDataPoint;
import org.hibernate.Query;
import org.hibernate.transform.ResultTransformer;
import org.seamless.util.time.DateRange;
import org.fourthline.konto.shared.LedgerCoordinates;
import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.Entry;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.fourthline.konto.shared.entity.Split;
import org.fourthline.konto.shared.query.EntriesQueryCriteria;
import org.fourthline.konto.shared.query.LedgerLinesQueryCriteria;
import org.fourthline.konto.shared.result.EntryReportLine;
import org.fourthline.konto.shared.result.LedgerLines;
import org.fourthline.konto.shared.result.AccountReportLine;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Christian Bauer
 */
public class EntryDAO extends HibernateDAO {

    public Entry getEntry(Long id) {
        return (Entry) getCurrentSession().get(Entry.class, id);
    }

    public Entry populateSplits(final Entry entry) {
        String sb = "select s, sa, eaUnit, saUnit from Entry e, Split s, " +
            "Account ea, MonetaryUnit eaUnit, Account sa, MonetaryUnit saUnit" +
            " where s.entryId = :entryId" +
            " and e.id = s.entryId" +
            " and ea.id = e.accountId and sa.id = s.accountId" +
            " and ea.monetaryUnitId = eaUnit.id and sa.monetaryUnitId = saUnit.id";

        Query q = getCurrentSession().createQuery(sb);
        q.setLong("entryId", entry.getId());

        q.setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] objects, String[] strings) {
                Split split = (Split) objects[0];
                split.setAccount((Account) objects[1]);
                split.setEntryMonetaryUnit((MonetaryUnit) objects[2]);
                split.setMonetaryUnit((MonetaryUnit) objects[3]);
                split.setEntry(entry);
                return split;
            }

            @Override
            public List transformList(List list) {
                return list;
            }
        });

        entry.setSplits(q.list());
        return entry;
    }

    public Split getSplit(Long id) {
        String sb = "select s, sa, eaUnit, saUnit from Entry e, Split s, " +
            "Account ea, MonetaryUnit eaUnit, Account sa, MonetaryUnit saUnit" +
            " where e.id = s.entryId" +
            " and ea.id = e.accountId and sa.id = s.accountId" +
            " and ea.monetaryUnitId = eaUnit.id and sa.monetaryUnitId = saUnit.id" +
            " and s.id = :id";

        Query q = getCurrentSession().createQuery(sb);
        q.setLong("id", id);

        q.setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] objects, String[] strings) {
                Split split = (Split) objects[0];
                split.setAccount((Account) objects[1]);
                split.setEntryMonetaryUnit((MonetaryUnit) objects[2]);
                split.setMonetaryUnit((MonetaryUnit) objects[3]);
                return split;
            }

            @Override
            public List transformList(List list) {
                return list;
            }
        });

        return (Split) q.uniqueResult();
    }

    public LedgerLines getLedgerLines(Account account,
                                      LedgerLinesQueryCriteria criteria) {

        MonetaryAmount startingBalance;

        if (criteria.isStringFiltered()) {
            // Always start at zero balance because not all lines are shown
            startingBalance = new MonetaryAmount(account.getMonetaryUnit());
        } else {
            // We might possibly show all lines...
            if (criteria.isStartingAfterAccountEffectiveOn(account)) {
                // No, we show a subset of all lines restricted by time, so calculate
                // the starting balance up to our starting timepoint (minus one day)
                Date oneDayBefore = criteria.getEffectiveOn().getOneDayBeforeStart();
                List<AccountReportLine> balanceOfDayBefore =
                    getAccountReportLines(Collections.singletonList(account), new DateRange(null, oneDayBefore), true);
                startingBalance = balanceOfDayBefore.get(0).getAmount();
            } else {
                // Yes, we show all lines, so start with the account's initial balance
                startingBalance = account.getInitialBalance();
            }
        }

        return getLedgerLines(account, criteria, startingBalance);
    }

    protected LedgerLines getLedgerLines(Account account,
                                         LedgerLinesQueryCriteria criteria,
                                         MonetaryAmount startingBalance) {

        List<Entry> entries = getEntries(account, criteria);

        LedgerLines lines = new LedgerLines(account, criteria.getEffectiveOn(), startingBalance);
        for (Entry entry : entries) {
            lines.addEntry(entry);
        }

        // TODO: More flexible sorting and balance calculation
        if (criteria.getOrderBy() == null || criteria.getOrderBy().equals(Entry.Property.effectiveOn)) {
            boolean balanceAscending = criteria.getOrderBy() == null || !criteria.isSortAscending();
            lines.updateBalances(balanceAscending);
        }

        return lines;
    }

    public List<Entry> getEntries(final Account account, EntriesQueryCriteria criteria) {

        StringBuilder sb = new StringBuilder();
        sb.append("select e, s, ea, sa, eaUnit, saUnit from Entry e, Split s, ");
        sb.append("Account ea, MonetaryUnit eaUnit, Account sa, MonetaryUnit saUnit");
        sb.append(" where e.id = s.entryId");
        sb.append(" and ea.id = e.accountId and sa.id = s.accountId");
        sb.append(" and ea.monetaryUnitId = eaUnit.id and sa.monetaryUnitId = saUnit.id");
        sb.append(" and (e.accountId = :accountId or s.accountId = :accountId)");

        if (!criteria.isStringFilterEmpty())
            sb.append(" and (lower(e.description) like :desc or lower(s.description) like :desc)");

        if (criteria.getEffectiveOn() != null && criteria.getEffectiveOn().getStart() != null)
            sb.append(" and e.effectiveOn >= :effectiveOnStart");

        if (criteria.getEffectiveOn() != null && criteria.getEffectiveOn().getEnd() != null)
            sb.append(" and e.effectiveOn <= :effectiveOnEnd");

        sb.append(" order by ");
        if (criteria.getOrderBy() == null) {
            sb.append("e.effectiveOn desc");
        } else if (criteria.getOrderBy().equals(Entry.Property.description)) {
            sb.append("e.description ").append(criteria.isSortAscending() ? "asc" : "desc");
            sb.append(", s.description ").append(criteria.isSortAscending() ? "asc" : "desc");
        } else if (criteria.getOrderBy().equals(Entry.Property.enteredOn)) {
            sb.append("e.enteredOn ").append(criteria.isSortAscending() ? "asc" : "desc");
        } else {
            sb.append("e.effectiveOn ").append(criteria.isSortAscending() ? "asc" : "desc");
        }
        sb.append(", e.enteredOn desc, s.enteredOn desc");

        Query q = getCurrentSession().createQuery(sb.toString());

        q.setLong("accountId", account.getId());

        if (!criteria.isStringFilterEmpty())
            q.setString("desc", criteria.getStringFilterWildcards());

        if (criteria.getEffectiveOn() != null && criteria.getEffectiveOn().getStart() != null)
            q.setDate("effectiveOnStart", criteria.getEffectiveOn().getStart());

        if (criteria.getEffectiveOn() != null && criteria.getEffectiveOn().getEnd() != null)
            q.setDate("effectiveOnEnd", criteria.getEffectiveOn().getEnd());

        // TODO: Pagination?

        final Map<Long, Entry> entries = new LinkedHashMap<Long, Entry>();

        q.setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] objects, String[] strings) {
                Entry entry = (Entry) objects[0];
                Split split = (Split) objects[1];
                entry.setAccount((Account) objects[2]);
                split.setAccount((Account) objects[3]);
                split.setEntryMonetaryUnit((MonetaryUnit) objects[4]);
                split.setMonetaryUnit((MonetaryUnit) objects[5]);

                Entry existingEntry = entries.get(entry.getId());
                if (existingEntry != null) {
                    split.setEntry(existingEntry);
                    existingEntry.getSplits().add(split);
                } else {
                    // Clear the splits, this entry might have passed through
                    // this transformer before (querying several times with the same PC)
                    entry.getSplits().clear();
                    entry.getSplits().add(split);
                    split.setEntry(entry);
                    entries.put(entry.getId(), entry);
                }
                return null;
            }

            @Override
            public List transformList(List list) {
                return list;
            }
        });

        q.list();

        return new ArrayList(entries.values());
    }

    protected List<Long> getAccountIds(List<Account> accounts) {
        List<Long> ids = new ArrayList(accounts.size());
        for (Account account : accounts) {
            ids.add(account.getId());
        }
        return ids;
    }

    public List<AccountReportLine> getAccountReportLines(List<Account> accounts, DateRange dateRange, boolean useInitialBalance) {
        List<Long> accountIds = getAccountIds(accounts);

        Query q = getCurrentSession().getNamedQuery("sumOfAccounts");
        q.setParameterList("ids", accountIds);
        // Default to 01.01.1900 -> Today if there is no start or end in the given date range
        q.setDate("rangeStart", dateRange.getStart() != null ? dateRange.getStart() : new Date(0, 0, 1));
        q.setDate("rangeEnd", dateRange.getEnd() != null ? dateRange.getEnd() : new Date());

        List<Object[]> result = q.list();

        List<AccountReportLine> lines = new ArrayList<>(accounts.size());

        for (Account account : accounts) {

            // Start with account's initial balance or zero, then add the query result balance
            MonetaryAmount amount =
                useInitialBalance
                    ? account.getInitialBalance()
                    : new MonetaryAmount(account.getMonetaryUnit());

            for (Object[] r : result) {
                if (account.getId().equals(r[0])) {
                    MonetaryAmount m =
                        new MonetaryAmount(account.getMonetaryUnit(), (BigDecimal) r[1]);
                    amount = amount.add(m);
                    break;
                }
            }
            lines.add(new AccountReportLine(account, amount));
        }
        return lines;
    }

    public Map<Account, List<EntryReportLine>> getEntryReportLines(List<Account> accounts, DateRange dateRange) {
        List<Long> accountIds = getAccountIds(accounts);

        Query q = getCurrentSession().getNamedQuery("cashflowOfAccounts");
        q.setParameterList("ids", accountIds);

        // Default to 01.01.1900 -> Today if there is no start or end in the given date range
        q.setDate("rangeStart", dateRange.getStart() != null ? dateRange.getStart() : new Date(0, 0, 1));
        q.setDate("rangeEnd", dateRange.getEnd() != null ? dateRange.getEnd() : new Date());

        List<Object[]> result = q.list();

        Map<Account, List<EntryReportLine>> accountEntryLines = new LinkedHashMap<>(accounts.size());

        for (Account account : accounts) {

            boolean haveEntries = false;
            for (Object[] r : result) {
                if (account.getId().equals(r[0])) {

                    List<EntryReportLine> lines;
                    if ((lines = accountEntryLines.get(account)) == null) {
                        lines = new ArrayList<>();
                        accountEntryLines.put(account, lines);
                    }

                    Long entryId = (Long) r[1];
                    Long splitId = (Long) r[2];
                    Date effectiveOn = (Date) r[3];
                    String description = (String) r[4];
                    Long fromToAccountId = (Long) r[5];
                    String fromToAccountGroup = (String) r[6];
                    String fromToAccount = (String) r[7];
                    BigDecimal amount = (BigDecimal) r[8];

                    lines.add(
                        new EntryReportLine(
                            description,
                            new MonetaryAmount(account.getMonetaryUnit(), amount),
                            new LedgerCoordinates(account.getId(), entryId, splitId),
                            effectiveOn,
                            fromToAccountId,
                            fromToAccountGroup,
                            fromToAccount
                        )
                    );
                    haveEntries = true;
                }
            }
            if (!haveEntries) {
                accountEntryLines.put(account, new ArrayList<>());
            }
        }
        return accountEntryLines;
    }

    public List<ChartDataPoint> getChartDataPoints(Account account, DateRange dateRange, ChartCriteria.GroupOption groupOption) {

        Query q;
        if (groupOption == ChartCriteria.GroupOption.MONTHLY) {
            q = getCurrentSession().getNamedQuery("sumOfAccountByMonth");
        } else if (groupOption == ChartCriteria.GroupOption.YEARLY) {
            q = getCurrentSession().getNamedQuery("sumOfAccountByYear");
        } else {
            throw new UnsupportedOperationException("Not implemented: " + groupOption);
        }

        q.setParameter("id", account.getId());

        switch (account.getType()) {
            case Asset:
            case Liability:
            case BankAccount:
                // For asset/liability accounts we must get all previous entries to sum the balance
                q.setDate("rangeStart", new Date(0, 0, 1));
                break;
            default:
                // Default to 01.01.1900 -> Today if there is no start or end in the given date range
                q.setDate("rangeStart", dateRange != null && dateRange.getStart() != null ? dateRange.getStart() : new Date(0, 0, 1));
        }
        q.setDate("rangeEnd", dateRange != null && dateRange.getEnd() != null ? dateRange.getEnd() : new Date());

        List<Object[]> result = q.list();

        List<ChartDataPoint> chartDataPoints = new ArrayList<>();

        for (Object[] r : result) {
            Integer year = (Integer) r[1];
            Integer month = (Integer) r[2];
            BigDecimal amount = (BigDecimal) r[3];
            chartDataPoints.add(
                new ChartDataPoint(
                    year,
                    month,
                    new MonetaryAmount(account.getMonetaryUnit(), amount)
                )
            );
        }

        // For asset/liability accounts, sum balance for all datapoints, starting with initial balance
        if (account.getType() == AccountType.Asset
            || account.getType() == AccountType.Liability
            || account.getType() == AccountType.BankAccount) {
            MonetaryAmount balance = account.getInitialBalance();
            for (ChartDataPoint chartDataPoint : chartDataPoints) {
                chartDataPoint.setMonetaryAmount(
                    balance.add(chartDataPoint.getMonetaryAmount())
                );
                balance = chartDataPoint.getMonetaryAmount();
            }
        } else if (account.getType() == AccountType.Income) {
            for (ChartDataPoint chartDataPoint : chartDataPoints) {
                chartDataPoint.setMonetaryAmount(
                    chartDataPoint.getMonetaryAmount().negate()
                );
            }
        }

        // Remove all chart data points which are not in range (query might use a different range)
        chartDataPoints.removeIf(chartDataPoint ->
            dateRange != null
                && dateRange.isValid()
                && !dateRange.isInRange(new Date(chartDataPoint.getYear()-1900, chartDataPoint.getMonth() + 1, 0))
        );

        return chartDataPoints;
    }

    public void persist(Entry entry) {

        getCurrentSession().saveOrUpdate(entry);
        getCurrentSession().flush();

        for (Split split : entry.getSplits()) {
            split.setEntryId(entry.getId());
            persist(split);
        }

        for (Split split : entry.getOrphanedSplits()) {
            delete(split);
        }
    }

    public void delete(Entry entry) {
        getCurrentSession().delete(entry);
    }

    public void persist(Split split) {
        getCurrentSession().saveOrUpdate(split);
    }

    public void delete(Split split) {
        getCurrentSession().delete(split);
        deleteOrphanedEntries();
    }

    public void deleteOrphanedEntries() {
        // Remove entries that now have no more splits
        getCurrentSession().createQuery(
            "delete from Entry e where not e.id in " +
                "(select distinct(s.entryId) from Split s)"
        ).executeUpdate();
    }

}
