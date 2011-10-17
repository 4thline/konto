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

import org.fourthline.konto.shared.AccountType;
import org.fourthline.konto.shared.Constants;
import org.seamless.gwt.validation.shared.EntityProperty;
import org.seamless.gwt.validation.shared.ValidationError;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "BANK_ACCOUNT")
public class BankAccount extends AssetAccount {

    public enum Property implements EntityProperty {
        bankName,
        number,
        routing
    }

    @Column(name = "BANK_NAME", nullable = false, length = 255)
    private String bankName = Constants.NULL_MARKER_UNKNOWN;

    @Column(name = "ACCOUNT_NUMBER", nullable = false, length = 255)
    private String number = Constants.NULL_MARKER_UNKNOWN;

    @Column(name = "BANK_ROUTING", nullable = false, length = 255)
    private String routing = Constants.NULL_MARKER_UNKNOWN;

    public BankAccount() {
    }

    public BankAccount(String name, Long monetaryUnitId) {
        super(name, monetaryUnitId);
    }

    public BankAccount(String name, Long monetaryUnitId, Date effectiveOn, BigDecimal initialBalance) {
        super(name, monetaryUnitId, effectiveOn, initialBalance);
    }

    public BankAccount(String bankName, String number, String routing) {
        this.bankName = bankName;
        this.number = number;
        this.routing = routing;
    }

    public BankAccount(String name, Long monetaryUnitId, String bankName, String number, String routing) {
        super(name, monetaryUnitId);
        this.bankName = bankName;
        this.number = number;
        this.routing = routing;
    }

    public BankAccount(String name, Long monetaryUnitId, Date effectiveOn, BigDecimal initialBalance, String bankName, String number, String routing) {
        super(name, monetaryUnitId, effectiveOn, initialBalance);
        this.bankName = bankName;
        this.number = number;
        this.routing = routing;
    }

    @Override
    public AccountType getType() {
        return AccountType.BankAccount;
    }

    public String getBankName() {
        return bankName.equals(Constants.NULL_MARKER_UNKNOWN) ? null : bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName != null && bankName.length() > 0
                ? bankName
                : Constants.NULL_MARKER_UNKNOWN;
    }

    public String getNumber() {
        return number.equals(Constants.NULL_MARKER_UNKNOWN) ? null : number;
    }

    public void setNumber(String number) {
        this.number = number != null && number.length() > 0
                ? number
                : Constants.NULL_MARKER_UNKNOWN;
    }

    public String getRouting() {
        return routing.equals(Constants.NULL_MARKER_UNKNOWN) ? null : routing;
    }

    public void setRouting(String routing) {
        this.routing = routing != null && routing.length() > 0
                ? routing
                : Constants.NULL_MARKER_UNKNOWN;
    }

    @Override
    public List<ValidationError> validate(String group) {
        List<ValidationError> errors = super.validate(group);

        if (getBankName() != null && getBankName().length() > 255) {
            errors.add(new ValidationError(
                    BankAccount.class.getName(),
                    Property.bankName,
                    "Bank name can have a maximum of 255 characters."
            ));
        }

        if (getNumber() != null && getNumber().length() > 255) {
            errors.add(new ValidationError(
                    BankAccount.class.getName(),
                    Property.number,
                    "Account number can have a maximum of 255 characters."
            ));
        }

        if (getRouting() != null && getRouting().length() > 255) {
            errors.add(new ValidationError(
                    BankAccount.class.getName(),
                    Property.routing,
                    "Bank routing information can have a maximum of 255 characters."
            ));
        }

        return errors;
    }
}
