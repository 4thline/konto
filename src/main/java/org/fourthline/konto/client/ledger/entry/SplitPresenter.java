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
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import javax.inject.Inject;

import org.fourthline.konto.client.ledger.LedgerPlace;
import org.fourthline.konto.client.service.CurrencyServiceAsync;
import org.fourthline.konto.client.service.LedgerServiceAsync;
import org.fourthline.konto.client.ledger.entry.event.EntryEditAmountUpdated;
import org.fourthline.konto.client.ledger.entry.event.EntryEditSubmit;
import org.fourthline.konto.client.ledger.entry.view.ExchangeView;
import org.fourthline.konto.client.ledger.entry.view.SplitView;
import org.seamless.gwt.notify.client.Message;
import org.seamless.gwt.notify.client.ServerFailureNotifyEvent;
import org.seamless.gwt.notify.client.NotifyEvent;
import org.seamless.gwt.notify.client.ValidationErrorNotifyEvent;
import org.fourthline.konto.shared.LedgerCoordinates;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.DebitCreditHolder;
import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.fourthline.konto.shared.entity.Split;
import org.seamless.gwt.validation.shared.ValidationError;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Christian Bauer
 */
public class SplitPresenter extends DescriptionPresenter implements SplitView.Presenter {

    final SplitView view;
    final AccountSelectPresenter accountSelectPresenter;
    final ExchangeView.Presenter exchangePresenter;
    final PlaceController placeController;

    Account currentAccount;
    Date effectiveDate;
    Split split;
    boolean editEntry;

    @Inject
    public SplitPresenter(final SplitView view,
                          EventBus bus,
                          LedgerServiceAsync ledgerService,
                          CurrencyServiceAsync currencyService,
                          PlaceController placeController) {
        super(bus, ledgerService);
        this.view = view;
        this.placeController = placeController;

        exchangePresenter =
                new ExchangePresenter(
                        view.getExchangeView(),
                        bus,
                        currencyService
                );

        accountSelectPresenter =
                new AccountSelectPresenter(
                        view.getAccountSelectView(),
                        bus,
                        ledgerService
                ) {

                    @Override
                    protected void onAccountDeselect() {
                        super.onAccountDeselect();
                        updateExchange(null);
                        view.enableSwitch(false);
                    }

                    @Override
                    protected void onAccountSelection(Account selected) {
                        super.onAccountSelection(selected);
                        Long id = editEntry ? split.getAccountId() : split.getEntry().getAccountId();
                        if (selected != null && selected.getId().equals(id)) {
                            view.enableSwitch(true);
                        } else {
                            view.enableSwitch(false);
                        }
                        updateExchange(selected);
                    }
                };
    }

    public AccountSelectPresenter getAccountSelectPresenter() {
        return accountSelectPresenter;
    }

    public ExchangeView.Presenter getExchangePresenter() {
        return exchangePresenter;
    }

    @Override
    public SplitView getView() {
        return view;
    }

    public Split getSplit() {
        return split;
    }

    @Override
    public void startWith(Account currentAccount, final Split split) {
        this.currentAccount = currentAccount;
        this.split = split;

        // Are we on the "owning" account where this entry was created (or is it
        // a new entry) or are we on the "other" side and just looking at the
        // split of an entry made somewhere else. This is key to the
        // logic of this presenter.
        this.editEntry =
                split.getEntry().getAccountId() == null
                        || split.getEntry().getAccountId().equals(currentAccount.getId());

        view.setPresenter(this);
        view.setSplitSuggestionHandler(this);
        view.setCurrentAccount(currentAccount);
        view.setDescription(split.getDescription());

        // Order sensitive inits here

        initViewDebitCredit();

        // Load the selected account (or not)
        Long selectedAccountId = null;
        if (editEntry && split.getAccountId() != null) {
            selectedAccountId = split.getAccountId();
        } else if (!editEntry && split.getEntry().getAccountId() != null) {
            selectedAccountId = split.getEntry().getAccountId();
        }

        if (selectedAccountId != null) {
            getLedgerService().getAccount(selectedAccountId, new AsyncCallback<Account>() {
                @Override
                public void onFailure(Throwable caught) {
                    bus.fireEvent(new ServerFailureNotifyEvent(caught));
                }

                @Override
                public void onSuccess(Account result) {
                    accountSelectPresenter.startWith(
                            SplitPresenter.this.currentAccount.getId(),
                            result
                    );
                    updateExchange(result);

                    if (split.getEntry().getId() != null && split.getId() != null) {
                        view.enableSwitch(true);
                    }
                }
            });
        } else {
            accountSelectPresenter.startWith(currentAccount.getId(), null);
            view.enableSwitch(false);
        }
    }

    @Override
    public void setEffectiveDate(Date date) {
        exchangePresenter.updateForDay(date);
        this.effectiveDate = date;
    }

    @Override
    public boolean isNewDescription(String text) {
        return split.getDescription() == null && text != null ||
                !split.getDescription().equals(text);
    }

    @Override
    public void suggest(Split suggestedSplit) {

        boolean editEntrySuggested =
                suggestedSplit.getEntry().getAccountId().equals(currentAccount.getId());

        // Only "suggest" if the user hasn't entered anything
        if (view.getDebit() == null && view.getCredit() == null) {
            DebitCreditHolder.Accessor.setDebitOrCredit(
                    view,
                    editEntrySuggested
                            ? suggestedSplit.getEntryAmount()
                            : suggestedSplit.getAmount()
            );
        }

        // ... or selected anything
        if (accountSelectPresenter.getSelected() == null) {
            Long retrieveAccountId;
            if (editEntrySuggested) {
                retrieveAccountId = suggestedSplit.getAccountId();
            } else {
                retrieveAccountId = suggestedSplit.getEntry().getAccountId();
            }
            getLedgerService().getAccount(retrieveAccountId, new AsyncCallback<Account>() {
                @Override
                public void onFailure(Throwable caught) {
                    bus.fireEvent(new ServerFailureNotifyEvent(caught));
                }

                @Override
                public void onSuccess(Account result) {
                    // Don't overwrite what the user might have typed meanwhile
                    if (accountSelectPresenter.getSelected() == null) {
                        accountSelectPresenter.startWith(currentAccount.getId(), result);
                        updateExchange(result);
                    }
                }
            });
        }
    }

    @Override
    public void debitUpdated() {
        exchangePresenter.updateOriginalAmount(DebitCreditHolder.Accessor.getDebitOrCredit(view));
        bus.fireEvent(new EntryEditAmountUpdated());
    }

    @Override
    public void creditUpdated() {
        exchangePresenter.updateOriginalAmount(DebitCreditHolder.Accessor.getDebitOrCredit(view));
        bus.fireEvent(new EntryEditAmountUpdated());
    }

    @Override
    public void immediateSubmit() {
        bus.fireEvent(new EntryEditSubmit());
    }

    @Override
    public void switchToOpposite() {
        Account selectedAccount = accountSelectPresenter.getSelected();
        if (selectedAccount == null) return;
        if (split == null || split.getId() == null || split.getEntry() == null || split.getEntry().getId() == null) return;
        placeController.goTo(
                new LedgerPlace(
                        new LedgerCoordinates(
                                selectedAccount.getId(),
                                split.getEntry().getId(),
                                split.getId()
                        )
                )
        );
    }

    @Override
    public List<ValidationError> flushView(int index) {
        List<ValidationError> errors = new ArrayList();

        if (split.getId() == null)
            split.setEnteredOn(new Date());

        if (view.getDescription() == null || view.getDescription().length() == 0) {
            errors.add(new ValidationError(
                    Integer.toString(index),
                    Split.class.getName(),
                    Split.Property.description,
                    "Description is required."
            ));
        } else {
            split.setDescription(view.getDescription());
        }

        Account selectedAccount = accountSelectPresenter.getSelected();
        if (selectedAccount == null) {
            errors.add(new ValidationError(
                    Integer.toString(index),
                    Split.class.getName(),
                    Split.Property.accountId,
                    "Doble-entry ledger requires two accounts for each entry."
            ));
            return errors; // Early exit, need to correct these errors first
        } else {
            if (editEntry) {
                split.getEntry().setAccountId(currentAccount.getId());
                split.setAccountId(selectedAccount.getId());
            } else {
                split.getEntry().setAccountId(selectedAccount.getId());
                split.setAccountId(currentAccount.getId());
            }
        }

        MonetaryAmount enteredAmount = DebitCreditHolder.Accessor.getDebitOrCredit(view);
        if (enteredAmount == null || enteredAmount.signum() == 0) {
            errors.add(new ValidationError(
                    Integer.toString(index),
                    Split.class.getName(),
                    Split.Property.amount,
                    "Debit or credit amount is required."
            ));
            return errors; // Early exit, need to correct these errors first
        }

        MonetaryAmount otherAmount;
        if (isCurrencyExchangeRequired(selectedAccount)) {

            // Get the entered exchanged amount, ignoring the rate
            otherAmount = exchangePresenter.getExchangedAmount();

            if (otherAmount == null || otherAmount.signum() == 0) {
                errors.add(new ValidationError(
                        Integer.toString(index),
                        Split.class.getName(),
                        Split.Property.amount,
                        "Exchanged amount is required."
                ));
                return errors;
            }

            // If the entered amount is a credit, the other amount has to be a debit
            if (enteredAmount.signum() > 0 && otherAmount.signum() > 0) {
                otherAmount = otherAmount.negate();
            }

        } else {
            // No exchange required, just balance the entered amount
            otherAmount = enteredAmount.negate();
        }

        // Flip it if necessary (we are not looking at the account the entry belongs to)
        if (editEntry) {
            split.setEntryAmount(enteredAmount);
            split.setAmount(otherAmount);
        } else {
            split.setEntryAmount(otherAmount);
            split.setAmount(enteredAmount);
        }

        return errors;
    }

    @Override
    public void clearValidationErrors() {
        view.clearValidationErrorDescription();
        view.clearValidationErrorAmount();
        view.clearValidationErrorAccount();
        exchangePresenter.clearValidationErrors();
    }

    @Override
    public void showValidationErrors(List<ValidationError> errors) {
        List<ValidationError> exchangeErrors = new ArrayList();

        StringBuilder sb = new StringBuilder();
        for (ValidationError error : errors) {
            if (Split.Property.description.equals(error.getProperty())) {
                view.showValidationErrorDescription(error);
                sb.append(error.getMessage()).append(" ");
            } else if (Split.Property.amount.equals(error.getProperty())) {
                view.showValidationErrorAmount(error);
                sb.append(error.getMessage()).append(" ");
                exchangeErrors.add(error);
            } else if (Split.Property.accountId.equals(error.getProperty())) {
                view.showValidationErrorAccount(error);
                sb.append(error.getMessage()).append(" ");
            } else {
                bus.fireEvent(new ValidationErrorNotifyEvent(error));
            }
        }

        exchangePresenter.showValidationErrors(exchangeErrors);

        if (sb.length() > 0) {
            bus.fireEvent(new NotifyEvent(
                    new Message(
                            Level.WARNING,
                            "Invalid entry and/or split data", sb.toString()
                    )
            ));
        }
    }

    protected void initViewDebitCredit() {
        if (editEntry) {
            DebitCreditHolder.Accessor.setDebitOrCredit(view, split.getEntryAmount());
        } else {
            DebitCreditHolder.Accessor.setDebitOrCredit(view, split.getAmount());
        }
    }

    protected boolean isCurrencyExchangeRequired(Account targetAccount) {
        return !targetAccount.getMonetaryUnitId().equals(currentAccount.getMonetaryUnitId());
    }

    protected void updateExchange(Account targetAccount) {
        if (targetAccount != null && isCurrencyExchangeRequired(targetAccount)) {

            Date forDay = effectiveDate != null ? effectiveDate : new Date();
            MonetaryUnit fromUnit = currentAccount.getMonetaryUnit();
            MonetaryUnit toUnit = targetAccount.getMonetaryUnit();
            MonetaryAmount originalAmount = DebitCreditHolder.Accessor.getDebitOrCredit(view);

            // Update the split's internal currencies and show the exchange form
            if (editEntry) {
                split.setEntryMonetaryUnit(currentAccount.getMonetaryUnit());
                split.setMonetaryUnit(targetAccount.getMonetaryUnit());

                exchangePresenter.startWith(
                        forDay,
                        fromUnit,
                        toUnit,
                        originalAmount,
                        split.getAmount()
                );

            } else {
                split.setEntryMonetaryUnit(targetAccount.getMonetaryUnit());
                split.setMonetaryUnit(currentAccount.getMonetaryUnit());

                exchangePresenter.startWith(
                        forDay,
                        currentAccount.getMonetaryUnit(),
                        targetAccount.getMonetaryUnit(),
                        originalAmount,
                        split.getEntryAmount()
                );
            }

            view.showExchangeView(true);
        } else {
            view.showExchangeView(false);
        }
    }

}
