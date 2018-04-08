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

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.fourthline.konto.client.bundle.Bundle;
import org.fourthline.konto.shared.result.ChartDataPoints;
import org.seamless.util.time.DateFormat;

import javax.inject.Inject;

/**
 * @author Christian Bauer
 */
public class ChartResultViewImpl extends Composite implements ChartResultView {


    interface UI extends UiBinder<ScrollPanel, ChartResultViewImpl> {
    }

    private static UI ui = GWT.create(UI.class);

    @UiField(provided = true)
    final Bundle bundle;

    @UiField
    Style style;

    @UiField
    SimplePanel chartPanel;

    Presenter presenter;
    DateFormat dateFormat;
    boolean roundFractions;
    Chart chart;

    @Inject
    public ChartResultViewImpl(Bundle bundle) {
        this.bundle = bundle;
        initWidget(ui.createAndBindUi(this));

        Canvas canvas = Canvas.createIfSupported();
        if (canvas == null) {
            chartPanel.add(new Label("Canvas not supported in this browser."));
            return;
        }
        canvas.getCanvasElement().setWidth(16);
        canvas.getCanvasElement().setHeight(9);
        canvas.getElement().getStyle().setOutlineWidth(0, com.google.gwt.dom.client.Style.Unit.EM);
        chartPanel.add(canvas);
        chart = ChartUtil.createLineChart(canvas.getContext2d());
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
        getWidget().setVisible(false);
    }

    @Override
    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public void setRoundFractions(Boolean roundFractions) {
        this.roundFractions = roundFractions != null ? roundFractions : false;
    }

    @Override
    public Style getPrintStyle() {
        return style;
    }

    @Override
    public UIObject getPrintObject() {
        return chartPanel;
    }

    @Override
    public void setDataPoints(ChartDataPoints dataPoints) {

        if (dataPoints != null) {
            ChartUtil.update(chart, ChartUtil.convertLabels(dataPoints), ChartUtil.convertData(dataPoints));
        }

        getWidget().setVisible(true);
    }

}
