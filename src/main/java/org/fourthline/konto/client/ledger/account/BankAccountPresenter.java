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

package org.fourthline.konto.client.ledger.account;

import com.google.gwt.user.client.ui.IsWidget;
import javax.inject.Inject;
import org.fourthline.konto.client.ledger.account.view.BankAccountView;
import org.fourthline.konto.shared.entity.BankAccount;
import org.seamless.gwt.validation.shared.ValidationError;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class BankAccountPresenter implements SubAccountPresenter<BankAccount> {

    final BankAccountView view;

    @Inject
    public BankAccountPresenter(BankAccountView view) {
        this.view = view;
    }

    @Override
    public IsWidget getSubAccountView() {
        return view;
    }

    @Override
    public void startWith(BankAccount account) {
        view.getBankNameProperty().set(account.getBankName());
        view.getNumberProperty().set(account.getNumber());
        view.getRoutingProperty().set(account.getRouting());
    }

    @Override
    public void showValidationErrors(List<ValidationError> errors) {
        Iterator<ValidationError> it = errors.iterator();
        while (it.hasNext()) {
            ValidationError error = it.next();
            if (!BankAccount.class.getName().equals(error.getEntity())) continue;
            if (BankAccount.Property.bankName.equals(error.getProperty())) {
                view.getBankNameProperty().showValidationError(error);
                it.remove();
            }
            if (BankAccount.Property.number.equals(error.getProperty())) {
                view.getNumberProperty().showValidationError(error);
                it.remove();
            }
            if (BankAccount.Property.routing.equals(error.getProperty())) {
                view.getRoutingProperty().showValidationError(error);
                it.remove();
            }
        }
    }

    @Override
    public void clearValidationErrors() {
        view.getBankNameProperty().clearValidationError();
        view.getNumberProperty().clearValidationError();
        view.getRoutingProperty().clearValidationError();
    }

    @Override
    public List<ValidationError> flush(BankAccount account) {
        List<ValidationError> errors = new ArrayList();

        account.setBankName(view.getBankNameProperty().get());
        account.setNumber(view.getNumberProperty().get());
        account.setRouting(view.getRoutingProperty().get());

        return errors;
    }

}
