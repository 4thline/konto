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

package org.fourthline.konto.client.ledger.view;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import org.seamless.util.time.DateFormat;
import org.seamless.util.time.DateRange;
import org.fourthline.konto.shared.result.LedgerLine;
import org.fourthline.konto.shared.result.LedgerLines;

/**
 * @author Christian Bauer
 */
public interface LedgerView extends IsWidget {

    public interface Presenter {

        void selectLine(LedgerLine line);

        void addLine();

        void filterEffectiveOn(DateRange dateRange);

        void filterDescription(String filter);

        void goTo(Place place);

        void showChart();
    }

    void setPresenter(Presenter presenter);

    void setDateFormat(DateFormat dateFormat);

    void focus();

    void setLedgerLines(LedgerLines lines, Long selectEntryId, Long selectSplitId);

    AcceptsOneWidget getEntryEditContainer();

    void showEntryEditContainer(boolean large, boolean scrollTop);

    void hideEntryEditContainer();

}
