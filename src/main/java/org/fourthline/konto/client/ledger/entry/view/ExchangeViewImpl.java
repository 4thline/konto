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
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import org.fourthline.konto.client.bundle.Bundle;
import org.fourthline.konto.client.ledger.Constants;
import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.seamless.gwt.component.client.binding.BigDecimalViewProperty;
import org.seamless.gwt.component.client.binding.ValidatableViewProperty;
import org.seamless.gwt.component.client.widget.BigDecimalGhostedTextBox;
import org.seamless.gwt.theme.shared.client.ThemeStyle;
import org.seamless.gwt.validation.shared.ValidationError;

import javax.inject.Inject;
import java.math.BigDecimal;

/**
 * @author Christian Bauer
 */
public class ExchangeViewImpl extends Composite implements ExchangeView {

    interface UI extends UiBinder<HTMLPanel, ExchangeViewImpl> {
    }

    private UI ui = GWT.create(UI.class);

    interface Style extends CssResource {
    }

    @UiField(provided = true)
    Bundle bundle;
    @UiField
    Style style;

    @UiField
    Label currencyPairLabel;
    @UiField(provided = true)
    BigDecimalGhostedTextBox rateTextBox;
    @UiField(provided = true)
    BigDecimalGhostedTextBox amountTextBox;

    final ValidatableViewProperty<BigDecimal> rateProperty;
    final ValidatableViewProperty<MonetaryAmount> amountProperty;

    Presenter presenter;
    MonetaryUnit monetaryUnit;

    @Inject
    public ExchangeViewImpl(Bundle bundle) {
        this.bundle = bundle;

        rateTextBox =
            new BigDecimalGhostedTextBox(getExchangeRateLabel(), ThemeStyle.GhostedTextBox()) {
                @Override
                public void onKeyUp(KeyUpEvent event) {
                    super.onKeyUp(event);
                    presenter.rateUpdated();
                }
            };

        rateProperty = new BigDecimalViewProperty(rateTextBox, ThemeStyle.FormErrorField());

        amountTextBox =
            new BigDecimalGhostedTextBox(getExchangedAmountLabel(), ThemeStyle.GhostedTextBox()) {
                @Override
                public void onKeyUp(KeyUpEvent event) {
                    super.onKeyUp(event);
                    presenter.exchangedAmountUpdated();
                }
            };

        amountProperty = new ValidatableViewProperty<MonetaryAmount>() {
            @Override
            public void reset() {
                set(null);
            }

            @Override
            public void set(MonetaryAmount value) {
                clearValidationError();
                amountTextBox.setValue(value == null ? "0" : value.getString(), true);
            }

            @Override
            public MonetaryAmount get() {
                if (amountTextBox.getValue().length() == 0) return null;
                try {
                    BigDecimal value = new BigDecimal(amountTextBox.getValue());
                    return new MonetaryAmount(monetaryUnit, value);
                } catch (Exception ex) {
                    // Well...
                }
                return null;
            }

            @Override
            public void showValidationError(ValidationError error) {
                amountTextBox.addStyleName(ThemeStyle.FormErrorField());
            }

            @Override
            public void clearValidationError() {
                amountTextBox.removeStyleName(ThemeStyle.FormErrorField());
            }

        };

        initWidget(ui.createAndBindUi(this));
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void reset(CurrencyPair pair, MonetaryAmount amount) {
        currencyPairLabel.setText(pair.getFromCode() + "/" + pair.getToCode());
        monetaryUnit = pair.getToUnit();
        rateProperty.set(pair.getExchangeRate());
        amountProperty.set(amount);
    }

    @Override
    public ValidatableViewProperty<BigDecimal> getRateProperty() {
        return rateProperty;
    }

    @Override
    public ValidatableViewProperty<MonetaryAmount> getAmountProperty() {
        return amountProperty;
    }

    protected String getExchangeRateLabel() {
        return Constants.LABEL_EXCHANGE_RATE;
    }

    protected String getExchangedAmountLabel() {
        return Constants.LABEL_EXCHANGED_AMOUNT;
    }

}
