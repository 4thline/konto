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

package org.fourthline.konto.client.currency.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.fourthline.konto.client.bundle.Bundle;
import org.seamless.gwt.component.client.binding.BigDecimalViewProperty;
import org.seamless.gwt.component.client.binding.DateBoxViewProperty;
import org.seamless.gwt.component.client.binding.ValidatableViewProperty;
import org.seamless.gwt.component.client.widget.AutocompleteDateTextBox;
import org.seamless.gwt.component.client.widget.EnterKeyHandler;
import org.seamless.gwt.theme.shared.client.ThemeStyle;
import org.seamless.util.time.DateFormat;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Christian Bauer
 */
public class CurrencyPairViewImpl extends Composite implements CurrencyPairView {

    interface UI extends UiBinder<VerticalPanel, CurrencyPairViewImpl> {
    }

    private UI ui = GWT.create(UI.class);

    interface Style extends CssResource{

    }

    @UiField(provided = true)
    Bundle bundle;
    @UiField
    Style style;

    @UiField
    VerticalPanel rateErrors;
    @UiField
    TextBox rateTextBox;

    @UiField
    VerticalPanel dateErrors;
    @UiField
    AutocompleteDateTextBox dateBox;

    @UiField
    Button saveButton;
    @UiField
    Button deleteButton;
    @UiField
    Button cancelButton;

    final KeyUpHandler immediateSubmitHandler =
            new EnterKeyHandler() {
                @Override
                protected void onEnterKey() {
                    presenter.save();
                }
            };

    final ValidatableViewProperty<BigDecimal> rateProperty;
    final ValidatableViewProperty<Date> dateProperty;

    Presenter presenter;

    @Inject
    public CurrencyPairViewImpl(Bundle bundle) {
        this.bundle = bundle;

        initWidget(ui.createAndBindUi(this));

        rateProperty =
                new BigDecimalViewProperty(rateErrors, rateTextBox, ThemeStyle.FormErrorField());
        dateProperty =
                new DateBoxViewProperty(dateErrors, dateBox, ThemeStyle.FormErrorField());

        rateTextBox.addKeyUpHandler(immediateSubmitHandler);

    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setDateFormat(DateFormat dateFormat) {
        dateBox.setDateFormat(dateFormat);
    }

    @Override
    public void reset(boolean createMode) {
        rateProperty.reset();
        dateProperty.reset();
        cancelButton.setVisible(!createMode);
        deleteButton.setVisible(!createMode);
    }

    @Override
    public void focus() {
        dateBox.getTextBox().selectAll();
        dateBox.getTextBox().setFocus(true);
        dateBox.hideDatePicker();
    }

    @Override
    public ValidatableViewProperty<BigDecimal> getRateProperty() {
        return rateProperty;
    }

    @Override
    public ValidatableViewProperty<Date> getDateProperty() {
        return dateProperty;
    }

    @UiHandler("saveButton")
    void onClickSave(ClickEvent e) {
        if (presenter != null) {
            presenter.save();
        }
    }

    @UiHandler("deleteButton")
    void onClickDelete(ClickEvent e) {
        if (presenter != null) {
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