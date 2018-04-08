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

import org.fourthline.konto.client.JsUtil;
import org.seamless.util.time.DateRange;

import java.io.Serializable;

/**
 * @author Christian Bauer
 */
public class ChartCriteria implements Serializable {

    public enum GroupOption {
        MONTHLY,
        YEARLY
    }

    protected Long accountId;
    protected DateRange range;
    protected GroupOption groupOption;

    public ChartCriteria() {
    }

    public ChartCriteria(Long accountId, DateRange range, GroupOption groupOption) {
        this.accountId = accountId;
        this.range = range;
        this.groupOption = groupOption;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public DateRange getRange() {
        return range;
    }

    public void setRange(DateRange range) {
        this.range = range;
    }

    public GroupOption getGroupOption() {
        return groupOption;
    }

    public void setGroupOption(GroupOption groupOption) {
        this.groupOption = groupOption;
    }

    public static ChartCriteria valueOf(String s) {
        try {
            String[] strings = s.split(";");

            Long accountId = null;
            DateRange dateRange = new DateRange();
            GroupOption groupOption = GroupOption.MONTHLY;

            if (strings.length > 0)
                accountId = strings[0].length() > 0 ? Long.valueOf(strings[0]) : null;
            if (strings.length > 1)
                dateRange = DateRange.valueOf(strings[1] + ";");
            if (strings.length > 2)
                groupOption = GroupOption.valueOf(strings[2]);

            return new ChartCriteria(accountId, dateRange, groupOption);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }

    @Override
    public String toString() {
        return (accountId != null ? String.valueOf(accountId) + ";" : ";")
            + (getRange() != null ? getRange() : "")
            + getGroupOption() + ";";
    }
}
