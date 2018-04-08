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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.fourthline.konto.client.bundle.Bundle;
import org.fourthline.konto.client.ledger.LedgerPlace;
import org.fourthline.konto.client.report.ReportType;
import org.fourthline.konto.shared.AccountType;
import org.seamless.util.time.DateFormat;
import org.fourthline.konto.shared.LedgerCoordinates;
import org.fourthline.konto.shared.query.LineReportCriteria;
import org.fourthline.konto.shared.result.ReportLines;

import javax.inject.Inject;
import java.util.Map;

/**
 * @author Christian Bauer
 */
public class ReportResultViewImpl extends Composite implements ReportResultView {


    interface UI extends UiBinder<ScrollPanel, ReportResultViewImpl> {
    }

    private static UI ui = GWT.create(UI.class);

    @UiField(provided = true)
    final Bundle bundle;

    @UiField
    Style style;

    @UiField
    VerticalPanel reportPanel;

    Presenter presenter;
    DateFormat dateFormat;
    boolean roundFractions;

    @Inject
    public ReportResultViewImpl(Bundle bundle) {
        this.bundle = bundle;
        initWidget(ui.createAndBindUi(this));
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
        return reportPanel;
    }

    @Override
    public void setReportLines(LineReportCriteria criteria, Map<AccountType, ReportLines> linesByType) {
        reportPanel.clear();

        ReportType reportType = criteria.getType();

        Label titleLabel = new Label(reportType.getLabel());
        titleLabel.addStyleName(style.titleLabel());
        Label titleLabelSub = new Label(reportType.getLabelSub(dateFormat, criteria));
        titleLabelSub.addStyleName(style.titleLabelSub());
        VerticalPanel titlePanel = new VerticalPanel();
        titlePanel.addStyleName(style.titlePanel());
        titlePanel.add(titleLabel);
        titlePanel.add(titleLabelSub);
        reportPanel.add(titlePanel);

        for (Map.Entry<AccountType, ReportLines> me : linesByType.entrySet()) {

            VerticalPanel blockPanel = new VerticalPanel();
            blockPanel.addStyleName(style.blockPanel());

            HorizontalPanel blockTitlePanel = new HorizontalPanel();
            blockTitlePanel.addStyleName(style.blockTitlePanel());
            Label blockLabel = new Label(me.getKey().getLabel());
            blockLabel.addStyleName(style.blockLabel());
            blockTitlePanel.add(blockLabel);

            Label blockSumLabel = new Label();
            blockSumLabel.addStyleName(style.blockSumLabel());
            blockSumLabel.setText(reportType.getTypeSum(roundFractions, me.getKey(), linesByType));
            blockTitlePanel.add(blockSumLabel);

            blockPanel.add(blockTitlePanel);

            ReportLinesTable linesTable =
                    new ReportLinesTable(new ReportLinesTable.Resources() {
                        @Override
                        public ReportLinesTable.Style style() {
                            return style;
                        }
                    }) {
                        @Override
                        protected void onLineClick(LedgerCoordinates ledgerCoordinates) {
                            presenter.goTo(new LedgerPlace(ledgerCoordinates));
                        }
                    };
            linesTable.setDateFormat(dateFormat);
            linesTable.setRoundFractions(roundFractions);
            linesTable.setValue(me.getValue(), criteria);
            linesTable.addStyleName(style.linesTable());
            blockPanel.add(linesTable);

            reportPanel.add(blockPanel);
        }

        String totalSum = reportType.getTotalSum(roundFractions, linesByType);
        if (totalSum != null) {
            Label totalLabel = new Label(totalSum);
            totalLabel.addStyleName(style.totalSumLabel());
            VerticalPanel totalPanel = new VerticalPanel();
            totalPanel.addStyleName(style.totalPanel());
            totalPanel.add(totalLabel);
            reportPanel.add(totalPanel);
        }

        getWidget().setVisible(true);
    }

}
