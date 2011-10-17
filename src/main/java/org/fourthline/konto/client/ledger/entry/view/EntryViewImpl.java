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

package org.fourthline.konto.client.ledger.entry.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.fourthline.konto.client.bundle.Bundle;
import org.seamless.gwt.component.client.binding.ValidatableViewProperty;
import org.seamless.gwt.component.client.binding.ViewProperty;
import org.seamless.gwt.component.client.widget.AutocompleteDateTextBox;
import org.seamless.gwt.theme.shared.client.ThemeStyle;
import org.seamless.util.time.DateFormat;
import org.seamless.gwt.validation.shared.ValidationError;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author Christian Bauer
 */
public class EntryViewImpl extends Composite implements EntryView {

    interface UI extends UiBinder<VerticalPanel, EntryViewImpl> {
    }

    private UI ui = GWT.create(UI.class);

    interface Style extends CssResource {

        String deleteSplitColumn();

        String splitViewColumn();

        String splitViewRowOdd();

        String splitViewRowEven();

        String splitView();

        String deleteSplitButton();
    }

    @UiField(provided = true)
    Bundle bundle;
    @UiField
    Style style;

    @UiField
    AutocompleteDateTextBox effectiveOnDateBox;

    @UiField
    SimplePanel entrySummaryPanel;

    @UiField
    FlexTable splitsTable;

    @UiField
    Button addSplitButton;
    @UiField
    Button saveButton;
    @UiField
    Button deleteButton;
    @UiField
    Button cancelButton;
    @UiField
    SimplePanel addSplitCell;

    EntryView.Presenter presenter;

    final ValidatableViewProperty<Date> effectiveOnProperty;

    @Inject
    public EntryViewImpl(final Bundle bundle) {
        this.bundle = bundle;
        initWidget(ui.createAndBindUi(this));

        splitsTable.getColumnFormatter().addStyleName(1, style.splitViewColumn());

        effectiveOnDateBox.getTextBox().addValueChangeHandler(new ValueChangeHandler() {
            @Override
            public void onValueChange(ValueChangeEvent event) {
                if (effectiveOnDateBox.getValue() != null)
                    presenter.dateEntered(effectiveOnDateBox.getValue());
            }
        });

        effectiveOnProperty = new ValidatableViewProperty<Date>() {
            @Override
            public void reset() {
                set(new Date());
            }

            @Override
            public void set(Date value) {
                effectiveOnDateBox.setValue(value, true);
            }

            @Override
            public Date get() {
                return effectiveOnDateBox.getValue();
            }

            @Override
            public void showValidationError(ValidationError error) {
                effectiveOnDateBox.getTextBox().addStyleName(ThemeStyle.FormErrorField());
            }

            @Override
            public void clearValidationError() {
                effectiveOnDateBox.getTextBox().removeStyleName(ThemeStyle.FormErrorField());
            }
        };
    }

    @Override
    public void focus(Boolean selectDay) {
        if (selectDay != null && selectDay)
            effectiveOnDateBox.selectDay();
        else
            effectiveOnDateBox.getTextBox().selectAll();
        effectiveOnDateBox.getTextBox().setFocus(true);
    }

    @Override
    public void setPresenter(EntryView.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setDateFormat(DateFormat dateFormat) {
        effectiveOnDateBox.setDateFormat(dateFormat);
    }

    @Override
    public void reset() {
        effectiveOnProperty.reset();
        entrySummaryPanel.clear();
        splitsTable.removeAllRows();
        hideSplitDelete();
        showSplitAdd();
    }

    @Override
    public ValidatableViewProperty<Date> getEffectiveOnProperty() {
        return effectiveOnProperty;
    }

    @Override
    public void showEntrySummaryView(EntrySummaryView summaryView) {
        entrySummaryPanel.clear();
        entrySummaryPanel.add(summaryView);
        entrySummaryPanel.setVisible(true);
    }

    @Override
    public void removeEntrySummaryView() {
        entrySummaryPanel.clear();
        entrySummaryPanel.setVisible(false);
    }

    @Override
    public void hideSplitDelete() {
        for (int i = 0; i < splitsTable.getRowCount(); ++i) {
            splitsTable.getWidget(i, 0).setVisible(false);
        }
        splitsTable.getColumnFormatter().removeStyleName(0, style.deleteSplitColumn());
    }

    @Override
    public void showSplitDelete() {
        for (int i = 0; i < splitsTable.getRowCount(); ++i) {
            splitsTable.getWidget(i, 0).setVisible(true);
        }
        splitsTable.getColumnFormatter().addStyleName(0, style.deleteSplitColumn());
    }

    @Override
    public void hideSplitAdd() {
        addSplitCell.setVisible(false);
    }

    @Override
    public void showSplitAdd() {
        addSplitCell.setVisible(true);
    }

    @Override
    public void addSplitView(SplitView splitView) {

        final int row =
                splitsTable.getRowCount() == 0 ? 0 : splitsTable.getRowCount();

        final Button deleteSplitButton = new Button("Delete Split");
        deleteSplitButton.addStyleName(ThemeStyle.FormButton());
        deleteSplitButton.addStyleName(style.deleteSplitButton());
        deleteSplitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                FlexTable.Cell clickedCell = splitsTable.getCellForEvent(event);
                presenter.removeSplit(clickedCell.getRowIndex());
            }
        });

        SimplePanel deleteButtonWrapper = new SimplePanel();
        deleteButtonWrapper.addStyleName(ThemeStyle.FormButton());
        deleteButtonWrapper.add(deleteSplitButton);
        deleteButtonWrapper.setVisible(false);

        SimplePanel splitViewWrapper = new SimplePanel();
        splitViewWrapper.addStyleName(style.splitView());
        splitViewWrapper.add(splitView);

        splitsTable.setWidget(row, 0, deleteButtonWrapper);
        splitsTable.setWidget(row, 1, splitViewWrapper);

        styleEntryView(row);
    }

    @Override
    public void removeSplitView(int row) {
        splitsTable.removeRow(row);
        for (int i = 0; i < splitsTable.getRowCount(); i++) {
            styleEntryView(i);
        }
    }

    protected void styleEntryView(Integer row) {
        HTMLTable.RowFormatter rf = splitsTable.getRowFormatter();
        rf.addStyleName(
                row,
                (row == 0 || ((row % 2) == 0))
                        ? style.splitViewRowEven()
                        : style.splitViewRowOdd()
        );
    }

    @UiHandler("addSplitButton")
    void onClickAddSplit(ClickEvent e) {
        if (presenter != null) {
            presenter.addSplit();
        }
    }

    @UiHandler("saveButton")
    void onClickSaveEntry(ClickEvent e) {
        if (presenter != null) {
            presenter.saveEntry();
        }
    }

    @UiHandler("deleteButton")
    void onClickDeleteEntry(ClickEvent e) {
        if (presenter != null && Window.confirm("Are you are you want to delete the entry?")) {
            presenter.deleteEntry();
        }
    }

    @UiHandler("cancelButton")
    void onClickCancel(ClickEvent e) {
        if (presenter != null) {
            presenter.cancel();
        }
    }

}
