package org.fourthline.konto.test.ledger;

import org.fourthline.konto.server.dao.AccountDAO;
import org.fourthline.konto.server.dao.EntryDAO;
import org.seamless.util.time.DateRange;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.Entry;
import org.fourthline.konto.shared.entity.Split;
import org.fourthline.konto.shared.query.LedgerLinesQueryCriteria;
import org.fourthline.konto.shared.result.LedgerLine;
import org.fourthline.konto.test.HibernateTest;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;


/**
 * @author Christian Bauer
 */
public class LedgerLineQueryTest extends HibernateTest {

    @Test
    public void loadLinesAccountOne() {
        getCurrentSession().beginTransaction();

        Account account = new AccountDAO().getAccount(1l);
        EntryDAO entryDAO = new EntryDAO();

        List<LedgerLine> lines = entryDAO.getLedgerLines(account, new LedgerLinesQueryCriteria(null));

        assertEquals(lines.size(), 10);

        Iterator<LedgerLine> it = lines.iterator();

        LedgerLine line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(10));
        assertEquals(line.getLedgerEntry().getClass(), Entry.class);
        assertEquals(line.getDescription(), "Paid mortgage interest");
        assertEquals(line.getFromToAccount().getName(), "Mortgage Interest");
        assertEquals(line.getDebit().getString(), "150.00");
        assertEquals(line.getCredit().getString(), "0.00");
        assertEquals(line.getBalance().getString(), "1000.00");

        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(12));
        assertEquals(line.getLedgerEntry().getClass(), Entry.class);
        assertEquals(line.getDescription(), "Withdrawal at ATM");
        assertEquals(line.getFromToAccount().getName(), "Cash in Wallet");
        assertEquals(line.getDebit().getString(), "100.00");
        assertEquals(line.getCredit().getString(), "0.00");
        assertEquals(line.getBalance().getString(), "1150.00");

        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(11));
        assertEquals(line.getLedgerEntry().getClass(), Entry.class);
        assertEquals(line.getDescription(), "Salary paid by employer");
        assertEquals(line.getFromToAccount().getName(), "Salary");
        assertEquals(line.getDebit().getString(), "0.00");
        assertEquals(line.getCredit().getString(), "700.00");
        assertEquals(line.getBalance().getString(), "1250.00");

        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(8));
        assertEquals(line.getLedgerEntry().getClass(), Entry.class);
        assertEquals(line.getDescription(), "Amortization");
        assertEquals(line.getFromToAccount().getName(), "Mortgage");
        assertEquals(line.getDebit().getString(), "500.00");
        assertEquals(line.getCredit().getString(), "0.00");
        assertEquals(line.getBalance().getString(), "550.00");

        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(10));
        assertEquals(line.getLedgerEntry().getClass(), Split.class);
        assertEquals(((Split) line.getLedgerEntry()).getEntryMonetaryUnit().getCurrencyCode(), "EUR");
        assertEquals(((Split) line.getLedgerEntry()).getMonetaryUnit().getCurrencyCode(), "USD");
        assertEquals(line.getDescription(), "Converted to USD");
        assertEquals(line.getFromToAccount().getName(), "My other account");
        assertEquals(line.getDebit().getString(), "0.00");
        assertEquals(line.getCredit().getString(), "50.00");
        assertEquals(line.getBalance().getString(), "1050.00");

        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(5));
        assertEquals(line.getLedgerEntry().getClass(), Entry.class);
        assertEquals(line.getDescription(), "Mastercard Bill");
        assertEquals(line.getFromToAccount(), null);
        assertEquals(line.getDebit().getString(), "130.00");
        assertEquals(line.getCredit().getString(), "0.00");
        assertEquals(line.getBalance().getString(), "1000.00");

        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(4));
        assertEquals(line.getLedgerEntry().getClass(), Entry.class);
        assertEquals(line.getDescription(), "Salary paid by employer");
        assertEquals(line.getFromToAccount().getName(), "Salary");
        assertEquals(line.getDebit().getString(), "0.00");
        assertEquals(line.getCredit().getString(), "700.00");
        assertEquals(line.getBalance().getString(), "1130.00");

        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(3));
        assertEquals(line.getLedgerEntry().getClass(), Entry.class);
        assertEquals(line.getDescription(), "Transfering money to EUR account");
        assertEquals(line.getFromToAccount().getName(), "My other account");
        assertEquals(line.getDebit().getString(), "500.00");
        assertEquals(line.getCredit().getString(), "0.00");
        assertEquals(line.getBalance().getString(), "430.00");

        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(2));
        assertEquals(line.getLedgerEntry().getClass(), Entry.class);
        assertEquals(line.getDescription(), "Changing the tires");
        assertEquals(line.getFromToAccount().getName(), "Maintenance");
        assertEquals(line.getDebit().getString(), "20.00");
        assertEquals(line.getCredit().getString(), "0.00");
        assertEquals(line.getBalance().getString(), "930.00");

        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(1));
        assertEquals(line.getLedgerEntry().getClass(), Entry.class);
        assertEquals(line.getDescription(), "Filling up the car");
        assertEquals(line.getFromToAccount().getName(), "Gas");
        assertEquals(line.getDebit().getString(), "50.00");
        assertEquals(line.getCredit().getString(), "0.00");
        assertEquals(line.getBalance().getString(), "950.00");

        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void loadLinesAccountTwo() {
        getCurrentSession().beginTransaction();

        Account account = new AccountDAO().getAccount(2l);
        EntryDAO entryDAO = new EntryDAO();

        List<LedgerLine> lines = entryDAO.getLedgerLines(account, new LedgerLinesQueryCriteria(null));

        assertEquals(lines.size(), 3);

        Iterator<LedgerLine> it = lines.iterator();

        LedgerLine line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(7));
        assertEquals(line.getLedgerEntry().getClass(), Entry.class);
        assertEquals(line.getDescription(), "Converted to USD");
        assertEquals(line.getFromToAccount().getName(), "My USD account");
        assertEquals(line.getDebit().getString(), "37.50");
        assertEquals(line.getCredit().getString(), "0.00");
        assertEquals(line.getBalance().getString(), "300.00");

        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(6));
        assertEquals(line.getLedgerEntry().getClass(), Entry.class);
        assertEquals(line.getDescription(), "Paid gas in euros");
        assertEquals(line.getFromToAccount().getName(), "Gas");
        assertEquals(line.getDebit().getString(), "37.50");
        assertEquals(line.getCredit().getString(), "0.00");
        assertEquals(line.getBalance().getString(), "337.50");

        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(3));
        assertEquals(line.getLedgerEntry().getClass(), Split.class);
        assertEquals(line.getDescription(), "Transfering money to EUR account");
        assertEquals(line.getFromToAccount().getName(), "My USD account");
        assertEquals(line.getDebit().getString(), "0.00");
        assertEquals(line.getCredit().getString(), "375.00");
        assertEquals(line.getBalance().getString(), "375.00");

        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void loadLinesAccountThree() {
        getCurrentSession().beginTransaction();

        Account account = new AccountDAO().getAccount(3l);
        EntryDAO entryDAO = new EntryDAO();

        List<LedgerLine> lines = entryDAO.getLedgerLines(account, new LedgerLinesQueryCriteria(null));

        assertEquals(lines.size(), 4);

        Iterator<LedgerLine> it = lines.iterator();

        LedgerLine line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(9));
        assertEquals(((Split) line.getLedgerEntry()).getEntry().getId(), new Long(6));
        assertEquals(line.getDescription(), "Paid gas in euros");
        assertEquals(line.getFromToAccount().getName(), "My other account");
        assertEquals(line.getDebit().getString(), "0.00");
        assertEquals(line.getCredit().getString(), "50.00");
        assertEquals(line.getBalance().getString(), "150.00");

        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(7));
        assertEquals(((Split) line.getLedgerEntry()).getEntry().getId(), new Long(5));
        assertEquals(line.getDescription(), "Refund from the gas station");
        assertEquals(line.getFromToAccount().getName(), "My USD account");
        assertEquals(line.getDebit().getString(), "20.00");
        assertEquals(line.getCredit().getString(), "0.00");
        assertEquals(line.getBalance().getString(), "100.00");

        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(5));
        assertEquals(((Split) line.getLedgerEntry()).getEntry().getId(), new Long(5));
        assertEquals(line.getDescription(), "Stop at the gas station");
        assertEquals(line.getFromToAccount().getName(), "My USD account");
        assertEquals(line.getDebit().getString(), "0.00");
        assertEquals(line.getCredit().getString(), "70.00");
        assertEquals(line.getBalance().getString(), "120.00");

        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(1));
        assertEquals(((Split) line.getLedgerEntry()).getEntry().getId(), new Long(1));
        assertEquals(line.getDescription(), "Filling up the car");
        assertEquals(line.getFromToAccount().getName(), "My USD account");
        assertEquals(line.getDebit().getString(), "0.00");
        assertEquals(line.getCredit().getString(), "50.00");
        assertEquals(line.getBalance().getString(), "50.00");

        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void loadLinesFiltered() {
        getCurrentSession().beginTransaction();

        Account account = new AccountDAO().getAccount(1l);
        EntryDAO entryDAO = new EntryDAO();

        LedgerLinesQueryCriteria criteria = new LedgerLinesQueryCriteria(
                "car", true, null
        );
        List<LedgerLine> lines = entryDAO.getLedgerLines(account, criteria);

        assertEquals(lines.size(), 2);

        Iterator<LedgerLine> it = lines.iterator();

        LedgerLine line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(5));
        assertEquals(line.getLedgerEntry().getClass(), Entry.class);
        assertEquals(line.getDescription(), "Mastercard Bill");
        assertEquals(line.getFromToAccount(), null);
        assertEquals(line.getDebit().getString(), "130.00");
        assertEquals(line.getCredit().getString(), "0.00");
        assertEquals(line.getBalance().getString(), "-180.00");

        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(1));
        assertEquals(line.getLedgerEntry().getClass(), Entry.class);
        assertEquals(line.getDescription(), "Filling up the car");
        assertEquals(line.getFromToAccount().getName(), "Gas");
        assertEquals(line.getDebit().getString(), "50.00");
        assertEquals(line.getCredit().getString(), "0.00");
        assertEquals(line.getBalance().getString(), "-50.00");

        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void loadLinesInDateRange() throws Exception {
        getCurrentSession().beginTransaction();

        Account account = new AccountDAO().getAccount(1l);
        EntryDAO entryDAO = new EntryDAO();

        Date startDate;
        Date endDate;
        LedgerLinesQueryCriteria criteria;
        List<LedgerLine> lines;
        Iterator<LedgerLine> it;
        LedgerLine line;

        endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2010-12-31");
        criteria = new LedgerLinesQueryCriteria();
        criteria.setEffectiveOn(new DateRange(null, endDate));
        lines = entryDAO.getLedgerLines(account, criteria);
        assertEquals(lines.size(), 6);
        it = lines.iterator();
        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(10));
        assertEquals(line.getBalance().getString(), "1050.00");
        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(5));
        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(4));
        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(3));
        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(2));
        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(1));

        startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2010-12-22");
        endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2010-12-25");
        criteria = new LedgerLinesQueryCriteria();
        criteria.setEffectiveOn(new DateRange(startDate, endDate));
        lines = entryDAO.getLedgerLines(account, criteria);
        assertEquals(lines.size(), 3);
        it = lines.iterator();
        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(4));
        assertEquals(line.getBalance().getString(), "1130.00");
        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(3));
        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(2));

        startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2011-01-01");
        criteria = new LedgerLinesQueryCriteria();
        criteria.setEffectiveOn(new DateRange(startDate));
        assertEquals(criteria.getEffectiveOn().getEnd(), null);
        lines = entryDAO.getLedgerLines(account, criteria);
        assertEquals(lines.size(), 4);
        it = lines.iterator();
        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(10));
        assertEquals(line.getBalance().getString(), "1000.00");
        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(12));
        assertEquals(line.getBalance().getString(), "1150.00");
        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(11));
        assertEquals(line.getBalance().getString(), "1250.00");
        line = it.next();
        assertEquals(line.getLedgerEntry().getId(), new Long(8));
        assertEquals(line.getBalance().getString(), "550.00");

        getCurrentSession().getTransaction().commit();
    }

}
