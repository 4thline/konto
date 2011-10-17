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
import javax.inject.Inject;
import org.fourthline.konto.client.service.LedgerServiceAsync;
import org.fourthline.konto.client.ledger.entry.view.EntrySummaryView;
import org.seamless.gwt.notify.client.Message;
import org.seamless.gwt.notify.client.NotifyEvent;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.Entry;
import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.fourthline.konto.shared.entity.Split;
import org.seamless.gwt.validation.shared.ValidationError;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Christian Bauer
 */
public class EntrySummaryPresenter extends DescriptionPresenter implements EntrySummaryView.Presenter {

    final EntrySummaryView view;

    Account currentAccount;
    Entry entry;

    @Inject
    public EntrySummaryPresenter(EntrySummaryView view,
                                 EventBus bus,
                                 LedgerServiceAsync ledgerService) {
        super(bus, ledgerService);
        this.view = view;
    }

    @Override
    public EntrySummaryView getView() {
        return view;
    }

    @Override
    public void startWith(Account currentAccount, Entry entry) {
        this.currentAccount = currentAccount;
        this.entry = entry;

        view.setPresenter(this);
        view.setSplitSuggestionHandler(this);

        view.setCurrentAccount(currentAccount);
        view.setDescription(entry.getDescription());

    }

    @Override
    public boolean isNewDescription(String text) {
        return entry.getDescription() == null && text != null ||
                !entry.getDescription().equals(text);
    }

    @Override
    public void suggest(Split suggestedSplit) {
        // TODO: Do nothing?
    }

    @Override
    public MonetaryUnit getMonetaryUnit() {
        return null;
    }

    @Override
    public void setMonetaryUnit(MonetaryUnit unit) {
        // NOOP
    }

    @Override
    public MonetaryAmount getDebit() {
        // NOOP
        return null;
    }

    @Override
    public void setDebit(MonetaryAmount amount) {
        view.setDebit(amount);
    }

    @Override
    public MonetaryAmount getCredit() {
        // NOOP
        return null;
    }

    @Override
    public void setCredit(MonetaryAmount amount) {
        view.setCredit(amount);
    }

    @Override
    public List<ValidationError> flushView() {
        List<ValidationError> errors = new ArrayList();

        if (view.getDescription() == null || view.getDescription().length() == 0) {
            errors.add(new ValidationError(
                    Entry.class.getName(),
                    Entry.Property.description,
                    "Entry description is required."
            ));
        } else {
            entry.setDescription(view.getDescription());
        }

        return errors;
    }

    @Override
    public void clearValidationErrors() {
        view.clearValidationErrorDescription();
    }

    @Override
    public void showValidationErrors(List<ValidationError> errors) {
        StringBuilder sb = new StringBuilder();
        for (ValidationError error : errors) {
            if (Entry.class.getName().equals(error.getEntity()) &&
                    Entry.Property.description.equals(error.getProperty())) {
                view.showValidationErrorDescription(error);
                sb.append(error.getMessage()).append(" ");
            }
        }

        if (sb.length() > 0) {
            bus.fireEvent(new NotifyEvent(
                    new Message(
                            Level.WARNING,
                            "Invalid input",
                            sb.toString()
                    )
            ));
        }
    }
}
