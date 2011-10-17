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

package org.fourthline.konto.client.currency;

import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import javax.inject.Inject;
import org.fourthline.konto.client.currency.event.CurrencyPairModified;
import org.fourthline.konto.client.currency.view.CurrencyPairView;
import org.seamless.gwt.notify.client.Message;
import org.seamless.gwt.notify.client.ServerFailureNotifyEvent;
import org.seamless.gwt.notify.client.NotifyEvent;
import org.seamless.gwt.notify.client.ValidationErrorNotifyEvent;
import org.fourthline.konto.client.service.CurrencyServiceAsync;
import org.fourthline.konto.client.settings.GlobalSettings;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.seamless.gwt.validation.shared.Validatable;
import org.seamless.gwt.validation.shared.ValidationError;
import org.seamless.gwt.validation.shared.ValidationException;
import org.fourthline.konto.shared.entity.settings.GlobalOption;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Christian Bauer
 */
public class CurrencyPairPresenter implements CurrencyPairView.Presenter {

    final CurrencyPairView view;
    final EventBus bus;
    final CurrencyServiceAsync service;

    MonetaryUnit fromUnit;
    MonetaryUnit toUnit;
    CurrencyPair pair;

    @Inject
    public CurrencyPairPresenter(CurrencyPairView view, EventBus bus, CurrencyServiceAsync service) {
        this.view = view;
        this.bus = bus;
        this.service = service;
    }

    @Override
    public void startWith(MonetaryUnit fromUnit, MonetaryUnit toUnit, CurrencyPair pair) {
        this.fromUnit = fromUnit;
        this.toUnit = toUnit;
        this.pair = pair;

        view.setPresenter(this);

        view.reset(pair == null);

        if (pair != null) {
            view.getRateProperty().set(pair.getExchangeRate());
            view.getDateProperty().set(pair.getCreatedOn());
        } else {
            view.getRateProperty().set(CurrencyPair.DEFAULT_EXCHANGE_RATE);
            view.getDateProperty().set(new Date());
        }

        view.focus();
    }

    @Override
    public void onSettingsRefreshed(GlobalSettings gs) {
        view.setDateFormat(gs.getValue(GlobalOption.OPT_DATE_FORMAT));
    }

    @Override
    public void save() {
        clearValidationErrors();

        // Client-side validation including view/model data binding
        List<ValidationError> errors = flushView();
        if (errors.size() > 0) {
            bus.fireEvent(new NotifyEvent(
                    new Message(Level.WARNING, "Can't save currency", "Please correct your input.")
            ));
            showValidationErrors(errors);
            return;
        }

        service.store(pair, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof ValidationException) {
                    ValidationException ex = (ValidationException) caught;

                    // This is probably a FK violation
                    if (!ex.hasErrors()) {
                        bus.fireEvent(new NotifyEvent(
                                new Message(
                                        Level.WARNING,
                                        "Can't save exchange rate, errors on server",
                                        ex.getMessage()
                                )
                        ));
                    }

                    showValidationErrors(ex.getErrors());
                } else {
                    bus.fireEvent(new ServerFailureNotifyEvent(caught));
                }
            }

            @Override
            public void onSuccess(Void result) {
                bus.fireEvent(new NotifyEvent(
                        new Message(
                                Level.INFO,
                                "Exchange rate saved",
                                "Modifications have been stored."
                        )
                ));
                bus.fireEvent(new CurrencyPairModified(pair));
            }
        });
    }

    @Override
    public void delete() {
        service.remove(pair, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                bus.fireEvent(new ServerFailureNotifyEvent(caught));
            }

            @Override
            public void onSuccess(Void result) {
                bus.fireEvent(new NotifyEvent(
                        new Message(
                                Level.INFO,
                                "Exchange rate deleted",
                                "The exchange rate has been permanently removed."
                        )
                ));
                bus.fireEvent(new CurrencyPairModified(pair));
            }
        });
    }

    @Override
    public void cancel() {
        bus.fireEvent(new CurrencyPairModified(null));
    }

    protected void clearValidationErrors() {
        view.getRateProperty().clearValidationError();
        view.getDateProperty().clearValidationError();
    }

    protected void showValidationErrors(List<ValidationError> errors) {
        List<ValidationError> entityErrors =
                ValidationError.filterEntity(errors, CurrencyPair.class.getName());

        for (ValidationError error : entityErrors) {
            if (CurrencyPair.Property.exchangeRate.equals(error.getProperty()))
                view.getRateProperty().showValidationError(error);
            else if (CurrencyPair.Property.createdOn.equals(error.getProperty()))
                view.getDateProperty().showValidationError(error);
            else
                errors.add(error);
        }

        for (ValidationError error : errors) {
            bus.fireEvent(new ValidationErrorNotifyEvent(error));
        }
    }

    protected List<ValidationError> flushView() {
        List<ValidationError> errors = new ArrayList();

        if (pair == null) {
            pair = new CurrencyPair(fromUnit, toUnit, CurrencyPair.DEFAULT_EXCHANGE_RATE);
        }

        pair.setExchangeRate(view.getRateProperty().get());
        pair.setCreatedOn(view.getDateProperty().get());

        errors.addAll(pair.validate(Validatable.GROUP_CLIENT));

        return errors;
    }

}
