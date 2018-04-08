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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import org.fourthline.konto.client.bundle.Bundle;
import org.fourthline.konto.client.chart.ChartPlace;
import org.fourthline.konto.client.currency.CurrencyPlace;
import org.fourthline.konto.client.ledger.LedgerPlace;
import org.fourthline.konto.client.ledger.account.AccountPlace;
import org.fourthline.konto.shared.LedgerCoordinates;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.query.ChartCriteria;
import org.fourthline.konto.shared.result.LedgerLine;
import org.fourthline.konto.shared.result.LedgerLines;
import org.seamless.gwt.component.client.widget.DateRangeSelect;
import org.seamless.gwt.component.client.widget.GhostedTextBox;
import org.seamless.gwt.component.client.widget.ImageTextButton;
import org.seamless.gwt.component.client.widget.ResizableSplitLayoutPanel;
import org.seamless.gwt.theme.shared.client.ThemeBundle;
import org.seamless.gwt.theme.shared.client.ThemeStyle;
import org.seamless.util.time.DateFormat;
import org.seamless.util.time.DateRange;

import javax.inject.Inject;

/**
 * TODO: Sorting of table http://code.google.com/p/google-web-toolkit/source/detail?r=9493
 *
 * @author Christian Bauer
 */
public class LedgerViewImpl extends Composite implements LedgerView {

    public static final int SMALL_SIZE_PX = 140;
    public static final int LARGE_SIZE_PX = 365;

    interface UI extends UiBinder<DockLayoutPanel, LedgerViewImpl> {
    }

    private UI ui = GWT.create(UI.class);

    interface Style extends CssResource, LedgerLineTable.Style {
        String dateColumn();

        String descriptionColumn();

        String amountColumn();

        String accountColumn();

        String accountCell();

        String infoLabel();

        String rowInFuture();

        String filteredBalance();
    }

    @UiField(provided = true)
    final Bundle bundle;
    @UiField(provided = true)
    final ThemeBundle themeBundle;
    @UiField
    Style style;

    @UiField
    Label accountLabel;
    @UiField
    Anchor accountCurrencyAnchor;
    @UiField
    Label accountInitialBalanceLabel;

    @UiField(provided = true)
    final DateRangeSelect dateRangeSelect;

    @UiField(provided = true)
    GhostedTextBox descriptionFilterTextBox;
    @UiField
    Button descriptionFilterClearButton;

    @UiField
    ImageTextButton showChartButton;

    @UiField
    ImageTextButton addEntryButton;

    @UiField
    ImageTextButton editAccountButton;

    @UiField(provided = true)
    final DockLayoutPanel ledgerEditSplitPanel;

    @UiField
    ScrollPanel ledgerPanel;

    @UiField
    ScrollPanel editPanel;

    final LedgerLineTable ledgerLineTable;

    double ledgerEditSplitPanelSize;
    Presenter presenter;
    Account account;
    Timer filterTimer;

    @Inject
    public LedgerViewImpl(Bundle bundle,
                          DateRangeSelect dateRangeSelect) {
        this.bundle = bundle;
        this.themeBundle = bundle.themeBundle().create();

        this.ledgerEditSplitPanel = new ResizableSplitLayoutPanel();

        this.dateRangeSelect = dateRangeSelect;

        dateRangeSelect.addValueChangeHandler(new ValueChangeHandler<DateRange>() {
            @Override
            public void onValueChange(ValueChangeEvent<DateRange> event) {
                presenter.filterEffectiveOn(event.getValue());
            }
        });

        this.descriptionFilterTextBox =
                new GhostedTextBox(getFilterDescriptionLabel(), ThemeStyle.GhostedTextBox()) {
                    @Override
                    public void onKeyUp(KeyUpEvent event) {
                        super.onKeyUp(event);

                        final String enteredValue = getValue();
                        if (enteredValue.length() > 0) {
                            descriptionFilterClearButton.setEnabled(true);
                            scheduleFilterReqest(new Timer() {
                                public void run() {
                                    if (presenter != null) {
                                        presenter.filterDescription(enteredValue);
                                    }
                                }
                            });
                        } else {
                            descriptionFilterClearButton.setEnabled(false);
                            if (filterTimer != null) filterTimer.cancel();
                            if (presenter != null) {
                                presenter.filterDescription(null);
                            }
                        }
                    }
                };

        initWidget(ui.createAndBindUi(this));

        CellTable.Resources cellTableResource =
                bundle.themeBundle().create().cellTableResources().create();
        ledgerLineTable = new LedgerLineTable(cellTableResource, style) {

            @Override
            protected String getDebitLabel() {
                return account != null ? account.getType().getDebitLabel() : "";
            }

            @Override
            protected String getCreditLabel() {
                return account != null ? account.getType().getCreditLabel() : "";
            }

            @Override
            protected String getCurrentDescriptionFilter() {
                return descriptionFilterTextBox.getValue();
            }

            @Override
            protected void onSelection(LedgerLine line) {
                presenter.selectLine(line);
            }

            @Override
            protected void onSelection(Account account) {
                presenter.goTo(new LedgerPlace(new LedgerCoordinates(account.getId())));
            }
        };
    }

    public ResizableSplitLayoutPanel getLedgerEditSplitPanel() {
        // The UIBinder parser of DockLayoutPanel is buggy and prevents us from
        // having this in XML. We need to instantiate and cast it.
        return (ResizableSplitLayoutPanel) ledgerEditSplitPanel;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
        dateRangeSelect.reset();
        descriptionFilterTextBox.clear();
    }

    @Override
    public void setDateFormat(DateFormat dateFormat) {
        ledgerLineTable.setDateFormat(dateFormat);
        dateRangeSelect.setDateFormat(dateFormat);
    }

    @Override
    public void focus() {
        addEntryButton.setFocus(true);
    }

    @Override
    public void setLedgerLines(LedgerLines lines, Long selectEntryId, Long selectSplitId) {
        this.account = lines.getAccount();

        accountLabel.setText(account.getLabel(true, true, true, false));

        accountCurrencyAnchor.setText("Currency: " + account.getMonetaryUnit().getCurrencyCode());
        accountCurrencyAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                presenter.goTo(new CurrencyPlace(account.getMonetaryUnitId()));

            }
        });

        if (account.getInitialBalance().signum() != 0) {
            accountInitialBalanceLabel.setText(
                    "Initial Balance: " + account.getInitialBalance().getReportString(true, false)
            );
        } else {
            accountInitialBalanceLabel.setText(
                    "Initial Balance: -"
            );
        }

        dateRangeSelect.setValue(lines.getEffectiveOn());

        if (lines.size() == 0) {

            String noLinesString;
            if (!dateRangeSelect.isAllSelected() && descriptionFilterTextBox.getValue().length() > 0) {
                noLinesString = "No entries in date range with description '" + descriptionFilterTextBox.getValue() + "' found.";
            } else if (!dateRangeSelect.isAllSelected()) {
                noLinesString = "No entries in selected date range.";
            } else if (descriptionFilterTextBox.getValue().length() > 0) {
                noLinesString = "No entries with description '" + descriptionFilterTextBox.getValue() + "' found.";
            } else {
                noLinesString = "Please add entries.";
            }

            Label noLines = new Label(noLinesString);
            noLines.addStyleName(style.infoLabel());
            ledgerPanel.setWidget(noLines);
        } else {
            ledgerPanel.setWidget(ledgerLineTable);
            ledgerLineTable.setRowCount(lines.size(), true);
            ledgerLineTable.setRowData(0, lines);
            ledgerLineTable.select(selectEntryId, selectSplitId);
        }
    }

    @Override
    public AcceptsOneWidget getEntryEditContainer() {
        return editPanel;
    }

    @Override
    public void showEntryEditContainer(boolean large, boolean scrollBottom) {

        // We try our best to memorize the size of the edit panel if the user changed it
        // even though we don't have a ResizeHandler API

        double desiredSize = large ? LARGE_SIZE_PX : SMALL_SIZE_PX;
        double currentSize = getLedgerEditSplitPanel().getSplitPosition(editPanel);

        // If the current size is not one of the default sizes, store it for later re-use
        ledgerEditSplitPanelSize =
                (currentSize > 0 && !(currentSize == SMALL_SIZE_PX || currentSize == LARGE_SIZE_PX))
                        ? currentSize
                        : ledgerEditSplitPanelSize;

        // If there is a non-zero stored size, use that
        if (ledgerEditSplitPanelSize > 0) {
            desiredSize = ledgerEditSplitPanelSize;
        }

        getLedgerEditSplitPanel().setSplitPosition(editPanel, desiredSize, false);
        editPanel.setVisible(true);

        if (scrollBottom) {
            editPanel.scrollToBottom();
        }
    }

    @Override
    public void hideEntryEditContainer() {
        double currentSize = getLedgerEditSplitPanel().setSplitPosition(editPanel, 0, false);

        // Store the size only if it's not one of the default sizes
        if (currentSize > 0 && !(currentSize == SMALL_SIZE_PX || currentSize == LARGE_SIZE_PX)) {
            ledgerEditSplitPanelSize = currentSize;
        }

        editPanel.setVisible(false);
        focus();
    }

    @UiHandler("addEntryButton")
    void onClickAddEntryButton(ClickEvent e) {
        if (presenter != null)
            presenter.addLine();
    }

    @UiHandler("showChartButton")
    void onClickShowChartButton(ClickEvent e) {
        if (presenter != null)
            presenter.showChart();
    }

    @UiHandler("descriptionFilterClearButton")
    void onClickDescriptionFilterClear(ClickEvent e) {
        descriptionFilterTextBox.clear();
        descriptionFilterClearButton.setEnabled(false);
        if (filterTimer != null) filterTimer.cancel();
        if (presenter != null) {
            presenter.filterDescription(null);
        }
    }

    @UiHandler("editAccountButton")
    void onClickEditAccountButton(ClickEvent e) {
        if (presenter != null)
            presenter.goTo(new AccountPlace(account.getId()));
    }

    protected String getFilterDescriptionLabel() {
        return "Filter description...";
    }

    protected void scheduleFilterReqest(Timer timer) {
        if (filterTimer != null) {
            filterTimer.cancel();
        }
        filterTimer = timer;
        filterTimer.schedule(1000);
    }


}
