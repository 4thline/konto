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

package org.fourthline.konto.client.report.view;

import com.google.gwt.user.client.ui.IsWidget;
import org.fourthline.konto.client.report.ReportType;
import org.seamless.util.time.DateFormat;
import org.seamless.util.time.DateRange;
import org.fourthline.konto.shared.query.LineReportCriteria;
import org.fourthline.konto.shared.query.LineReportOption;

/**
 * @author Christian Bauer
 */
public interface ReportSelectView extends IsWidget {

    public interface Presenter {
        void onReportTypeSelected(ReportType type);

        void onDateRangeUpdated(DateRange dateRange);

        void onReportOptionsUpdated(LineReportOption reportOptions);

        void onCurrencySelected(String currencyCode);

        void onBookmark();

        void onPrint();
    }

    public enum Option {
        USE_DATE_RANGE,
        ENABLE_ENTRY_DETAILS;

        public boolean in(Option[] haystack) {
            if (haystack == null) return false;
            for (Option o : haystack) {
                if (o.equals(this)) return true;
            }
            return false;
        }

        public static boolean equals(Option[] setA, Option[] setB) {
            if (setA == null || setB == null || setA.length != setB.length) return false;
            for (Option a : setA)
                if (!a.in(setB)) return false;
            return true;
        }
    }

    void setPresenter(Presenter presenter);

    void setDateFormat(DateFormat dateFormat);

    void setCriteria(LineReportCriteria criteria);

    ReportType getReportType();

    DateRange getDateRange(ReportType type);

    void setCurrencyCodes(String[] currencyCodes, String selectedCurrencyCode);

    String getCurrencyCode();

    LineReportOption getReportOptions();
}
