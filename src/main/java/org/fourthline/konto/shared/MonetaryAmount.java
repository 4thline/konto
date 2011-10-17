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

package org.fourthline.konto.shared;

import org.fourthline.konto.shared.entity.MonetaryUnit;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Christian Bauer
 */
public class MonetaryAmount implements Serializable {

    public static final char SEPARATOR_THOUSANDS = '\'';
    public static final char SEPARATOR_FRACTION = '.'; // Well that depends on toString() of BigDecimal

    protected MonetaryUnit unit;
    protected BigDecimal value;

    public MonetaryAmount() {
    }

    public MonetaryAmount(MonetaryAmount other) {
        this(other.getUnit(), other.getValue());
    }

    public MonetaryAmount(MonetaryUnit unit) {
        this(unit, 0);
    }

    public MonetaryAmount(MonetaryUnit unit, BigDecimal value) {
        this(unit, value == null ? "0" : value.toString());
    }

    public MonetaryAmount(MonetaryUnit unit, String value) {
        if (unit == null) throw new NullPointerException("MonetaryUnit is required");
        this.unit = unit;
        this.value = new BigDecimal(value)
                .setScale(unit.getFractionDigits(), unit.getRoundingMode());
    }

    public MonetaryAmount(MonetaryUnit unit, int value) {
        if (unit == null) throw new NullPointerException("MonetaryUnit is required");
        this.unit = unit;
        this.value = new BigDecimal(value)
                .setScale(unit.getFractionDigits(), unit.getRoundingMode());
    }

    public MonetaryUnit getUnit() {
        return unit;
    }

    public BigDecimal getValue() {
        return value;
    }

    public MonetaryAmount abs() {
        return newInstance(getUnit(), getValue().abs());
    }

    public MonetaryAmount subtract(MonetaryAmount amount) {
        return newInstance(getUnit(), getValue().subtract(amount.getValue()));
    }

    public MonetaryAmount add(MonetaryAmount amount) {
        return newInstance(getUnit(), getValue().add(amount.getValue()));
    }

    public MonetaryAmount divide(MonetaryAmount amount) {
        return newInstance(
                getUnit(),
                getValue().divide(
                        amount.getValue(),
                        getUnit().getFractionDigits(),
                        getUnit().getRoundingMode()
                )
        );
    }

    public MonetaryAmount negate() {
        return newInstance(getUnit(), getValue().negate());
    }

    public int signum() {
        return getValue().signum();
    }

    public String getString() {
        return getValue().toString();
    }

    public String getReportString() {
        return getReportString(false, false, false);
    }

    public String getReportString(boolean prefix, boolean suffix) {
        return getReportString(prefix, suffix, false);
    }

    public String getReportString(boolean prefix, boolean suffix, boolean emptyIfZero) {
        return getReportString(prefix, suffix, emptyIfZero, false);
    }

    public String getReportString(boolean prefix, boolean suffix, boolean emptyIfZero, boolean roundFractions) {
        if (emptyIfZero && signum() == 0) return "";

        String text = roundFractions
                ? new BigDecimal(getValue().toString()).setScale(0, RoundingMode.HALF_UP).toString()
                : getString();

        // We have no regex, so it's kinda ugly if you want "thousands" seperators
        StringBuilder sb = new StringBuilder();
        boolean inFraction = text.contains("" + SEPARATOR_FRACTION);
        int thousands = inFraction ? 0 : 1;
        char[] chars = text.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {
            char c = chars[i];
            sb.append(c);

            if (c == SEPARATOR_FRACTION) inFraction = false;
            if (!inFraction) {
                if (thousands == 3) {
                    thousands = 0;
                    sb.append(SEPARATOR_THOUSANDS);
                }
                thousands++;
            }
        }
        if (sb.charAt(sb.length() - 1) == SEPARATOR_THOUSANDS) sb.deleteCharAt(sb.length() - 1);
        if (sb.charAt(sb.length() - 1) == '-' && sb.charAt(sb.length() - 2) == SEPARATOR_THOUSANDS)
            sb.deleteCharAt(sb.length() - 2);

        // GWT doesn't emulate sb.reverse()
        char[] rev = sb.toString().toCharArray();
        StringBuilder revsb = new StringBuilder();
        for (int i = rev.length - 1; i >= 0; i--) {
            revsb.append(rev[i]);
        }

        StringBuilder sb2 = new StringBuilder();
        if (prefix)
            sb2.append(getUnit().getPrefix()).append(" ");
        sb2.append(revsb.toString());
        if (suffix)
            sb2.append(" ").append(getUnit().getCurrencyCode());
        return sb2.toString();
    }

    public boolean requiresCurrencyExchange(MonetaryUnit monetaryUnit) {
        String currencyCode = getUnit().getCurrencyCode();
        return !currencyCode.equals(monetaryUnit.getCurrencyCode());
    }

    protected MonetaryAmount newInstance(MonetaryUnit unit, BigDecimal value) {
        return new MonetaryAmount(unit, value);
    }

    @Override
    public String toString() {
        return getValue() + " " + getUnit();
    }
}
