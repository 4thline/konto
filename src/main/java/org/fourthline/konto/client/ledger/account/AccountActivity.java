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

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Provider;
import org.fourthline.konto.client.currency.CurrencyPlace;
import org.fourthline.konto.client.dashboard.DashboardPlace;
import org.fourthline.konto.client.ledger.LedgerPlace;
import org.fourthline.konto.client.ledger.account.event.AccountModified;
import org.fourthline.konto.client.ledger.account.event.AccountRemoved;
import org.fourthline.konto.client.ledger.account.view.AccountView;
import org.fourthline.konto.client.ledger.event.AccountSelectionModeChange;
import org.fourthline.konto.client.ledger.event.SingleAccountSelected;
import org.seamless.gwt.notify.client.Message;
import org.seamless.gwt.notify.client.ServerFailureNotifyEvent;
import org.seamless.gwt.notify.client.NotifyEvent;
import org.seamless.gwt.notify.client.ValidationErrorNotifyEvent;
import org.fourthline.konto.client.service.CurrencyServiceAsync;
import org.fourthline.konto.client.service.LedgerServiceAsync;
import org.fourthline.konto.client.settings.GlobalSettings;
import org.fourthline.konto.client.settings.event.GlobalSettingsRefreshedEvent;
import org.fourthline.konto.shared.AccountType;
import org.fourthline.konto.shared.Constants;
import org.fourthline.konto.shared.LedgerCoordinates;
import org.fourthline.konto.shared.MonetaryAmount;
import org.seamless.gwt.validation.shared.Validatable;
import org.seamless.gwt.validation.shared.ValidationError;
import org.seamless.gwt.validation.shared.ValidationException;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.fourthline.konto.shared.entity.settings.GlobalOption;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Christian Bauer
 */
public class AccountActivity
    extends AbstractActivity
    implements
    AccountView.Presenter,
    SingleAccountSelected.Handler,
    GlobalSettingsRefreshedEvent.Handler {

    class NewAccountCallback implements AsyncCallback<List<MonetaryUnit>> {
        @Override
        public void onFailure(Throwable caught) {
            bus.fireEvent(new ServerFailureNotifyEvent(caught));
        }

        @Override
        public void onSuccess(List<MonetaryUnit> result) {
            currencies = result;

            if (currencies.size() == 0) {

                bus.fireEvent(new NotifyEvent(
                    new Message(
                        Level.INFO,
                        "Can't create account",
                        "Create a currency first."
                    )
                ));
                placeController.goTo(new CurrencyPlace());
                return;
            }

            type = AccountType.values()[0]; // Default to first type
            unit = currencies.get(0); // Default to first currency

            accountGroupSelectPresenter.startWith(type, null);

            view.setCurrencies(result);
            view.setCurrency(Constants.SYSTEM_BASE_CURRENCY_CODE);
            view.getEffectiveOnProperty().set(new Date());
            view.getInitialBalanceProperty().set(new BigDecimal("0"));

            view.setCreateMode(true);
            view.focus();
        }
    }

    class EditAccountCallback implements AsyncCallback<Account> {
        @Override
        public void onFailure(Throwable caught) {
            bus.fireEvent(new ServerFailureNotifyEvent(caught));
        }

        @Override
        public void onSuccess(Account result) {
            initialAccount = result;
            type = initialAccount.getType();
            unit = initialAccount.getMonetaryUnit();
            accountGroupId = initialAccount.getGroupId();
            accountGroupSelectPresenter.startWith(initialAccount.getType(), initialAccount.getGroupName());

            view.getNameProperty().set(initialAccount.getName());
            view.getAccountGroupSelectView().setName(initialAccount.getGroupName());
            view.getEffectiveOnProperty().set(initialAccount.getEffectiveOn());
            // TODO view.setCurrency(initialAccount.getCurrencyCode());
            view.getInitialBalanceProperty().set(initialAccount.getInitialBalance().getValue());

            switchSubAccountPresenter(initialAccount.getType());
            if (subAccountPresenter != null) {
                subAccountPresenter.startWith(initialAccount);
            }

            view.setCreateMode(false);
            view.focus();
        }
    }

    final AccountView view;
    final AccountGroupSelectPresenter accountGroupSelectPresenter;

    final PlaceController placeController;
    final EventBus bus;
    final LedgerServiceAsync ledgerService;
    final CurrencyServiceAsync currencyService;

    final Provider<BankAccountPresenter> bankAccountPresenterProvider;
    SubAccountPresenter subAccountPresenter;

    Long accountId;
    Account initialAccount;
    List<MonetaryUnit> currencies;

    Account account;
    AccountType type;
    Long accountGroupId;
    MonetaryUnit unit;

    @Inject
    public AccountActivity(AccountView view,
                           Provider<BankAccountPresenter> bankAccountPresenterProvider,
                           PlaceController placeController,
                           EventBus bus,
                           LedgerServiceAsync ledgerService,
                           CurrencyServiceAsync currencyService,
                           GlobalSettings globalSettings) {
        this.view = view;
        this.bankAccountPresenterProvider = bankAccountPresenterProvider;
        this.placeController = placeController;
        this.bus = bus;
        this.ledgerService = ledgerService;
        this.currencyService = currencyService;

        accountGroupSelectPresenter =
            new AccountGroupSelectPresenter(view.getAccountGroupSelectView(), bus, ledgerService);

        onSettingsRefreshed(globalSettings);
    }

    public Account getAccount() {
        return account;
    }

    public AccountActivity init(AccountPlace place) {
        this.accountId = place.getAccountId();
        return this;
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, com.google.gwt.event.shared.EventBus activityBus) {
        view.reset();
        view.setPresenter(this);

        containerWidget.setWidget(view.asWidget());

        activityBus.addHandler(SingleAccountSelected.TYPE, this);
        activityBus.addHandler(GlobalSettingsRefreshedEvent.TYPE, this);

        bus.fireEvent(new AccountSelectionModeChange());

        if (accountId != null) {
            ledgerService.getAccount(accountId, new EditAccountCallback());
        } else {
            currencyService.getMonetaryUnits(new NewAccountCallback());
        }
    }

    @Override
    public void onSettingsRefreshed(GlobalSettings gs) {
        view.setDateFormat(gs.getValue(GlobalOption.OPT_DATE_FORMAT));
    }

    @Override
    public void typeSelected(AccountType type) {
        this.type = type;
        accountGroupSelectPresenter.setAccountType(type);
        switchSubAccountPresenter(type);
    }

    @Override
    public void currencySelected(int index) {
        unit = currencies.get(index);
        view.setCurrency(unit.getCurrencyCode());
    }

    @Override
    public void save() {
        clearValidationErrors();

        // Client-side validation including view/model data binding
        List<ValidationError> errors = flushView();
        if (errors.size() > 0) {
            bus.fireEvent(new NotifyEvent(
                new Message(Level.WARNING, "Can't save account", "Please correct your input.")
            ));
            showValidationErrors(errors);
            return;
        }

        ledgerService.store(account, new AsyncCallback<Long>() {
            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof ValidationException) {
                    ValidationException ex = (ValidationException) caught;

                    // This is probably a FK violation
                    if (!ex.hasErrors()) {
                        bus.fireEvent(new NotifyEvent(
                            new Message(
                                Level.WARNING,
                                "Can't save account, errors on server",
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
            public void onSuccess(Long result) {
                bus.fireEvent(new NotifyEvent(
                    new Message(
                        Level.INFO,
                        "Account saved",
                        "Modifications have been stored."
                    )
                ));
                placeController.goTo(new LedgerPlace(new LedgerCoordinates(result)));
                bus.fireEvent(new AccountModified(account));
            }
        });
    }

    @Override
    public void delete() {
        ledgerService.remove(initialAccount, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                bus.fireEvent(new ServerFailureNotifyEvent(caught));
            }

            @Override
            public void onSuccess(Void result) {
                bus.fireEvent(new NotifyEvent(
                    new Message(
                        Level.INFO,
                        "Account deleted",
                        "The account has been permanently removed."
                    )
                ));
                placeController.goTo(new DashboardPlace());
                bus.fireEvent(new AccountRemoved(initialAccount));
            }
        });
    }

    @Override
    public void cancel() {
        if (accountId != null) {
            placeController.goTo(new LedgerPlace(new LedgerCoordinates(accountId)));
        } else {
            placeController.goTo(new DashboardPlace());
        }
    }

    @Override
    public void onSingleAccountSelected(SingleAccountSelected event) {
        placeController.goTo(new LedgerPlace(new LedgerCoordinates(event.getSelection().getId())));
    }

    protected void switchSubAccountPresenter(AccountType type) {
        if (subAccountPresenter != null) {
            view.removeFormPanelRow(subAccountPresenter.getSubAccountView());
        }
        switch (type) {
            case BankAccount:
                subAccountPresenter = bankAccountPresenterProvider.get();
                //((SubAccountPresenter<BankAccount>)subAccountPresenter).startWith();
                view.addFormPanelRow(subAccountPresenter.getSubAccountView());
                break;
            default:
                subAccountPresenter = null;
        }
    }

    protected void clearValidationErrors() {
        if (subAccountPresenter != null)
            subAccountPresenter.clearValidationErrors();
        view.getNameProperty().clearValidationError();
        view.clearValidationErrorAccountGroup();
        view.getEffectiveOnProperty().clearValidationError();
        view.getInitialBalanceProperty().clearValidationError();
    }

    protected void showValidationErrors(List<ValidationError> errors) {

        if (subAccountPresenter != null) {
            subAccountPresenter.showValidationErrors(errors);
        }

        List<ValidationError> accountErrors =
            ValidationError.filterEntity(errors, Account.class.getName());

        for (ValidationError error : accountErrors) {
            if (Account.Property.name.equals(error.getProperty()))
                view.getNameProperty().showValidationError(error);
            else if (Account.Property.effectiveOn.equals(error.getProperty()))
                view.getEffectiveOnProperty().showValidationError(error);
            else if (Account.Property.initialBalance.equals(error.getProperty()))
                view.getInitialBalanceProperty().showValidationError(error);
            else if (Account.Property.groupName.equals(error.getProperty()))
                view.showValidationErrorAccountGroup(error);
            else
                errors.add(error);
        }

        for (ValidationError error : errors) {
            bus.fireEvent(new ValidationErrorNotifyEvent(error));
        }
    }

    protected List<ValidationError> flushView() {
        List<ValidationError> errors = new ArrayList();

        account = initialAccount != null ? initialAccount : type.instantiate();

        account.setName(view.getNameProperty().get());
        account.setEffectiveOn(view.getEffectiveOnProperty().get());
        account.setInitialBalance(new MonetaryAmount(unit, view.getInitialBalanceProperty().get()));
        account.setMonetaryUnitId(unit.getId());
        account.setGroupName(accountGroupSelectPresenter.getName());

        if (subAccountPresenter != null) {
            errors.addAll(subAccountPresenter.flush(account));
        }

        errors.addAll(account.validate(Validatable.GROUP_CLIENT));

        return errors;
    }

}

