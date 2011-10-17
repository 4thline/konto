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
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.fourthline.konto.client.bundle.Bundle;
import org.seamless.gwt.component.client.binding.TextBoxIntegerViewProperty;
import org.seamless.gwt.component.client.binding.TextBoxViewProperty;
import org.seamless.gwt.component.client.binding.ValidatableViewProperty;
import org.seamless.gwt.theme.shared.client.ThemeStyle;

import javax.inject.Inject;

/**
 * @author Christian Bauer
 */
public class MonetaryUnitViewImpl extends Composite implements MonetaryUnitView {

    interface UI extends UiBinder<VerticalPanel, MonetaryUnitViewImpl> {
    }

    private UI ui = GWT.create(UI.class);

    interface Style extends CssResource {

    }

    @UiField(provided = true)
    Bundle bundle;
    @UiField
    Style style;

    @UiField
    VerticalPanel codeErrors;
    @UiField
    TextBox codeTextBox;

    @UiField
    VerticalPanel fractionDigitsErrors;
    @UiField
    TextBox fractionDigitsTextBox;

    @UiField
    VerticalPanel prefixErrors;
    @UiField
    TextBox prefixTextBox;

    @UiField
    Button saveButton;
    @UiField
    Button deleteButton;
    @UiField
    Button cancelButton;

    final ValidatableViewProperty<String> codeProperty;
    final ValidatableViewProperty<String> prefixProperty;
    final ValidatableViewProperty<Integer> fractionDigitsProperty;

    Presenter presenter;

    @Inject
    public MonetaryUnitViewImpl(Bundle bundle) {
        this.bundle = bundle;

        initWidget(ui.createAndBindUi(this));

        codeProperty =
                new TextBoxViewProperty(codeErrors, codeTextBox, ThemeStyle.FormErrorField());
        fractionDigitsProperty =
                new TextBoxIntegerViewProperty(fractionDigitsErrors, fractionDigitsTextBox, ThemeStyle.FormErrorField());
        prefixProperty =
                new TextBoxViewProperty(prefixErrors, prefixTextBox, ThemeStyle.FormErrorField());

    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void reset(boolean createMode) {
        codeProperty.reset();
        prefixProperty.reset();
        codeTextBox.setEnabled(createMode);
        fractionDigitsTextBox.setEnabled(createMode);
        deleteButton.setVisible(!createMode);
        cancelButton.setVisible(!createMode);
    }

    @Override
    public void focus() {
        codeTextBox.setFocus(true);
    }

    @Override
    public ValidatableViewProperty<String> getCodeProperty() {
        return codeProperty;
    }

    @Override
    public ValidatableViewProperty<Integer> getFractionDigitsProperty() {
        return fractionDigitsProperty;
    }

    @Override
    public ValidatableViewProperty<String> getPrefixProperty() {
        return prefixProperty;
    }

    @UiHandler("saveButton")
    void onClickSave(ClickEvent e) {
        if (presenter != null) {
            presenter.save();
        }
    }

    @UiHandler("deleteButton")
    void onClickDelete(ClickEvent e) {
        if (presenter != null && Window.confirm("Are you are you want to delete this currency?")) {
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