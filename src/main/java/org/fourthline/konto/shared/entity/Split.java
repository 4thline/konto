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

import org.hibernate.annotations.Check;
import org.fourthline.konto.shared.Constants;
import org.seamless.gwt.validation.shared.EntityProperty;
import org.fourthline.konto.shared.LedgerEntry;
import org.fourthline.konto.shared.MonetaryAmount;
import org.seamless.gwt.validation.shared.Validatable;
import org.seamless.gwt.validation.shared.ValidationError;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Christian Bauer
 */
@Entity
@Table(name = "SPLIT")
@Check(constraints =
                "(ENTRY_AMOUNT = 0 and AMOUNT = 0) " +
                        "or (ENTRY_AMOUNT > 0 and AMOUNT < 0) " +
                        "or (ENTRY_AMOUNT < 0  and AMOUNT > 0)")
public class Split implements LedgerEntry, Validatable, Comparable<Split> {

    public enum Property implements EntityProperty {
        id,
        accountId,
        description,
        entryAmount,
        amount,
        exchangeRate
    }

    @Id
    @GeneratedValue(generator = Constants.SEQUENCE_NAME)
    @Column(name = "SPLIT_ID")
    private Long id;

    @Version
    @Column(name = "OBJ_VERSION")
    private Long version;

    @Column(name = "ACCOUNT_ID", nullable = false)
    private Long accountId;

    @Column(name = "ENTRY_ID", nullable = false)
    private Long entryId;

    @Column(name = "DESCRIPTION", length = 255, nullable = false)
    private String description;

    @Column(name = "ENTRY_AMOUNT", nullable = false, precision = 15, scale = 3)
    private BigDecimal entryAmount;

    @Column(name = "AMOUNT", nullable = false, precision = 15, scale = 3)
    private BigDecimal amount;

    @Column(name = "ENTERED_ON", nullable = false)
    private Date enteredOn = new Date();

    @Transient
    private Account account;

    @Transient
    private Entry entry;

    @Transient
    private MonetaryUnit entryMonetaryUnit;

    @Transient
    private MonetaryUnit monetaryUnit;

    public Split() {
    }

    public Split(MonetaryUnit entryMonetaryUnit, MonetaryUnit monetaryUnit) {
        this.entryMonetaryUnit = entryMonetaryUnit;
        this.monetaryUnit = monetaryUnit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Long getEntryId() {
        return entryId;
    }

    public void setEntryId(Long entryId) {
        this.entryId = entryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MonetaryAmount getEntryAmount() {
        return new MonetaryAmount(getEntryMonetaryUnit(), entryAmount);
    }

    public void setEntryAmount(MonetaryAmount amount) {
        this.entryAmount = amount.getValue();
    }

    public MonetaryAmount getAmount() {
        return new MonetaryAmount(getMonetaryUnit(), amount);
    }

    public void setAmount(MonetaryAmount amount) {
        this.amount = amount.getValue();
    }

    public Date getEnteredOn() {
        return enteredOn;
    }

    public void setEnteredOn(Date enteredOn) {
        this.enteredOn = enteredOn;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public MonetaryUnit getEntryMonetaryUnit() {
        return entryMonetaryUnit;
    }

    public void setEntryMonetaryUnit(MonetaryUnit entryMonetaryUnit) {
        this.entryMonetaryUnit = entryMonetaryUnit;
    }

    public MonetaryUnit getMonetaryUnit() {
        return monetaryUnit;
    }

    public void setMonetaryUnit(MonetaryUnit monetaryUnit) {
        this.monetaryUnit = monetaryUnit;
    }

    @Override
    public List<ValidationError> validate(String group) {
        List<ValidationError> errors = new ArrayList();

        if (getDescription() == null || getDescription().length() == 0) {
            errors.add(new ValidationError(
                    getClass().getName(),
                    Property.description,
                    "Description is required."
            ));
        }

        boolean haveAmount = false;
        if (getAmount() == null) {
            errors.add(new ValidationError(
                    getClass().getName(),
                    Property.amount,
                    "Amount is required."
            ));
        } else {
            haveAmount = getAmount().signum() != 0;
        }

        boolean haveEntryAmount = false;
        if (getEntryAmount() == null) {
            errors.add(new ValidationError(
                    getClass().getName(),
                    Property.entryAmount,
                    "Entry amount is required."
            ));
        } else {
            haveEntryAmount = getEntryAmount().signum() != 0;
        }

        if (haveAmount && haveEntryAmount) {
            if (!((getAmount().signum() > 0 && getEntryAmount().signum() < 0) ||
                    (getAmount().signum() < 0 && getEntryAmount().signum() > 0))) {
                errors.add(new ValidationError(
                        getClass().getName(),
                        Property.amount,
                        "Credit/debit amounts are not balanced."
                ));
            }
        }

        return errors;
    }

    @Override
    public int compareTo(Split split) {
        // Newest first
        if (getEnteredOn() == null || split.getEnteredOn() == null) return 0;
        return new Long(getEnteredOn().getTime()).compareTo(split.getEnteredOn().getTime());
    }

    @Override
    public String toString() {
        return "Split: " + getId() + ", Entry: " + getEntryId() + ", " + getDescription() + ", " + getAmount() + ", " + getEntryAmount();
    }
}
