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
import org.fourthline.konto.shared.query.AccountsQueryCriteria;

/**
 * @author Christian Bauer
 */
public interface ReportView extends IsWidget {

    public interface Presenter {

    }

    void setPresenter(Presenter presenter);

    void setAccountSelection(AccountsQueryCriteria[] selection);

    AccountsQueryCriteria[] getAccountSelection();

    ReportSelectView getReportSelectView();

    ReportResultView getReportResultWidget();

    void promptBookmark(String title);

}
