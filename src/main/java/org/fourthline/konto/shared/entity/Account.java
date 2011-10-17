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

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Formula;
import org.fourthline.konto.shared.AccountType;
import org.fourthline.konto.shared.Constants;
import org.seamless.gwt.validation.shared.EntityProperty;
import org.fourthline.konto.shared.MonetaryAmount;
import org.seamless.gwt.validation.shared.Validatable;
import org.seamless.gwt.validation.shared.ValidationError;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ACCOUNT")
@Inheritance(strategy = InheritanceType.JOINED)
@BatchSize(size = 10)
public abstract class Account implements Serializable, Validatable {

    public enum Property implements EntityProperty {
        id,
        name,
        flags,
        monetaryUnitId,
        effectiveOn,
        initialBalance,
        baseCurrencyLastRate,
        baseCurrencyCode,
        groupId,
        groupName
    }

    @Id
    @GeneratedValue(generator = Constants.SEQUENCE_NAME)
    @Column(name = "ACCOUNT_ID")
    private Long id;

    @Column(name = "ACCOUNT_NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "ACCOUNT_FLAGS", nullable = false)
    private int flags;

    @Column(name = "MONETARY_UNIT_ID", nullable = false, updatable = false)
    private Long monetaryUnitId;

    @Temporal(TemporalType.DATE)
    @Column(name = "EFFECTIVE_ON", nullable = false)
    private Date effectiveOn = new Date();

    @Column(name = "INITIAL_BALANCE", nullable = false)
    private BigDecimal initialBalance = new BigDecimal(0);

    @Formula("select agl.ACCOUNT_GROUP_ID from ACCOUNT_GROUP_LINK agl " +
                     "where agl.ACCOUNT_ID = ACCOUNT_ID")
    private Long groupId;

    @Formula("select ag.GROUP_NAME from ACCOUNT_GROUP ag " +
                     "inner join ACCOUNT_GROUP_LINK agl " +
                     "on ag.ACCOUNT_GROUP_ID = agl.ACCOUNT_GROUP_ID " +
                     "and agl.ACCOUNT_ID = ACCOUNT_ID")
    private String groupName;

    @Transient
    private MonetaryUnit monetaryUnit;

    public Account() {
    }

    protected Account(String name, Long monetaryUnitId) {
        this.name = name;
        this.monetaryUnitId = monetaryUnitId;
    }

    protected Account(String name, Long monetaryUnitId, Date effectiveOn, BigDecimal initialBalance) {
        this.name = name;
        this.monetaryUnitId = monetaryUnitId;
        this.effectiveOn = effectiveOn;
        this.initialBalance = initialBalance;
    }

    public abstract AccountType getType();

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public Long getMonetaryUnitId() {
        return monetaryUnitId;
    }

    public void setMonetaryUnitId(Long monetaryUnitId) {
        this.monetaryUnitId = monetaryUnitId;
    }

    public MonetaryUnit getMonetaryUnit() {
        return monetaryUnit;
    }

    public void setMonetaryUnit(MonetaryUnit monetaryUnit) {
        this.monetaryUnit = monetaryUnit;
    }

    public Date getEffectiveOn() {
        return effectiveOn;
    }

    public void setEffectiveOn(Date effectiveOn) {
        if (effectiveOn == null)
            effectiveOn = new Date();
        this.effectiveOn = effectiveOn;
    }

    public MonetaryAmount getInitialBalance() {
        return new MonetaryAmount(getMonetaryUnit(), initialBalance);
    }

    public void setInitialBalance(MonetaryAmount amount) {
        this.initialBalance = amount.getValue();
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getLabel(boolean includeType, boolean rootType, boolean includeGroupName, boolean includeCurrency) {
        StringBuilder sb = new StringBuilder();
        if (includeType)
            sb.append("(").append(rootType ? getType().getRootType().getLabel() : getType().getLabel()).append(") ");
        if (includeGroupName && getGroupName() != null)
            sb.append(getGroupName()).append(": ");
        sb.append(getName());
        if (includeCurrency
                && getMonetaryUnit() != null
                && !getMonetaryUnit().getCurrencyCode().equals(Constants.SYSTEM_BASE_CURRENCY_CODE)) {
            sb.append(" (").append(getMonetaryUnit().getCurrencyCode()).append(")");
        }
        return sb.toString();
    }

    @Override
    public List<ValidationError> validate(String group) {
        List<ValidationError> errors = new ArrayList();

        if (getName() == null || getName().length() == 0) {
            errors.add(new ValidationError(
                    Account.class.getName(),
                    Property.name,
                    "Name is required."
            ));
        } else if (getName().length() > 100) {
            errors.add(new ValidationError(
                    Account.class.getName(),
                    Property.name,
                    "Account name can't be longer than 100 characters."
            ));
        }

        if (getGroupName() != null && getGroupName().length() > 100) {
            errors.add(new ValidationError(
                    Account.class.getName(),
                    Property.groupName,
                    "Group name can't be longer than 100 characters."
            ));
        }

        long currentTime = new Date().getTime();
        if (getEffectiveOn() == null || effectiveOn.getTime() > currentTime) {
            errors.add(new ValidationError(
                    Account.class.getName(),
                    Property.effectiveOn,
                    "Effective date required and must be past date."
            ));
        }

        return errors;
    }

    public String toString() {
        return getId() + ", " + getName() + ", " + getMonetaryUnitId();
    }

}
