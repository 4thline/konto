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

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import org.fourthline.konto.client.bundle.Bundle;
import org.fourthline.konto.shared.query.AccountsQueryCriteria;

import javax.inject.Inject;

/**
 * @author Christian Bauer
 */
public class ReportViewImpl extends Composite implements ReportView {

    interface UI extends UiBinder<DockLayoutPanel, ReportViewImpl> {
    }

    private UI ui = GWT.create(UI.class);

    interface Style extends CssResource {
    }

    @UiField(provided = true)
    Bundle bundle;

    @UiField
    Style style;

    @UiField(provided = true)
    final Widget reportSelectWidget;
    @UiField(provided = true)
    final Widget reportResultWidget;

    final ReportSelectView reportSelectView;
    final ReportResultView reportResultView;

    Presenter presenter;
    AccountsQueryCriteria[] selection;

    @Inject
    public ReportViewImpl(Bundle bundle,
                          ReportSelectView reportSelectView,
                          ReportResultView reportResultView) {
        this.bundle = bundle;

        this.reportSelectView = reportSelectView;
        this.reportSelectWidget = reportSelectView.asWidget();
        this.reportResultView = reportResultView;
        this.reportResultWidget = reportResultView.asWidget();

        initWidget(ui.createAndBindUi(this));
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setAccountSelection(AccountsQueryCriteria[] selection) {
        this.selection = selection;

    }

    @Override
    public AccountsQueryCriteria[] getAccountSelection() {
        return selection;
    }

    @Override
    public ReportSelectView getReportSelectView() {
        return reportSelectView;
    }

    @Override
    public ReportResultView getReportResultWidget() {
        return reportResultView;
    }

    @Override
    public native void promptBookmark(String title) /*-{
        var url = $wnd.location.href;

        if ($wnd.sidebar) { // Mozilla Firefox Bookmark
            $wnd.sidebar.addPanel(title, url, "");
        } else if ($wnd.external) { // IE Favorite
            $wnd.external.AddFavorite(url, title);
        } else {
            $wnd.alert("Your browser does not support automatic bookmarking. "
                               + "Press CTRL or COMMAND  and D to bookmark this report.");
        }
    }-*/;
}