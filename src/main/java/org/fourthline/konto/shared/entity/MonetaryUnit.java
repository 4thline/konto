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
import org.seamless.gwt.validation.shared.Validatable;
import org.seamless.gwt.validation.shared.ValidationError;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "MONETARY_UNIT")
public class MonetaryUnit implements Serializable, Validatable {

    public enum Property implements EntityProperty {
        id,
        currencyCode,
        fractionDigits,
        prefix,
    }

    @Id
    @GeneratedValue(generator = Constants.SEQUENCE_NAME)
    @Column(name = "MONETARY_UNIT_ID")
    private Long id;

    @Column(name = "CURRENCY_CODE", nullable = false, updatable = false, unique = true, length = 3)
    private String currencyCode;

    @Column(name = "FRACTION_DIGITS", nullable = false, updatable = false)
    private int fractionDigits = 2;

    @Column(name = "PREFIX", nullable = false, length = 5)
    private String prefix = "";

    public MonetaryUnit() {
    }

    public MonetaryUnit(String currencyCode) {
        this(currencyCode, "");
    }

    public MonetaryUnit(String currencyCode, String prefix) {
        this.currencyCode = currencyCode;
        this.prefix = prefix;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public int getFractionDigits() {
        return fractionDigits;
    }

    public void setFractionDigits(int fractionDigits) {
        this.fractionDigits = fractionDigits;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public RoundingMode getRoundingMode() {
        // TODO: The rounding mode is NOT the same for all currencies, right?
        return RoundingMode.HALF_UP;
    }

    @Override
    public List<ValidationError> validate(String group) {
        List<ValidationError> errors = new ArrayList();

        if (getCurrencyCode() == null || getCurrencyCode().length() != 3) {
            errors.add(new ValidationError(
                    MonetaryUnit.class.getName(),
                    Property.currencyCode,
                    "Currency code must be 3 characters."
            ));
        }

        if (getPrefix().length() > 5) {
            errors.add(new ValidationError(
                    MonetaryUnit.class.getName(),
                    Property.prefix,
                    "Currency reporting prefix must be 5 characters maximum length."
            ));
        }

        if (getFractionDigits() < 0) {
            errors.add(new ValidationError(
                    MonetaryUnit.class.getName(),
                    Property.fractionDigits,
                    "Number of currency fraction digits must be zero or larger."
            ));
        }

        return errors;
    }

    // TODO: Can't use this, including MU in a ReportCriteria will cause weird CNFE in GWT 2.2/2.3 on serialization
    public static MonetaryUnit valueOf(String s) {
        if (!s.contains("mu=")) return null;
        try {
            String mu = s.substring(s.indexOf("mu=") + 3);
            mu = mu.substring(0, mu.indexOf(";"));
            return new MonetaryUnit(mu);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "mu=" + getCurrencyCode() + ";";
    }
}
