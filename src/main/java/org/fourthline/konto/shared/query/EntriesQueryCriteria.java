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

package org.fourthline.konto.shared.query;

import org.seamless.util.time.DateRange;
import org.seamless.gwt.validation.shared.EntityProperty;
import org.fourthline.konto.shared.entity.Account;

/**
 * @author Christian Bauer
 */
public class EntriesQueryCriteria extends QueryCriteria {

    protected Long accountId;
    protected Long entryId;
    protected DateRange effectiveOn;

    public EntriesQueryCriteria() {
    }

    public EntriesQueryCriteria(Long accountId) {
        this.accountId = accountId;
    }

    public EntriesQueryCriteria(Long accountId, DateRange effectiveOn) {
        this.accountId = accountId;
        this.effectiveOn = effectiveOn;
    }

    public EntriesQueryCriteria(String stringFilter, boolean substringQuery, Long accountId) {
        super(stringFilter, substringQuery);
        this.accountId = accountId;
    }

    public EntriesQueryCriteria(String stringFilter, boolean substringQuery, Long accountId, DateRange effectiveOn) {
        super(stringFilter, substringQuery);
        this.accountId = accountId;
        this.effectiveOn = effectiveOn;
    }

    public EntriesQueryCriteria(EntityProperty orderBy, boolean sortAscending, String stringFilter, boolean substringQuery, Integer firstResult, Integer maxResults, Long accountId) {
        super(orderBy, sortAscending, stringFilter, substringQuery, firstResult, maxResults);
        this.accountId = accountId;
    }

    public EntriesQueryCriteria(EntityProperty orderBy, boolean sortAscending, String stringFilter, boolean substringQuery, Integer firstResult, Integer maxResults, Long accountId, DateRange effectiveOn) {
        super(orderBy, sortAscending, stringFilter, substringQuery, firstResult, maxResults);
        this.accountId = accountId;
        this.effectiveOn = effectiveOn;
    }

    public Long getAccountId() {
        return accountId;
    }

    public EntriesQueryCriteria setAccountId(Long accountId) {
        this.accountId = accountId;
        return this;
    }

    public DateRange getEffectiveOn() {
        return effectiveOn;
    }

    public EntriesQueryCriteria setEffectiveOn(DateRange effectiveOn) {
        this.effectiveOn = effectiveOn;
        return this;
    }

    public boolean isStartingAfterAccountEffectiveOn(Account account) {
        return getEffectiveOn() != null && getEffectiveOn().isStartAfter(account.getEffectiveOn());
    }

}
