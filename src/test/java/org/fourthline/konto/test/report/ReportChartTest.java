package org.fourthline.konto.test.report;

import org.fourthline.konto.server.dao.AccountDAO;
import org.fourthline.konto.server.dao.EntryDAO;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.query.ChartCriteria;
import org.fourthline.konto.shared.result.ChartDataPoint;
import org.fourthline.konto.test.HibernateTest;
import org.seamless.util.time.DateRange;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author Christian Bauer
 */
public class ReportChartTest extends HibernateTest {

    @Test
    public void createChart() throws Exception {
        getCurrentSession().beginTransaction();

        AccountDAO accountDAO = new AccountDAO();
        EntryDAO entryDAO = new EntryDAO();

        DateRange range = new DateRange(
            new SimpleDateFormat("yyyy-MM-dd").parse("2010-01-01"),
            new SimpleDateFormat("yyyy-MM-dd").parse("2015-12-31")
        );

        Account account = accountDAO.getAccount(1L);
        List<ChartDataPoint> chartDataPoints = entryDAO.getChartDataPoints(account, range, ChartCriteria.GroupOption.MONTHLY);

        assertEquals(chartDataPoints.size(), 3);

        for (ChartDataPoint chartDataPoint : chartDataPoints) {
            System.out.println(chartDataPoint);
        }
        assertEquals(chartDataPoints.get(0).getYear(), (Integer) 2010);
        assertEquals(chartDataPoints.get(0).getMonth(), (Integer) 12);
        assertEquals(chartDataPoints.get(0).getMonetaryAmount().getValue(), new BigDecimal("1050.00"));
        assertEquals(chartDataPoints.get(0).getMonetaryAmount().getUnit().getCurrencyCode(), "USD");
        assertEquals(chartDataPoints.get(1).getYear(), (Integer) 2011);
        assertEquals(chartDataPoints.get(1).getMonth(), (Integer) 1);
        assertEquals(chartDataPoints.get(1).getMonetaryAmount().getValue(), new BigDecimal("1150.00"));
        assertEquals(chartDataPoints.get(1).getMonetaryAmount().getUnit().getCurrencyCode(), "USD");
        assertEquals(chartDataPoints.get(2).getYear(), (Integer) 2011);
        assertEquals(chartDataPoints.get(2).getMonth(), (Integer) 2);
        assertEquals(chartDataPoints.get(2).getMonetaryAmount().getValue(), new BigDecimal("1000.00"));
        assertEquals(chartDataPoints.get(2).getMonetaryAmount().getUnit().getCurrencyCode(), "USD");

        getCurrentSession().getTransaction().commit();
    }

}
