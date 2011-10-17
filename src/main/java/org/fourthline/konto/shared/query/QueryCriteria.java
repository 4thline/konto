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

import org.seamless.gwt.validation.shared.EntityProperty;

import java.io.Serializable;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class QueryCriteria implements Serializable {

    List<Long> listOfIdentifiers;
    EntityProperty orderBy = null;
    boolean sortAscending = false;
    String stringFilter = null;
    boolean substringQuery = false;
    Integer firstResult = null;
    Integer maxResults = null;

    public QueryCriteria() {
    }

    public QueryCriteria(List<Long> listOfIdentifiers) {
        this.listOfIdentifiers = listOfIdentifiers;
    }

    public QueryCriteria(List<Long> listOfIdentifiers, EntityProperty orderBy, boolean sortAscending, Integer firstResult, Integer maxResults) {
        this.listOfIdentifiers = listOfIdentifiers;
        this.orderBy = orderBy;
        this.sortAscending = sortAscending;
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    public QueryCriteria(String stringFilter, boolean substringQuery) {
        this.stringFilter = stringFilter;
        this.substringQuery = substringQuery;
    }

    public QueryCriteria(EntityProperty orderBy, boolean sortAscending) {
        this.orderBy = orderBy;
        this.sortAscending = sortAscending;
    }

    public QueryCriteria(EntityProperty orderBy, boolean sortAscending, Integer firstResult, Integer maxResults) {
        this.orderBy = orderBy;
        this.sortAscending = sortAscending;
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    public QueryCriteria(EntityProperty orderBy, boolean sortAscending, String stringFilter, boolean substringQuery, Integer firstResult, Integer maxResults) {
        this.orderBy = orderBy;
        this.sortAscending = sortAscending;
        this.stringFilter = stringFilter;
        this.substringQuery = substringQuery;
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    public List<Long> getListOfIdentifiers() {
        return listOfIdentifiers;
    }

    public void setListOfIdentifiers(List<Long> listOfIdentifiers) {
        this.listOfIdentifiers = listOfIdentifiers;
    }

    public boolean isListOfIdentifiersEmpty() {
        return getListOfIdentifiers() == null || getListOfIdentifiers().size() == 0;
    }

    public EntityProperty getOrderBy() {
        return orderBy;
    }

    public QueryCriteria setOrderBy(EntityProperty orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public boolean isSortAscending() {
        return sortAscending;
    }

    public QueryCriteria setSortAscending(boolean sortAscending) {
        this.sortAscending = sortAscending;
        return this;
    }

    public String getStringFilter() {
        return stringFilter;
    }

    public QueryCriteria setStringFilter(String stringFilter) {
        this.stringFilter = stringFilter;
        return this;
    }

    public boolean isSubstringQuery() {
        return substringQuery;
    }

    public QueryCriteria setSubstringQuery(boolean substringQuery) {
        this.substringQuery = substringQuery;
        return this;
    }

    public Integer getFirstResult() {
        return firstResult;
    }

    public QueryCriteria setFirstResult(Integer firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public QueryCriteria setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public boolean isStringFilterEmpty() {
        return stringFilter == null || stringFilter.length() == 0;
    }

    public String getStringFilterWildcards() {
        return isSubstringQuery()
                ? "%" + getStringFilter().toLowerCase() + "%"
                : getStringFilter().toLowerCase() + "%";

    }

    public boolean isStringFiltered() {
        return getStringFilter() != null && getStringFilter().length() > 0;
    }

}
