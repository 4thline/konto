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
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import org.fourthline.konto.client.bundle.Bundle;
import org.fourthline.konto.client.report.ReportType;
import org.fourthline.konto.shared.Constants;
import org.seamless.gwt.component.client.widget.AutocompleteDateTextBox;
import org.seamless.gwt.component.client.widget.DateRangeSelect;
import org.seamless.util.time.DateFormat;
import org.seamless.util.time.DateRange;
import org.fourthline.konto.shared.query.LineReportCriteria;
import org.fourthline.konto.shared.query.LineReportOption;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author Christian Bauer
 */
public class ReportSelectViewImpl extends Composite implements ReportSelectView {

    interface UI extends UiBinder<DockLayoutPanel, ReportSelectViewImpl> {
    }

    interface Style extends CssResource {
    }

    private static UI ui = GWT.create(UI.class);

    @UiField(provided = true)
    final Bundle bundle;

    @UiField
    Style style;

    @UiField
    ListBox typeListBox;
    @UiField
    AutocompleteDateTextBox dateBox;
    @UiField(provided = true)
    final DateRangeSelect dateRangeSelect;
    @UiField
    CheckBox showDetailedCheckBox;
    @UiField
    CheckBox showDetailedAccountsCheckBox;
    @UiField
    CheckBox showExchangeCheckBox;
    @UiField
    CheckBox showZeroBalancesCheckBox;
    @UiField
    ListBox currencyListBox;
    @UiField
    Button printButton;
    @UiField
    Button bookmarkButton;

    Presenter presenter;
    LineReportCriteria criteria;

    @Inject
    public ReportSelectViewImpl(Bundle bundle, DateRangeSelect dateRangeSelect) {
        this.bundle = bundle;

        this.dateRangeSelect = dateRangeSelect;

        initWidget(ui.createAndBindUi(this));

        typeListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                presenter.onReportTypeSelected(ReportType.values()[typeListBox.getSelectedIndex()]);
            }
        });

        dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                DateRange dateRange =
                        new DateRange(null, event.getValue() != null ? event.getValue() : new Date());
                if (presenter != null)
                    presenter.onDateRangeUpdated(dateRange);
            }
        });

        dateRangeSelect.addValueChangeHandler(new ValueChangeHandler<DateRange>() {
            @Override
            public void onValueChange(ValueChangeEvent<DateRange> event) {
                if (presenter != null)
                    presenter.onDateRangeUpdated(event.getValue());
            }
        });

        showDetailedCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                showDetailedAccountsCheckBox.setEnabled(event.getValue());
                if (presenter != null)
                    presenter.onReportOptionsUpdated(getReportOptions());
            }
        });

        showDetailedAccountsCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (presenter != null)
                    presenter.onReportOptionsUpdated(getReportOptions());
            }
        });

        showExchangeCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (presenter != null)
                    presenter.onReportOptionsUpdated(getReportOptions());
            }
        });

        showZeroBalancesCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (presenter != null)
                    presenter.onReportOptionsUpdated(getReportOptions());
            }
        });

        currencyListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                presenter.onCurrencySelected(currencyListBox.getSelectedItemText());
            }
        });

        initCurrencyListBox();
        initTypeListBox();
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
        dateBox.reset();
        dateRangeSelect.reset();
    }

    @Override
    public void setDateFormat(DateFormat dateFormat) {
        dateBox.setDateFormat(dateFormat);
        dateRangeSelect.setDateFormat(dateFormat);
    }

    @Override
    public void setCriteria(LineReportCriteria criteria) {
        this.criteria = criteria;
        applyCriteria();
    }

    @Override
    public ReportType getReportType() {
        return ReportType.values()[typeListBox.getSelectedIndex()];
    }

    @Override
    public DateRange getDateRange(ReportType type) {
        if (ReportSelectView.Option.USE_DATE_RANGE.in(type.getReportSelectOptions())) {
            return dateRangeSelect.getValue() != null ? dateRangeSelect.getValue() : new DateRange();
        } else {
            return new DateRange(null, dateBox.getValue() != null ? dateBox.getValue() : new Date());
        }
    }

    @Override
    public LineReportOption getReportOptions() {
        return new LineReportOption(
                showDetailedCheckBox.getValue(),
                showDetailedAccountsCheckBox.getValue(),
                showExchangeCheckBox.getValue(),
                showZeroBalancesCheckBox.getValue()
        );
    }

    @Override
    public void setCurrencyCodes(String[] currencyCodes, String selectedCurrencyCode) {
            currencyListBox.clear();
            if (currencyCodes != null)
                for (int i = 0; i < currencyCodes.length; i++) {
                    String currencyCode = currencyCodes[i];
                    currencyListBox.addItem(currencyCode);
                    if (currencyCode.equals(selectedCurrencyCode))
                        currencyListBox.setSelectedIndex(i);
                }
    }

    @Override
    public String getCurrencyCode() {
        return currencyListBox.getSelectedItemText();
    }

    @UiHandler("printButton")
    void onClickPrint(ClickEvent e) {
        if (presenter != null) {
            presenter.onPrint();
        }
    }

    @UiHandler("bookmarkButton")
    void onClickBookmark(ClickEvent e) {
        if (presenter != null) {
            presenter.onBookmark();
        }
    }

    protected void applyCriteria() {

        initTypeListBox(criteria.getType());

        if (ReportSelectView.Option.USE_DATE_RANGE.in(criteria.getType().getReportSelectOptions())) {
            dateRangeSelect.setVisible(true);
            dateRangeSelect.setValue(criteria.getRange());
            dateBox.setVisible(false);
            dateBox.setValue(null);
        } else {
            dateRangeSelect.setVisible(false);
            dateRangeSelect.setValue(null);
            dateBox.setVisible(true);
            dateBox.setValue(criteria.getRange().getEnd());
        }

        if (ReportSelectView.Option.ENABLE_ENTRY_DETAILS.in(criteria.getType().getReportSelectOptions())) {
            showDetailedCheckBox.setEnabled(true);
        } else {
            showDetailedCheckBox.setEnabled(false);
        }

        showDetailedCheckBox.setValue(criteria.getOptions().isEntryDetails());

        showDetailedAccountsCheckBox.setEnabled(criteria.getOptions().isEntryDetails());
        showDetailedAccountsCheckBox.setValue(criteria.getOptions().isEntryAccounts());

        showExchangeCheckBox.setValue(criteria.getOptions().isExchangeRates());

        showZeroBalancesCheckBox.setValue(criteria.getOptions().isZeroBalances());
    }

    protected void initTypeListBox() {
        initTypeListBox(null);
    }

    protected void initTypeListBox(ReportType selected) {
        typeListBox.clear();
        for (ReportType t : ReportType.values()) {
            typeListBox.addItem(t.getLabel());
        }

        for (int i = 0; i < ReportType.values().length; i++) {
            ReportType rt = ReportType.values()[i];
            if (rt.equals(selected)) {
                typeListBox.setSelectedIndex(i);
                break;
            }
        }
    }

    protected void initCurrencyListBox() {
        setCurrencyCodes(new String[]{Constants.SYSTEM_BASE_CURRENCY_CODE}, null);
    }

}
