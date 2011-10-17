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

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.UIObject;
import org.fourthline.konto.shared.AccountType;
import org.seamless.util.time.DateFormat;
import org.fourthline.konto.shared.query.LineReportCriteria;
import org.fourthline.konto.shared.result.ReportLines;

import java.util.Map;

/**
 * @author Christian Bauer
 */
public interface ReportResultView extends IsWidget {

    public interface Style extends ReportLinesTable.Style {

        String blockTitlePanel();

        String titleLabelSub();

        String linesTable();

        String titleLabel();

        String blockSumLabel();

        String titlePanel();

        String blockLabel();

        String blockPanel();

        String reportPanel();

        String totalSumLabel();

        String totalPanel();
    }

    public interface Presenter {
        void goTo(Place place);
    }

    void setPresenter(Presenter presenter);

    void setDateFormat(DateFormat dateFormat);

    void setRoundFractions(Boolean visible);

    void setReportLines(LineReportCriteria criteria, Map<AccountType, ReportLines> linesByType);

    Style getPrintStyle();

    UIObject getPrintObject();
}
