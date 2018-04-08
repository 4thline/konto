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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import org.fourthline.konto.client.bundle.Bundle;
import org.fourthline.konto.shared.query.ChartCriteria;
import org.seamless.gwt.component.client.widget.DateRangeSelect;
import org.seamless.gwt.component.client.widget.ImageTextButton;
import org.seamless.util.time.DateFormat;
import org.seamless.util.time.DateRange;

import javax.inject.Inject;
import java.util.Arrays;

/**
 * @author Christian Bauer
 */
public class ChartSelectViewImpl extends Composite implements ChartSelectView {

    interface UI extends UiBinder<DockLayoutPanel, ChartSelectViewImpl> {
    }

    interface Style extends CssResource {
    }

    private static UI ui = GWT.create(UI.class);

    @UiField(provided = true)
    final Bundle bundle;

    @UiField
    Style style;

    @UiField(provided = true)
    final DateRangeSelect dateRangeSelect;

    @UiField
    ListBox groupOptionListBox;

    @UiField
    ImageTextButton showLedgerButton;

    Presenter presenter;

    @Inject
    public ChartSelectViewImpl(Bundle bundle, DateRangeSelect dateRangeSelect) {
        this.bundle = bundle;

        this.dateRangeSelect = dateRangeSelect;

        initWidget(ui.createAndBindUi(this));

        dateRangeSelect.addValueChangeHandler(event -> {
            if (presenter != null)
                presenter.onDateRangeUpdated(event.getValue());
        });

        for (ChartCriteria.GroupOption groupOption : ChartCriteria.GroupOption.values()) {
            groupOptionListBox.addItem(groupOption.name());
        }
        groupOptionListBox.addChangeHandler(changeEvent -> {
            if (presenter != null) {
                presenter.onGroupOptionSelected(
                    ChartCriteria.GroupOption.values()[groupOptionListBox.getSelectedIndex()]
                );
            }
        });
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
        dateRangeSelect.reset();
    }

    @Override
    public void setDateFormat(DateFormat dateFormat) {
        dateRangeSelect.setDateFormat(dateFormat);
    }

    @Override
    public void setCriteria(ChartCriteria criteria) {
        dateRangeSelect.setValue(criteria.getRange());
        groupOptionListBox.setSelectedIndex(
            Arrays.asList(ChartCriteria.GroupOption.values()).indexOf(criteria.getGroupOption())
        );
    }

    @Override
    public DateRange getDateRange() {
        return dateRangeSelect.getValue() != null ? dateRangeSelect.getValue() : new DateRange();
    }


    @UiHandler("showLedgerButton")
    void onClickShowLedger(ClickEvent e) {
        if (presenter != null) {
            presenter.showLedger();
        }
    }
}
