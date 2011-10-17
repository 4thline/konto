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
import org.fourthline.konto.shared.LedgerEntry;
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
import javax.persistence.Version;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Christian Bauer
 */
@Entity
@Table(name = "ENTRY")
public class Entry implements LedgerEntry, Validatable {

    public enum Property implements EntityProperty {
        id,
        accountId,
        description,
        effectiveOn,
        enteredOn,
        splits
    }

    @Id
    @GeneratedValue(generator = Constants.SEQUENCE_NAME)
    @Column(name = "ENTRY_ID")
    private Long id;

    @Version
    @Column(name = "OBJ_VERSION")
    private Long version;

    @Column(name = "ACCOUNT_ID", nullable = false)
    private Long accountId;

    @Column(name = "DESCRIPTION", length = 255, nullable = false)
    private String description;

    @Temporal(TemporalType.DATE)
    @Column(name = "EFFECTIVE_ON", nullable = false)
    private Date effectiveOn = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ENTERED_ON", nullable = false)
    private Date enteredOn = new Date();

    @Transient
    List<Split> splits = new ArrayList();

    @Transient
    List<Split> orphanedSplits = new ArrayList();

    @Transient
    private Account account;

    public Entry() {
    }

    public Entry(Long id, Long version, Long accountId, String description, Date effectiveOn, Date enteredOn) {
        this.id = id;
        this.version = version;
        this.accountId = accountId;
        this.description = description;
        this.effectiveOn = effectiveOn;
        this.enteredOn = enteredOn;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getEffectiveOn() {
        return effectiveOn;
    }

    public void setEffectiveOn(Date effectiveOn) {
        this.effectiveOn = effectiveOn;
    }

    public Date getEnteredOn() {
        return enteredOn;
    }

    public void setEnteredOn(Date enteredOn) {
        this.enteredOn = enteredOn;
    }

    public List<Split> getSplits() {
        return splits;
    }

    public void setSplits(List<Split> splits) {
        this.splits = splits;
    }

    public List<Split> getOrphanedSplits() {
        return orphanedSplits;
    }

    public void setOrphanedSplits(List<Split> orphanedSplits) {
        this.orphanedSplits = orphanedSplits;
    }

    public boolean isMultipleSplits() {
        return getSplits().size() > 0;
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

        if (getAccountId() == null) {
            errors.add(new ValidationError(
                    getClass().getName(),
                    Property.accountId,
                    "Account for entry is required."
            ));
        }

        if (getSplits().size() == 0) {
            errors.add(new ValidationError(
                    getClass().getName(),
                    Property.splits,
                    "At least one transaction (split) is required."
            ));
        } else {
            // Validate all splits and store their errors assigned with
            // the position in the list, given clients the information which
            // split failed validation.
            if (getSplits().size() > 0) {
                for (int i = 0; i < getSplits().size(); i++) {
                    Split split = getSplits().get(i);

                    List<ValidationError> entryErrors = split.validate(group);
                    if (entryErrors.size() > 0) {
                        for (ValidationError entryError : entryErrors) {
                            errors.add(new ValidationError(Integer.toString(i), entryError));
                        }
                    }
                }
            }
        }

        return errors;
    }

    @Override
    public String toString() {
        return "Entry: " + getId()
                + ", " + getEffectiveOn()
                + ", " + getEnteredOn()
                + ", " + getDescription()
                + ", Splits: " + getSplits().size();
    }
}
