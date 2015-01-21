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

package org.fourthline.konto.client.dashboard.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import org.fourthline.konto.client.bundle.Bundle;
import org.fourthline.konto.client.ledger.LedgerPlace;
import org.fourthline.konto.client.report.view.ReportLinesTable;
import org.seamless.util.time.DateFormat;
import org.fourthline.konto.shared.LedgerCoordinates;
import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.result.ReportLines;

import javax.inject.Inject;

/**
 * @author Christian Bauer
 */
public class DashboardViewImpl extends Composite implements DashboardView {

    interface UI extends UiBinder<ScrollPanel, DashboardViewImpl> {
    }

    private UI ui = GWT.create(UI.class);

    interface Style extends CssResource {
    }

    @UiField(provided = true)
    Bundle bundle;
    @UiField
    Style style;

    Presenter presenter;

    @UiField
    SimplePanel assetPanel;
    @UiField
    Label assetSumLabel;

    @UiField
    SimplePanel liabilityPanel;
    @UiField
    Label liabilitySumLabel;

    @UiField
    Label networthLabel;

    final ReportLinesTable assetTable;
    final ReportLinesTable liabilityTable;

    @Inject
    public DashboardViewImpl(Bundle bundle) {
        this.bundle = bundle;
        initWidget(ui.createAndBindUi(this));

        assetTable = new ReportLinesTable() {
            @Override
            protected void onLineClick(LedgerCoordinates ledgerCoordinates) {
                presenter.goTo(new LedgerPlace(ledgerCoordinates));
            }
        };
        liabilityTable = new ReportLinesTable() {
            @Override
            protected void onLineClick(LedgerCoordinates ledgerCoordinates) {
                presenter.goTo(new LedgerPlace(ledgerCoordinates));
            }
        };
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
        getWidget().setVisible(false);
    }


    @Override
    public void setReportLines(ReportLines assetLines, ReportLines liabilityLines, MonetaryAmount networth) {
        getWidget().setVisible(true);
        
        assetTable.setValue(assetLines, false, false, true, false);
        assetPanel.clear();
        assetPanel.setWidget(assetTable);
        assetSumLabel.setText(assetLines.getTotal().getReportString(true, true));

        liabilityTable.setValue(liabilityLines, true, false, true, false);
        liabilityPanel.clear();
        liabilityPanel.setWidget(liabilityTable);
        liabilitySumLabel.setText(liabilityLines.getTotal().negate().getReportString(true, true));

        networthLabel.setText(networth.getReportString(true, true));
    }

    @Override
    public void setDateFormat(DateFormat dateFormat) {
        assetTable.setDateFormat(dateFormat);
        liabilityTable.setDateFormat(dateFormat);
    }
}
