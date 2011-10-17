package org.fourthline.konto.test.currency;

import org.fourthline.konto.client.service.CurrencyService;
import org.fourthline.konto.server.service.CurrencyServiceImpl;
import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.fourthline.konto.test.HibernateTest;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author Christian Bauer
 */
public class CurrencyTest extends HibernateTest {

    @Test
    public void createPairs() {

        List<MonetaryUnit> list = new ArrayList();
        list.add(new MonetaryUnit("USD", ""));
        list.add(new MonetaryUnit("EUR", ""));
        list.add(new MonetaryUnit("CHF", ""));

        List<CurrencyPair> pairs = CurrencyPair.getPairs(list);

        assertEquals(pairs.size(), 6);

        assertEquals(pairs.get(0).getFromCode(), "CHF");
        assertEquals(pairs.get(0).getToCode(), "EUR");
        assertEquals(pairs.get(1).getFromCode(), "CHF");
        assertEquals(pairs.get(1).getToCode(), "USD");
        assertEquals(pairs.get(2).getFromCode(), "EUR");
        assertEquals(pairs.get(2).getToCode(), "CHF");
        assertEquals(pairs.get(3).getFromCode(), "EUR");
        assertEquals(pairs.get(3).getToCode(), "USD");
        assertEquals(pairs.get(4).getFromCode(), "USD");
        assertEquals(pairs.get(4).getToCode(), "CHF");
        assertEquals(pairs.get(5).getFromCode(), "USD");
        assertEquals(pairs.get(5).getToCode(), "EUR");
    }

    @Test
    public void calculateExchange() {
        MonetaryUnit chf = new MonetaryUnit("CHF");
        MonetaryUnit eur = new MonetaryUnit("EUR");

        MonetaryAmount a = new MonetaryAmount(chf, 50);
        MonetaryAmount b = new MonetaryAmount(eur, "37.50");
        CurrencyPair chfeur = new CurrencyPair(chf, eur, a, b);
        assertEquals(chfeur.getExchangeRate(), new BigDecimal("0.750000"));
        a = new MonetaryAmount(chf, -50); // Sign should be ignored!
        chfeur = new CurrencyPair(chf, eur, a, b);
        assertEquals(chfeur.getExchangeRate(), new BigDecimal("0.750000"));
        b = new MonetaryAmount(eur, "-37.50"); // Sign should be ignored!
        chfeur = new CurrencyPair(chf, eur, a, b);
        assertEquals(chfeur.getExchangeRate(), new BigDecimal("0.750000"));

        chfeur = new CurrencyPair(chf, eur, "0.65");
        assertEquals(chfeur.getExchangeRate(), new BigDecimal("0.650000"));

        CurrencyPair eurchf = new CurrencyPair(chf, eur, "1.538462");
        assertEquals(eurchf.getExchangeRate(), new BigDecimal("1.538462"));

        // Note how the rounding effects change the amounts!

        MonetaryAmount originalAmount = new MonetaryAmount(chf, "33.33");
        MonetaryAmount exchangedAmount = chfeur.getExchangedAmount(originalAmount);
        assertEquals(exchangedAmount.getString(), "21.66");

        originalAmount = new MonetaryAmount(eur, "21.66");
        exchangedAmount = eurchf.getExchangedAmount(originalAmount);
        assertEquals(exchangedAmount.getString(), "33.32");
        assertEquals(eurchf.getOriginalAmount(exchangedAmount).getString(), "21.66");

        BigDecimal exchangeRate = CurrencyPair.getExchangeRate(originalAmount, exchangedAmount);
        assertEquals(exchangeRate.toString(), "1.538319");
    }

    @Test
    public void queryExchangeRateForDay() throws Exception {
        getCurrentSession().beginTransaction();

        CurrencyService service = new CurrencyServiceImpl();

        Date forDay;
        CurrencyPair pair;

        forDay = new SimpleDateFormat("yyyy-MM-dd").parse("2011-01-05");

        pair = service.getCurrencyPair(new MonetaryUnit("CHF"), new MonetaryUnit("EUR"), forDay);
        assertEquals(pair.getExchangeRate(), new BigDecimal("0.650000"));
        assertEquals(pair.getFromUnit().getCurrencyCode(), "CHF");
        assertEquals(pair.getToUnit().getCurrencyCode(), "EUR");

        pair = service.getCurrencyPair(new MonetaryUnit("EUR"), new MonetaryUnit("CHF"), forDay);
        assertEquals(pair.getExchangeRate(), new BigDecimal("1.538462"));
        assertEquals(pair.getFromUnit().getCurrencyCode(), "EUR");
        assertEquals(pair.getToUnit().getCurrencyCode(), "CHF");

        forDay = new SimpleDateFormat("yyyy-MM-dd").parse("2011-01-06");
        pair = service.getCurrencyPair(new MonetaryUnit("CHF"), new MonetaryUnit("EUR"), forDay);
        assertEquals(pair.getExchangeRate(), new BigDecimal("0.660000"));
        pair = service.getCurrencyPair(new MonetaryUnit("EUR"), new MonetaryUnit("CHF"), forDay);
        assertEquals(pair.getExchangeRate(), new BigDecimal("1.515152"));

        forDay = new SimpleDateFormat("yyyy-MM-dd").parse("2011-01-07");
        pair = service.getCurrencyPair(new MonetaryUnit("CHF"), new MonetaryUnit("EUR"), forDay);
        assertEquals(pair.getExchangeRate(), new BigDecimal("0.670000"));

        // Don't have rates for these days, get the rate of the day nearest
        forDay = new SimpleDateFormat("yyyy-MM-dd").parse("2011-01-08");
        pair = service.getCurrencyPair(new MonetaryUnit("CHF"), new MonetaryUnit("EUR"), forDay);
        assertEquals(pair.getExchangeRate(), new BigDecimal("0.670000"));

        forDay = new SimpleDateFormat("yyyy-MM-dd").parse("2011-01-09");
        pair = service.getCurrencyPair(new MonetaryUnit("CHF"), new MonetaryUnit("EUR"), forDay);
        assertEquals(pair.getExchangeRate(), new BigDecimal("0.670000"));

        forDay = new SimpleDateFormat("yyyy-MM-dd").parse("2011-01-03");
        pair = service.getCurrencyPair(new MonetaryUnit("CHF"), new MonetaryUnit("EUR"), forDay);
        assertEquals(pair.getExchangeRate(), new BigDecimal("0.650000"));

        getCurrentSession().getTransaction().commit();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void invalidPrecision() throws Exception {
        CurrencyPair pair = new CurrencyPair("USD", "CHF");
        pair.setExchangeRate(new BigDecimal("505570816.000000"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void invalidSignum() throws Exception {
        CurrencyPair pair = new CurrencyPair("USD", "CHF");
        pair.setExchangeRate(new BigDecimal("-1"));
    }

    public void invalidScale() throws Exception {
        CurrencyPair pair = new CurrencyPair("USD", "CHF");
        pair.setExchangeRate(new BigDecimal("1.1234567"));
        assertEquals(pair.getExchangeRate().toString(), "1.123457");
    }

}
