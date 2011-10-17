package org.fourthline.konto.test.report;

import org.fourthline.konto.server.dao.AccountDAO;
import org.fourthline.konto.server.dao.CurrencyDAO;
import org.fourthline.konto.server.dao.EntryDAO;
import org.fourthline.konto.server.service.DefaultCurrencyProvider;
import org.fourthline.konto.shared.AccountType;
import org.seamless.util.time.DateRange;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.fourthline.konto.shared.query.AccountsQueryCriteria;
import org.fourthline.konto.shared.result.EntryReportLine;
import org.fourthline.konto.shared.result.ExchangedMonetaryAmount;
import org.fourthline.konto.shared.result.ReportLine;
import org.fourthline.konto.shared.result.AccountReportLine;
import org.fourthline.konto.shared.result.ReportLines;
import org.fourthline.konto.test.HibernateTest;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * @author Christian Bauer
 */
public class ReportLinesTest extends HibernateTest {

    @Test
    public void createReportForDay() throws Exception {
        getCurrentSession().beginTransaction();

        AccountDAO accountDAO = new AccountDAO();
        EntryDAO entryDAO = new EntryDAO();
        CurrencyDAO currencyDAO = new CurrencyDAO();

        AccountsQueryCriteria crit =
                new AccountsQueryCriteria(Account.Property.groupName, true, AccountType.Asset);

        List<Account> accounts = accountDAO.getAccounts(crit);
        MonetaryUnit unit = currencyDAO.getMonetaryUnit("USD");

        DateRange range = new DateRange(
                null,
                new SimpleDateFormat("yyyy-MM-dd").parse("2011-03-01")
        );

        List<AccountReportLine> accountLines =
                entryDAO.getAccountReportLines(accounts, range, true);

        ReportLines reportLines = new ReportLines(
                crit,
                unit,
                accountLines,
                new DefaultCurrencyProvider(currencyDAO),
                new SimpleDateFormat("yyyy-MM-dd").parse("2011-01-06")
        );

        assertEquals(reportLines.size(), 3);
        assertEquals(reportLines.getTotal().getString(), "56202.00");

        ReportLine sub;

        assertEquals(reportLines.get(0).getLabel(), ReportLines.DEFAULT_GROUP.getName());
        assertEquals(reportLines.get(0).getAmount().getString(), "50078.00");

        sub = reportLines.get(0).getSubLines().get(0);
        assertEquals(sub.getLabel(), "Cash in Wallet");
        assertEquals(sub.getAmount().getString(), "78.00");

        sub = reportLines.get(0).getSubLines().get(1);
        assertEquals(sub.getLabel(), "My House");
        assertEquals(sub.getAmount().getString(), "50000.00");

        assertEquals(reportLines.get(1).getLabel(), "Bank Accounts");
        assertEquals(reportLines.get(1).getAmount().getString(), "5704.00");

        sub = reportLines.get(1).getSubLines().get(0);
        assertEquals(sub.getLabel(), "My CHF account");
        assertEquals(sub.getAmount().getString(), "4704.00");
        assertEquals(((ExchangedMonetaryAmount) sub.getAmount()).getOriginalAmount().getString(), "4800.00");
        assertEquals(sub.getAmount().getClass(), ExchangedMonetaryAmount.class);

        sub = reportLines.get(1).getSubLines().get(1);
        assertEquals(sub.getLabel(), "My USD account");
        assertEquals(sub.getAmount().getString(), "1000.00");

        assertEquals(reportLines.get(2).getLabel(), "More Bank Accounts");
        assertEquals(reportLines.get(2).getAmount().getString(), "420.00");

        sub = reportLines.get(2).getSubLines().get(0);
        assertEquals(sub.getLabel(), "My other account");
        assertEquals(sub.getAmount().getString(), "420.00");
        assertEquals(((ExchangedMonetaryAmount) sub.getAmount()).getOriginalAmount().getString(), "300.00");
        assertEquals(sub.getAmount().getClass(), ExchangedMonetaryAmount.class);

        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void createReportDateRange() throws Exception {
        getCurrentSession().beginTransaction();

        AccountDAO accountDAO = new AccountDAO();
        EntryDAO entryDAO = new EntryDAO();
        CurrencyDAO currencyDAO = new CurrencyDAO();

        AccountsQueryCriteria crit =
                new AccountsQueryCriteria(Account.Property.groupName, true, AccountType.Expense);

        List<Account> accounts = accountDAO.getAccounts(crit);
        MonetaryUnit unit = currencyDAO.getMonetaryUnit("USD");

        DateRange range = new DateRange(
                new SimpleDateFormat("yyyy-MM-dd").parse("2010-12-01"),
                new SimpleDateFormat("yyyy-MM-dd").parse("2010-12-31")
        );

        List<AccountReportLine> accountLines =
                entryDAO.getAccountReportLines(accounts, range, false);

        ReportLines reportLines = new ReportLines(
                crit,
                unit,
                accountLines,
                new DefaultCurrencyProvider(currencyDAO),
                new SimpleDateFormat("yyyy-MM-dd").parse("2011-01-06")
        );

        assertEquals(reportLines.size(), 2);
        assertEquals(reportLines.getTotal().getString(), "251.50");

        ReportLine sub;

        assertEquals(reportLines.get(0).getLabel(), ReportLines.DEFAULT_GROUP.getName());
        assertEquals(reportLines.get(0).getAmount().getString(), "31.50");

        sub = reportLines.get(0).getSubLines().get(0);
        assertEquals(sub.getLabel(), "Entertainment");
        assertEquals(sub.getAmount().getString(), "31.50");

        sub = reportLines.get(0).getSubLines().get(1);
        assertEquals(sub.getLabel(), "Groceries");
        assertEquals(sub.getAmount().getString(), "0.00");

        sub = reportLines.get(0).getSubLines().get(2);
        assertEquals(sub.getLabel(), "Mortgage Interest");
        assertEquals(sub.getAmount().getString(), "0.00");

        sub = reportLines.get(0).getSubLines().get(3);
        assertEquals(sub.getLabel(), "Phone/Internet");
        assertEquals(sub.getAmount().getString(), "0.00");

        assertEquals(reportLines.get(1).getLabel(), "Automobile");
        assertEquals(reportLines.get(1).getAmount().getString(), "220.00");

        sub = reportLines.get(1).getSubLines().get(0);
        assertEquals(sub.getLabel(), "Gas");
        assertEquals(sub.getAmount().getString(), "150.00");

        sub = reportLines.get(1).getSubLines().get(1);
        assertEquals(sub.getLabel(), "Maintenance");
        assertEquals(sub.getAmount().getString(), "70.00");

        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void createReportDetailed() throws Exception {
        getCurrentSession().beginTransaction();

        AccountDAO accountDAO = new AccountDAO();
        EntryDAO entryDAO = new EntryDAO();
        CurrencyDAO currencyDAO = new CurrencyDAO();

        AccountsQueryCriteria crit =
                new AccountsQueryCriteria(Account.Property.groupName, true, AccountType.Expense);

        List<Account> accounts = accountDAO.getAccounts(crit);
        MonetaryUnit unit = currencyDAO.getMonetaryUnit("USD");

        DateRange range = new DateRange(
                new SimpleDateFormat("yyyy-MM-dd").parse("2010-12-01"),
                new SimpleDateFormat("yyyy-MM-dd").parse("2010-12-31")
        );

        Map<Account, List<EntryReportLine>> entryLines = entryDAO.getEntryReportLines(accounts, range);

        ReportLines reportLines = new ReportLines(
                crit,
                unit,
                entryLines,
                false,
                new DefaultCurrencyProvider(currencyDAO),
                new SimpleDateFormat("yyyy-MM-dd").parse("2011-01-06")
        );

        //reportLines.print(System.out);

        assertEquals(reportLines.size(), 2);
        assertEquals(reportLines.getTotal().getString(), "251.50");

        ReportLine sub;

        assertEquals(reportLines.get(0).getLabel(), ReportLines.DEFAULT_GROUP.getName());
        assertEquals(reportLines.get(0).getAmount().getString(), "31.50");

        sub = reportLines.get(0).getSubLines().get(0);
        assertEquals(sub.getLabel(), "Entertainment");
        assertEquals(sub.getAmount().getString(), "31.50");

        assertEquals(reportLines.get(0).getSubLines().get(0).getSubLines().size(), 1);

        sub = reportLines.get(0).getSubLines().get(0).getSubLines().get(0);
        assertEquals(sub.getLabel(), "Watched a movie");
        assertEquals(sub.getAmount().getString(), "22.50");
        assertEquals(sub.getAmount().getUnit().getCurrencyCode(), "EUR");
        assertEquals(((EntryReportLine) sub).getLedgerCoordinates().toString(), "5/5/8");
        assertEquals(
                ((EntryReportLine) sub).getEffectiveOn().getTime(),
                new SimpleDateFormat("yyyy-MM-dd").parse("2010-12-26").getTime()
        );
        assertEquals(((EntryReportLine) sub).getFromToAccountId(), new Long(1));
        assertEquals(((EntryReportLine) sub).getFromToAccount(), "My USD account");
        assertEquals(((EntryReportLine) sub).getFromToAccountGroup(), "Bank Accounts");

        sub = reportLines.get(0).getSubLines().get(1);
        assertEquals(sub.getLabel(), "Groceries");
        assertEquals(sub.getAmount().getString(), "0.00");

        sub = reportLines.get(0).getSubLines().get(2);
        assertEquals(sub.getLabel(), "Mortgage Interest");
        assertEquals(sub.getAmount().getString(), "0.00");

        sub = reportLines.get(0).getSubLines().get(3);
        assertEquals(sub.getLabel(), "Phone/Internet");
        assertEquals(sub.getAmount().getString(), "0.00");

        assertEquals(reportLines.get(1).getLabel(), "Automobile");
        assertEquals(reportLines.get(1).getAmount().getString(), "220.00");

        sub = reportLines.get(1).getSubLines().get(0);
        assertEquals(sub.getLabel(), "Gas");
        assertEquals(sub.getAmount().getString(), "150.00");

        assertEquals(reportLines.get(1).getSubLines().get(0).getSubLines().size(), 4);

        sub = reportLines.get(1).getSubLines().get(0).getSubLines().get(0);
        assertEquals(sub.getLabel(), "Filling up the car");
        assertEquals(sub.getAmount().getString(), "50.00");
        assertEquals(sub.getAmount().getUnit().getCurrencyCode(), "USD");

        sub = reportLines.get(1).getSubLines().get(1);
        assertEquals(sub.getLabel(), "Maintenance");
        assertEquals(sub.getAmount().getString(), "70.00");

        assertEquals(reportLines.get(1).getSubLines().get(1).getSubLines().size(), 2);

        getCurrentSession().getTransaction().commit();
    }

}
