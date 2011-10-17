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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;
import javax.inject.Inject;
import org.fourthline.konto.client.bundle.Bundle;
import org.seamless.gwt.component.client.binding.TextBoxViewProperty;
import org.seamless.gwt.component.client.binding.ValidatableViewProperty;
import org.seamless.gwt.component.client.binding.ViewProperty;
import org.seamless.gwt.theme.shared.client.ThemeStyle;

/**
 * @author Christian Bauer
 */
public class BankAccountViewImpl extends Composite implements BankAccountView {

    interface UI extends UiBinder<HTMLPanel, BankAccountViewImpl> {
    }

    private UI ui = GWT.create(UI.class);

    @UiField(provided = true)
    Bundle bundle;
    @UiField
    AccountViewStyle parentStyle;

    @UiField
    TextBox bankNameTextBox;
    @UiField
    TextBox numberTextBox;
    @UiField
    TextBox routingTextBox;

    final ValidatableViewProperty<String> bankNameProperty;
    final ValidatableViewProperty<String> numberProperty;
    final ValidatableViewProperty<String> routingProperty;

    @Inject
    public BankAccountViewImpl(Bundle bundle) {
        this.bundle = bundle;

        initWidget(ui.createAndBindUi(this));

        bankNameProperty =
                new TextBoxViewProperty(bankNameTextBox, ThemeStyle.FormErrorField());

        numberProperty =
                new TextBoxViewProperty(numberTextBox, ThemeStyle.FormErrorField());

        routingProperty =
                new TextBoxViewProperty(routingTextBox, ThemeStyle.FormErrorField());

    }

    @Override
    public ValidatableViewProperty<String> getBankNameProperty() {
        return bankNameProperty;
    }

    @Override
    public ValidatableViewProperty<String> getNumberProperty() {
        return numberProperty;
    }

    @Override
    public ValidatableViewProperty<String> getRoutingProperty() {
        return routingProperty;
    }
}