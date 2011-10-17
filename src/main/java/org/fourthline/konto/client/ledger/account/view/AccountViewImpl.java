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

package org.fourthline.konto.client.ledger.account.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.fourthline.konto.client.bundle.Bundle;
import org.fourthline.konto.client.ledger.account.AccountGroupSuggestion;
import org.fourthline.konto.shared.AccountType;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.seamless.gwt.component.client.binding.BigDecimalViewProperty;
import org.seamless.gwt.component.client.binding.DateBoxViewProperty;
import org.seamless.gwt.component.client.binding.TextBoxViewProperty;
import org.seamless.gwt.component.client.binding.ValidatableViewProperty;
import org.seamless.gwt.component.client.suggest.SuggestionSelectView;
import org.seamless.gwt.component.client.widget.AutocompleteDateTextBox;
import org.seamless.gwt.theme.shared.client.ThemeStyle;
import org.seamless.gwt.validation.shared.ValidationError;
import org.seamless.util.time.DateFormat;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class AccountViewImpl extends Composite implements AccountView {

    interface UI extends UiBinder<DockLayoutPanel, AccountViewImpl> {
    }

    private UI ui = GWT.create(UI.class);

    @UiField(provided = true)
    Bundle bundle;
    @UiField
    AccountViewStyle style;

    @UiField
    VerticalPanel form;
    @UiField
    Button saveButton;
    @UiField
    Button deleteButton;
    @UiField
    Button cancelButton;
    @UiField
    ListBox typeListBox;
    @UiField
    TextBox nameTextBox;
    @UiField
    TextBox initialBalanceTextBox;
    @UiField
    ListBox currencyListBox;
    @UiField
    TableRowElement typeRow;
    @UiField
    AutocompleteDateTextBox effectiveOnDateBox;
    @UiField
    TableRowElement currencyRow;
    @UiField
    SimplePanel accountGroupSelectPanel;
    @UiField
    Label initialBalanceCurrencyLabel;
    @UiField
    VerticalPanel nameErrors;
    @UiField
    VerticalPanel groupErrors;
    @UiField
    VerticalPanel effectiveOnErrors;
    @UiField
    VerticalPanel initialBalanceErrors;

    Presenter presenter;
    List<IsWidget> addedRows = new ArrayList();

    final ValidatableViewProperty<String> nameProperty;
    final SuggestionSelectView<AccountGroupSuggestion> accountGroupSelectView;
    final ValidatableViewProperty<Date> effectiveOnProperty;
    final ValidatableViewProperty<BigDecimal> initialBalanceProperty;

    @Inject
    public AccountViewImpl(Bundle bundle, AccountGroupSelectView accountGroupSelectView) {
        this.bundle = bundle;
        this.accountGroupSelectView = accountGroupSelectView;

        initWidget(ui.createAndBindUi(this));

        initTypeListBox();

        nameProperty =
            new TextBoxViewProperty(nameErrors, nameTextBox, ThemeStyle.FormErrorField());

        effectiveOnProperty =
            new DateBoxViewProperty(effectiveOnErrors, effectiveOnDateBox, ThemeStyle.FormErrorField());

        initialBalanceProperty =
            new BigDecimalViewProperty(initialBalanceErrors, initialBalanceTextBox, ThemeStyle.FormErrorField());

        accountGroupSelectPanel.setWidget(accountGroupSelectView);
        accountGroupSelectView.getTextBox().addStyleName(style.groupNameBox());
        accountGroupSelectView.getButton().addStyleName(style.groupShowAllButton());

        currencyListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                presenter.currencySelected(currencyListBox.getSelectedIndex());
            }
        });
    }

    protected void initTypeListBox() {
        typeListBox.clear();
        for (AccountType type : AccountType.values()) {
            typeListBox.addItem(type.getLabel());
        }
        typeListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                presenter.typeSelected(AccountType.values()[typeListBox.getSelectedIndex()]);
            }
        });
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
        setVisible(false); // Avoid jitter and wait until setCreateMode() is complete
    }

    @Override
    public void setDateFormat(DateFormat dateFormat) {
        effectiveOnDateBox.setDateFormat(dateFormat);
    }

    @Override
    public void reset() {
        initTypeListBox();
        nameProperty.reset();
        groupErrors.clear();
        accountGroupSelectView.reset();
        effectiveOnProperty.reset();
        currencyListBox.clear();
        initialBalanceProperty.reset();
        initialBalanceCurrencyLabel.setText("");
        for (IsWidget addedRow : addedRows) {
            removeFormPanelRow(addedRow);
        }
    }

    @Override
    public void focus() {
        if (deleteButton.isVisible()) {
            nameTextBox.selectAll();
            nameTextBox.setFocus(true);
        } else {
            typeListBox.setFocus(true);
        }
    }

    @Override
    public SuggestionSelectView<AccountGroupSuggestion> getAccountGroupSelectView() {
        return accountGroupSelectView;
    }

    @Override
    public void showValidationErrorAccountGroup(ValidationError error) {
        Label l = new Label(error.getMessage());
        l.setStyleName(ThemeStyle.ErrorMessage());
        groupErrors.add(l);
        accountGroupSelectView.showValidationError(error);
    }

    @Override
    public void clearValidationErrorAccountGroup() {
        groupErrors.clear();
        accountGroupSelectView.clearValidationError();
    }

    @Override
    public void setCreateMode(boolean createMode) {
        typeRow.setClassName(
            createMode
                ? ThemeStyle.FormRowVisible()
                : ThemeStyle.FormRowInvisible()
        );
        currencyRow.setClassName(
            createMode
                ? ThemeStyle.FormRowVisible()
                : ThemeStyle.FormRowInvisible()
        );
        deleteButton.setVisible(!createMode);
        setVisible(true);
    }

    @Override
    public ValidatableViewProperty<String> getNameProperty() {
        return nameProperty;
    }

    @Override
    public ValidatableViewProperty<Date> getEffectiveOnProperty() {
        return effectiveOnProperty;
    }

    @Override
    public ValidatableViewProperty<BigDecimal> getInitialBalanceProperty() {
        return initialBalanceProperty;
    }

    @Override
    public void setCurrency(String string) {
        for (int i = 0; i < currencyListBox.getItemCount(); i++) {
            if (currencyListBox.getItemText(i).equals(string)) {
                currencyListBox.setSelectedIndex(i);
            }
        }
        initialBalanceCurrencyLabel.setText(string);
    }

    @Override
    public void setCurrencies(List<MonetaryUnit> currencies) {
        currencyListBox.clear();
        for (MonetaryUnit currency : currencies) {
            currencyListBox.addItem(currency.getCurrencyCode());
        }
    }

    @Override
    public void addFormPanelRow(IsWidget widget) {
        addedRows.add(widget);
        form.add(widget);
    }

    @Override
    public void removeFormPanelRow(IsWidget widget) {
        form.remove(widget);
    }

    @UiHandler("saveButton")
    void onClickSave(ClickEvent e) {
        if (presenter != null) {
            presenter.save();
        }
    }

    @UiHandler("deleteButton")
    void onClickDelete(ClickEvent e) {
        if (presenter != null && Window.confirm(
            "Are you are you want to delete this account? " +
                "All entries will be deleted!"
        )) {
            presenter.delete();
        }
    }

    @UiHandler("cancelButton")
    void onClickCancel(ClickEvent e) {
        if (presenter != null) {
            presenter.cancel();
        }
    }

}