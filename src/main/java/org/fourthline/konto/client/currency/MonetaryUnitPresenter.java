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
import org.fourthline.konto.client.currency.event.MonetaryUnitModified;
import org.fourthline.konto.client.currency.view.MonetaryUnitView;
import org.seamless.gwt.notify.client.Message;
import org.seamless.gwt.notify.client.ValidationErrorNotifyEvent;
import org.seamless.gwt.notify.client.ServerFailureNotifyEvent;
import org.seamless.gwt.notify.client.NotifyEvent;
import org.fourthline.konto.client.service.CurrencyServiceAsync;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.seamless.gwt.validation.shared.Validatable;
import org.seamless.gwt.validation.shared.ValidationError;
import org.seamless.gwt.validation.shared.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Christian Bauer
 */
public class MonetaryUnitPresenter implements MonetaryUnitView.Presenter {

    final MonetaryUnitView view;
    final EventBus bus;
    final CurrencyServiceAsync service;

    MonetaryUnit unit;

    @Inject
    public MonetaryUnitPresenter(MonetaryUnitView view, EventBus bus, CurrencyServiceAsync service) {
        this.view = view;
        this.bus = bus;
        this.service = service;
    }

    @Override
    public void startWith(MonetaryUnit unit) {
        this.unit = unit;

        view.setPresenter(this);
        view.reset(unit == null);

        if (unit != null) {
            view.getCodeProperty().set(unit.getCurrencyCode());
            view.getFractionDigitsProperty().set(unit.getFractionDigits());
            view.getPrefixProperty().set(unit.getPrefix());
        } else {
            view.getFractionDigitsProperty().set(new MonetaryUnit().getFractionDigits());
        }

        view.focus();
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

        service.store(unit, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof ValidationException) {
                    ValidationException ex = (ValidationException) caught;

                    // This is probably a FK violation
                    if (!ex.hasErrors()) {
                        bus.fireEvent(new NotifyEvent(
                                new Message(
                                        Level.WARNING,
                                        "Can't save currency, errors on server",
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
                                "Currency saved",
                                "Modifications have been stored."
                        )
                ));
                bus.fireEvent(new MonetaryUnitModified(unit));
            }
        });
    }

    @Override
    public void delete() {
        service.remove(unit, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
                bus.fireEvent(new ServerFailureNotifyEvent(caught));
            }

            @Override
            public void onSuccess(Boolean result) {
                if (!result) {
                    bus.fireEvent(new NotifyEvent(
                            new Message(
                                    Level.WARNING,
                                    "Currency '" + unit.getCurrencyCode() + "' removal failed",
                                    "First delete all accounts using this currency."
                            )
                    ));
                } else {
                    bus.fireEvent(new NotifyEvent(
                            new Message(
                                    Level.INFO,
                                    "Currency deleted",
                                    "The currency has been permanently removed."
                            )
                    ));
                    bus.fireEvent(new MonetaryUnitModified(unit));
                }
            }
        });
    }

    @Override
    public void cancel() {
        bus.fireEvent(new MonetaryUnitModified(null));
    }

    protected void clearValidationErrors() {
        view.getCodeProperty().clearValidationError();
        view.getFractionDigitsProperty().clearValidationError();
        view.getPrefixProperty().clearValidationError();
    }

    protected void showValidationErrors(List<ValidationError> errors) {
        List<ValidationError> entityErrors =
                ValidationError.filterEntity(errors, MonetaryUnit.class.getName());

        for (ValidationError error: entityErrors) {
            if (MonetaryUnit.Property.currencyCode.equals(error.getProperty()))
                view.getCodeProperty().showValidationError(error);
            else if (MonetaryUnit.Property.fractionDigits.equals(error.getProperty()))
                view.getFractionDigitsProperty().showValidationError(error);
            else if (MonetaryUnit.Property.prefix.equals(error.getProperty()))
                view.getPrefixProperty().showValidationError(error);
            else
                errors.add(error);
        }

        for (ValidationError error : errors) {
            bus.fireEvent(new ValidationErrorNotifyEvent(error));
        }
    }

    protected List<ValidationError> flushView() {
        List<ValidationError> errors = new ArrayList();

        if (unit == null)
            unit = new MonetaryUnit();

        unit.setCurrencyCode(view.getCodeProperty().get());

        Integer fractionDigits = view.getFractionDigitsProperty().get();
        unit.setFractionDigits(fractionDigits == null ? -1 : fractionDigits);

        unit.setPrefix(view.getPrefixProperty().get());

        errors.addAll(unit.validate(Validatable.GROUP_CLIENT));

        return errors;
    }
}
