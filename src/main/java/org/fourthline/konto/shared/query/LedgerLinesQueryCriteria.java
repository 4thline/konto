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

/**
 * @author Christian Bauer
 */
public class LedgerLinesQueryCriteria extends EntriesQueryCriteria {

    public Long selectEntryId;

    public LedgerLinesQueryCriteria() {
    }

    public LedgerLinesQueryCriteria(Long accountId) {
        super(accountId);
    }

    public LedgerLinesQueryCriteria(Long accountId, DateRange effectiveOn) {
        super(accountId, effectiveOn);
    }

    public LedgerLinesQueryCriteria(String stringFilter, boolean substringQuery, Long accountId) {
        super(stringFilter, substringQuery, accountId);
    }

    public LedgerLinesQueryCriteria(String stringFilter, boolean substringQuery, Long accountId, DateRange effectiveOn) {
        super(stringFilter, substringQuery, accountId, effectiveOn);
    }

    public LedgerLinesQueryCriteria(EntityProperty orderBy, boolean sortAscending, String stringFilter, boolean substringQuery, Integer firstResult, Integer maxResults, Long accountId) {
        super(orderBy, sortAscending, stringFilter, substringQuery, firstResult, maxResults, accountId);
    }

    public LedgerLinesQueryCriteria(EntityProperty orderBy, boolean sortAscending, String stringFilter, boolean substringQuery, Integer firstResult, Integer maxResults, Long accountId, DateRange effectiveOn) {
        super(orderBy, sortAscending, stringFilter, substringQuery, firstResult, maxResults, accountId, effectiveOn);
    }

    public LedgerLinesQueryCriteria(Long accountId, Long selectEntryId) {
        super(accountId);
        this.selectEntryId = selectEntryId;
    }

    public LedgerLinesQueryCriteria(Long accountId, DateRange effectiveOn, Long selectEntryId) {
        super(accountId, effectiveOn);
        this.selectEntryId = selectEntryId;
    }

    public LedgerLinesQueryCriteria(String stringFilter, boolean substringQuery, Long accountId, Long selectEntryId) {
        super(stringFilter, substringQuery, accountId);
        this.selectEntryId = selectEntryId;
    }

    public LedgerLinesQueryCriteria(String stringFilter, boolean substringQuery, Long accountId, DateRange effectiveOn, Long selectEntryId) {
        super(stringFilter, substringQuery, accountId, effectiveOn);
        this.selectEntryId = selectEntryId;
    }

    public LedgerLinesQueryCriteria(EntityProperty orderBy, boolean sortAscending, String stringFilter, boolean substringQuery, Integer firstResult, Integer maxResults, Long accountId, Long selectEntryId) {
        super(orderBy, sortAscending, stringFilter, substringQuery, firstResult, maxResults, accountId);
        this.selectEntryId = selectEntryId;
    }

    public LedgerLinesQueryCriteria(EntityProperty orderBy, boolean sortAscending, String stringFilter, boolean substringQuery, Integer firstResult, Integer maxResults, Long accountId, DateRange effectiveOn, Long selectEntryId) {
        super(orderBy, sortAscending, stringFilter, substringQuery, firstResult, maxResults, accountId, effectiveOn);
        this.selectEntryId = selectEntryId;
    }

    public Long getSelectEntryId() {
        return selectEntryId;
    }

    public void setSelectEntryId(Long selectEntryId) {
        this.selectEntryId = selectEntryId;
    }
}
