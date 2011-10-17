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

package org.fourthline.konto.client.ledger.entry;

import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.fourthline.konto.client.service.CurrencyServiceAsync;
import org.fourthline.konto.client.ledger.entry.view.ExchangeView;
import org.seamless.gwt.notify.client.Message;
import org.seamless.gwt.notify.client.ServerFailureNotifyEvent;
import org.seamless.gwt.notify.client.NotifyEvent;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.fourthline.konto.shared.entity.Split;
import org.seamless.gwt.validation.shared.ValidationError;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Christian Bauer
 */
public class ExchangePresenter implements ExchangeView.Presenter {

    class ResetRateAmountCallback implements AsyncCallback<CurrencyPair> {
        @Override
        public void onFailure(Throwable caught) {
            bus.fireEvent(new ServerFailureNotifyEvent(caught));
        }

        @Override
        public void onSuccess(CurrencyPair result) {
            if (result != null) {
                currencyPair = result;
                bus.fireEvent(new NotifyEvent(
                        new Message(
                                Level.INFO,
                                "Retrieved exchange rate from server",
                                "Suggested rate is dated " + currencyPair.getCreatedOn() + "."
                        )
                ));
            } else {
                currencyPair =
                        new CurrencyPair(fromUnit, toUnit, CurrencyPair.DEFAULT_EXCHANGE_RATE);
            }
            MonetaryAmount exchangedAmount =
                    originalAmount != null
                            ? currencyPair.getExchangedAmount(originalAmount)
                            : null;

            view.reset(currencyPair, exchangedAmount);
        }
    }

    final ExchangeView view;
    final EventBus bus;
    final CurrencyServiceAsync currencyService;

    Date forDay;
    MonetaryUnit fromUnit;
    MonetaryUnit toUnit;
    MonetaryAmount originalAmount;
    CurrencyPair currencyPair;

    public ExchangePresenter(ExchangeView view,
                             EventBus bus,
                             CurrencyServiceAsync currencyService) {
        this.view = view;
        this.bus = bus;
        this.currencyService = currencyService;
    }

    @Override
    public void startWith(Date forDay, MonetaryUnit fromUnit, MonetaryUnit toUnit,
                          MonetaryAmount originalAmt, MonetaryAmount exchangedAmt) {

        this.forDay = forDay;
        this.fromUnit = fromUnit;
        this.toUnit = toUnit;
        this.originalAmount = originalAmt != null ? originalAmt.abs() : originalAmt;

        view.setPresenter(this);

        if (exchangedAmt != null && exchangedAmt.signum() != 0) {
            exchangedAmt = exchangedAmt.abs();
            currencyPair = new CurrencyPair(fromUnit, toUnit, originalAmount, exchangedAmt);
            view.reset(currencyPair, exchangedAmt);
        } else {
            currencyService.getCurrencyPair(fromUnit, toUnit, forDay, new ResetRateAmountCallback());
        }
    }

    @Override
    public void updateForDay(Date date) {
        // Not initialized yet
        if (fromUnit == null || toUnit == null) return;

        // Valid rate has already been entered, don't retrieve it again and don't overwrite the user value
        BigDecimal enteredRate = view.getRateProperty().get();
        if (enteredRate != null &&
                !enteredRate.equals(CurrencyPair.DEFAULT_EXCHANGE_RATE) &&
                enteredRate.signum() > 0) return;

        this.forDay = date;
        currencyService.getCurrencyPair(fromUnit, toUnit, forDay, new ResetRateAmountCallback());
    }

    @Override
    public void updateOriginalAmount(MonetaryAmount amt) {
        if (fromUnit == null || toUnit == null) return;
        originalAmount = amt != null ? amt.abs() : amt;
        setViewExchangedAmount();
    }

    @Override
    public void rateUpdated() {
        // Calculate the target amount using the exchange rate
        setViewExchangedAmount();
    }

    @Override
    public void exchangedAmountUpdated() {
        MonetaryAmount exchangedAmount = view.getAmountProperty().get();

        if (exchangedAmount == null) {
            setViewExchangedAmount();
        } else {
            // If the user entered a "-" symbol, negate that
            if (exchangedAmount.signum() < 0) {
                exchangedAmount = exchangedAmount.abs();
                view.getAmountProperty().set(exchangedAmount);
            }

            if (originalAmount != null && originalAmount.signum() > 0 &&
                    exchangedAmount.signum() > 0) {
                BigDecimal exchangeRate = CurrencyPair.getExchangeRate(originalAmount, exchangedAmount);
                view.getRateProperty().set(exchangeRate);
            }
        }
    }

    @Override
    public MonetaryAmount getExchangedAmount() {
        return view.getAmountProperty().get();
    }

    @Override
    public void clearValidationErrors() {
        view.getRateProperty().clearValidationError();
        view.getAmountProperty().clearValidationError();
    }

    @Override
    public void showValidationErrors(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            if (Split.Property.amount.equals(error.getProperty())) {
                view.getRateProperty().showValidationError(error);
                view.getAmountProperty().showValidationError(error);
            }
        }
    }

    protected BigDecimal getViewExchangeRate() {
        BigDecimal exchangeRate = view.getRateProperty().get();
        // Use the default (1.0) exchange rate if there is none entered or invalid
        if (exchangeRate == null || exchangeRate.signum() < 1)
            exchangeRate = CurrencyPair.DEFAULT_EXCHANGE_RATE;
        return exchangeRate;
    }

    protected void setViewExchangedAmount() {
        if (originalAmount != null && originalAmount.signum() > 0) {
            // Calculcate the exchanged amount based on original amount and visible rate
            CurrencyPair cp = new CurrencyPair(fromUnit, toUnit, getViewExchangeRate());
            MonetaryAmount exchangedAmount = cp.getExchangedAmount(originalAmount);
            view.getAmountProperty().set(exchangedAmount);
        } else {
            // Set to zero
            view.getAmountProperty().set(new MonetaryAmount(toUnit));
        }
    }
}
