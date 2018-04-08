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

package org.fourthline.konto.client.chart.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.fourthline.konto.client.bundle.Bundle;

import javax.inject.Inject;

/**
 * @author Christian Bauer
 */
public class ChartViewImpl extends Composite implements ChartView {

    interface UI extends UiBinder<DockLayoutPanel, ChartViewImpl> {
    }

    interface Style extends CssResource {
    }

    @UiField(provided = true)
    Bundle bundle;

    @UiField
    Style style;

    @UiField
    Label headlineLabel;

    @UiField(provided = true)
    final Widget chartSelectWidget;
    @UiField(provided = true)
    final Widget chartResultWidget;

    final ChartSelectView chartSelectView;
    final ChartResultView chartResultView;

    Presenter presenter;
    Long accountId;

    @Inject
    public ChartViewImpl(Bundle bundle,
                         ChartSelectView chartSelectView,
                         ChartResultView chartResultView) {
        this.bundle = bundle;

        this.chartSelectView = chartSelectView;
        this.chartSelectWidget = chartSelectView.asWidget();
        this.chartResultView = chartResultView;
        this.chartResultWidget = chartResultView.asWidget();

        UI ui = GWT.create(UI.class);
        initWidget(ui.createAndBindUi(this));
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }


    @Override
    public void setAccountSelection(Long accountId) {
        this.accountId = accountId;

    }

    @Override
    public Long getAccountSelection() {
        return accountId;
    }

    @Override
    public ChartSelectView getChartSelectView() {
        return chartSelectView;
    }

    @Override
    public ChartResultView getChartResultView() {
        return chartResultView;
    }

    @Override
    public void setHeadline(String headline) {
        headlineLabel.setText(headline);
    }
}