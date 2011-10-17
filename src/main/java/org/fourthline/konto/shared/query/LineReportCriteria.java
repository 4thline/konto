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

import org.fourthline.konto.client.report.LineReportType;
import org.fourthline.konto.shared.Constants;
import org.seamless.util.time.DateRange;
import org.fourthline.konto.shared.entity.Account;

import java.util.Date;

/**
 * @author Christian Bauer
 */
public class LineReportCriteria extends ReportCriteria {

    protected LineReportType type;
    protected DateRange range;
    protected LineReportOption options;

    public LineReportCriteria() {
    }

    public LineReportCriteria(AccountsQueryCriteria[] accountSelection, String currencyCode, Date dayOfExchangeRate,
                              LineReportType type, DateRange range) {
        this(accountSelection, currencyCode, dayOfExchangeRate, type, range, new LineReportOption());
    }

    public LineReportCriteria(AccountsQueryCriteria[] accountSelection, String currencyCode, Date dayOfExchangeRate,
                              LineReportType type, DateRange range, LineReportOption options) {
        super(accountSelection, currencyCode, dayOfExchangeRate);
        this.type = type;
        this.range = range;
        this.options = options;

        setAccountSelection(getAccountSelection());
    }

    @Override
    public void setAccountSelection(AccountsQueryCriteria[] accountSelection) {
        // Default order/sorting for all line reports
        for (AccountsQueryCriteria criteria : accountSelection) {
            criteria.setOrderBy(Account.Property.groupName);
            criteria.setSortAscending(true);
        }
        super.setAccountSelection(accountSelection);

    }

    public LineReportType getType() {
        return type;
    }

    public void setType(LineReportType type) {
        this.type = type;
    }

    public DateRange getRange() {
        return range;
    }

    public void setRange(DateRange range) {
        this.range = range;
    }

    public LineReportOption getOptions() {
        return options;
    }

    public void setOptions(LineReportOption options) {
        this.options = options;
    }

    public static LineReportCriteria valueOf(String s) {
        try {

            LineReportType type = LineReportType.valueOf(s.substring(0, 2));

            String currencyCode = null;
            if (s.contains("cc=")) {
                try {
                    String cc = s.substring(s.indexOf("cc=") + 3);
                    currencyCode = cc.substring(0, cc.indexOf(";"));
                } catch (Exception ex) {
                    // Ignore
                }
            }
            if (currencyCode == null)
                currencyCode = Constants.SYSTEM_BASE_CURRENCY_CODE;

            AccountsQueryCriteria[] accountSelection = AccountsQueryCriteria.valueOf(s);
            if (accountSelection == null)
                accountSelection = type.getDefaultAccountSelection();

            DateRange dateRange = DateRange.valueOf(s);
            if (dateRange == null)
                dateRange = new DateRange();

            LineReportOption options = LineReportOption.valueOf(s);
            if (options == null)
                options = new LineReportOption();

            return new LineReportCriteria(
                    accountSelection,
                    currencyCode,
                    new Date(), // Always use today for exchange rates
                    type,
                    dateRange,
                    options
            );
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getType()).append(";");
        sb.append(AccountsQueryCriteria.toString(getAccountSelection()));
        sb.append("cc=").append(getCurrencyCode()).append(";");
        sb.append(getRange());
        sb.append(getOptions());
        return sb.toString();
    }
}
