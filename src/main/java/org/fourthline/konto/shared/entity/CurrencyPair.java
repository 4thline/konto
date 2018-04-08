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

package org.fourthline.konto.shared.entity;

import org.fourthline.konto.shared.Constants;
import org.seamless.gwt.validation.shared.EntityProperty;
import org.fourthline.konto.shared.MonetaryAmount;
import org.seamless.gwt.validation.shared.Validatable;
import org.seamless.gwt.validation.shared.ValidationError;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * http://ec.europa.eu/economy_finance/euro/adoption/conversion/index_en.htm
 * <p/>
 * Conversion rates are used for converting euro to national currency units and vice versa.
 * Inverse rates derived from the conversion rates may not be used. Converting a national
 * currency unit into the euro unit consists in dividing the amount expressed in the national
 * currency unit by the conversion rate of the euro against this national currency unit.
 * <p/>
 * Examples:
 * 12 euro = 12 * 6.55957 = 78.7148 francs = 78.71 francs
 * 52 francs = 52 / 6.55957 = 7.9273 euro = 7.93 euro
 * <p/>
 * Monetary amounts to be converted from one national currency unit into another shall first
 * be converted into a monetary amount expressed in the euro unit, whose amount may be rounded
 * to not less than three decimals and shall then be converted into the other national currency
 * unit. No alternative method (in particular, that which consists in calculating directly
 * the bilateral conversion rates between national currency units using the euro conversion
 * rates of the latter) may be used unless it produces the same results.
 * <p/>
 * Example:
 * 1 euro = 1.95583 mark
 * 54 francs = 54 / 6.55957 = 8.23224 euro
 * 8.232 euro = 8.232 * 1.95583 = 16.1003 marks = 16.10 marks
 * <p/>
 * http://www.banque-france.fr/gb/eurosys/telechar/docs/arrondis.pdf
 * <br/>
 * http://www.banana.ch/accounting/files/multicurrency_eng.pdf
 *
 * @author Christian Bauer
 */
@Entity
@Table(name = "CURRENCY_PAIR",
       uniqueConstraints = @UniqueConstraint(columnNames = {"FROM_CODE", "TO_CODE", "CREATED_ON"})
)
public class CurrencyPair implements Serializable, Validatable {

    public static final int PRECISION = 10;
    public static final int SCALE = 6;

    public static final BigDecimal DEFAULT_EXCHANGE_RATE =
            new BigDecimal(1).setScale(CurrencyPair.SCALE, RoundingMode.HALF_UP);

    public static enum Property implements EntityProperty {
        id,
        fromCode,
        toCode,
        exchangeRate,
        createdOn
    }

    @Id
    @GeneratedValue(generator = Constants.SEQUENCE_NAME)
    @Column(name = "CURRENCY_PAIR_ID")
    private Long id;

    @Column(name = "FROM_CODE", nullable = false)
    private String fromCode;

    @Column(name = "TO_CODE", nullable = false)
    private String toCode;

    @Column(name = "EXCHANGE_RATE",
            nullable = false,
            precision = CurrencyPair.PRECISION,
            scale = CurrencyPair.SCALE
    )
    private BigDecimal exchangeRate = DEFAULT_EXCHANGE_RATE;

    @Temporal(TemporalType.DATE)
    @Column(name = "CREATED_ON", nullable = false)
    private Date createdOn = new Date();

    @Transient
    private MonetaryUnit fromUnit;

    @Transient
    private MonetaryUnit toUnit;

    public CurrencyPair() {
    }

    public CurrencyPair(String fromCode, String toCode) {
        this(fromCode, toCode, DEFAULT_EXCHANGE_RATE);
    }

    public CurrencyPair(MonetaryUnit fromUnit, MonetaryUnit toUnit, MonetaryAmount originalAmount, MonetaryAmount exchangedAmount) {
        this(fromUnit.getCurrencyCode(),
             toUnit.getCurrencyCode(),
             originalAmount != null && exchangedAmount != null
                     ? getExchangeRate(originalAmount, exchangedAmount)
                     : DEFAULT_EXCHANGE_RATE
        );
        this.fromUnit = fromUnit;
        this.toUnit = toUnit;
    }

    public CurrencyPair(MonetaryUnit fromUnit, MonetaryUnit toUnit, String exchangeRate) {
        this(fromUnit.getCurrencyCode(), toUnit.getCurrencyCode(), new BigDecimal(exchangeRate));
        this.fromUnit = fromUnit;
        this.toUnit = toUnit;
    }

    public CurrencyPair(MonetaryUnit fromUnit, MonetaryUnit toUnit) {
        this(fromUnit, toUnit, DEFAULT_EXCHANGE_RATE);
    }

    public CurrencyPair(MonetaryUnit fromUnit, MonetaryUnit toUnit, BigDecimal exchangeRate) {
        this(fromUnit.getCurrencyCode(), toUnit.getCurrencyCode(), exchangeRate);
        this.fromUnit = fromUnit;
        this.toUnit = toUnit;
    }

    public CurrencyPair(String fromCode, String toCode, BigDecimal exchangeRate) {
        this.fromCode = fromCode;
        this.toCode = toCode;
        setExchangeRate(exchangeRate);
    }

    public CurrencyPair(String fromCode, String toCode, BigDecimal exchangeRate, Date createdOn) {
        this.fromCode = fromCode;
        this.toCode = toCode;
        setExchangeRate(exchangeRate);
        this.createdOn = createdOn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFromCode() {
        return fromCode;
    }

    public void setFromCode(String fromCode) {
        this.fromCode = fromCode;
    }

    public String getToCode() {
        return toCode;
    }

    public void setToCode(String toCode) {
        this.toCode = toCode;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public String getExchangeRateString() {
        // Cut off the remaining zeroes of fraction
        StringBuilder sb = new StringBuilder();
        sb.append(getExchangeRate().toString());
        int dot = sb.lastIndexOf(".");
        for (int i = sb.length() -1; i >= dot && (sb.charAt(i) == '0' || sb.charAt(i) == '.'); i--) {
            sb.deleteCharAt(i);
        }
        return sb.toString();
    }

    public boolean isValidExchangeRate(BigDecimal er) {
        // Make sure it fits within our constraints for the database type etc.
        if (er.precision() > PRECISION)
            return false;
        if (er.signum() <= 0)
            return false;

        return true;
    }

    /**
     * @param er The desired rate.
     * @throws IllegalArgumentException if the rate is not valid (negative, zero, invalid precision, etc.)
     */
    public void setExchangeRate(BigDecimal er) throws IllegalArgumentException {
        if (!isValidExchangeRate(er))
            throw new IllegalArgumentException("Invalid exchange rate '" + getFromCode() + "/" + getToCode() + "': " + er);
        this.exchangeRate = er.setScale(CurrencyPair.SCALE, RoundingMode.HALF_UP);
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public MonetaryUnit getFromUnit() {
        return fromUnit;
    }

    public void setFromUnit(MonetaryUnit fromUnit) {
        this.fromUnit = fromUnit;
    }

    public MonetaryUnit getToUnit() {
        return toUnit;
    }

    public void setToUnit(MonetaryUnit toUnit) {
        this.toUnit = toUnit;
    }

    static public BigDecimal getExchangeRate(MonetaryAmount fromAmount, MonetaryAmount toAmount) {
        if (toAmount.abs().signum() == 0) return DEFAULT_EXCHANGE_RATE; // Avoid divide by zero
        return toAmount.getValue().abs().divide(
                fromAmount.getValue().abs(), CurrencyPair.SCALE, RoundingMode.HALF_UP
        );
    }

    public MonetaryAmount getExchangedAmount(MonetaryAmount originalAmount) {
        return new MonetaryAmount(
                toUnit,
                originalAmount.getValue().multiply(getExchangeRate())
        );
    }

    public MonetaryAmount getOriginalAmount(MonetaryAmount exchangedAmount) {
        // A scale of 32 should be enough, it is cut to unit's scale next anyway
        BigDecimal original =
                exchangedAmount.getValue()
                        .divide(getExchangeRate(), 32, RoundingMode.HALF_UP);
        return new MonetaryAmount(getFromUnit(), original);
    }

    public boolean equalsFromToCodes(CurrencyPair that) {
        return (getFromCode().equals(that.getFromCode())
                && getToCode().equals(that.getToCode()));
    }

    public boolean equalsFromToCodes(MonetaryUnit fromUnit, MonetaryUnit toUnit) {
        return equalsFromToCodes(fromUnit.getCurrencyCode(), toUnit.getCurrencyCode());
    }

    public boolean equalsFromToCodes(String fromCode, String toCode) {
        return getFromCode().equals(fromCode) && getToCode().equals(toCode);
    }

    public static List<CurrencyPair> getPairs(List<MonetaryUnit> monetaryUnits) {

        // Always sort the collection so result is guaranteed to be the same
        List<MonetaryUnit> sortedUnits = new ArrayList(monetaryUnits);
        Collections.sort(
                sortedUnits,
                new Comparator<MonetaryUnit>() {
                    @Override
                    public int compare(MonetaryUnit a, MonetaryUnit b) {
                        return a.getCurrencyCode().compareToIgnoreCase(b.getCurrencyCode());
                    }
                });

        List<CurrencyPair> pairs = new ArrayList();
        for (MonetaryUnit unitA : sortedUnits) {
            for (MonetaryUnit unitB : sortedUnits) {
                if (unitA.equals(unitB)) continue;
                pairs.add(new CurrencyPair(unitA.getCurrencyCode(), unitB.getCurrencyCode()));
            }
        }
        return pairs;
    }

    @Override
    public List<ValidationError> validate(String group) {
        return new ArrayList();
    }

    @Override
    public String toString() {
        return getFromCode() + "/" + getToCode() + ", " + getExchangeRate() + ", " + getCreatedOn();
    }
}
