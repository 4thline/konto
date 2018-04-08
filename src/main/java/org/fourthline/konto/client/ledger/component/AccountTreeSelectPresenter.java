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

package org.fourthline.konto.client.ledger.component;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import org.fourthline.konto.client.ledger.account.AccountPlace;
import org.fourthline.konto.client.ledger.account.event.AccountModified;
import org.fourthline.konto.client.ledger.account.event.AccountRemoved;
import org.fourthline.konto.client.ledger.event.MultipleAccountsSelected;
import org.fourthline.konto.client.ledger.event.SingleAccountSelected;
import org.fourthline.konto.client.service.LedgerServiceAsync;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.query.AccountsQueryCriteria;
import org.seamless.gwt.component.client.AbstractEventListeningPresenter;
import org.seamless.gwt.notify.client.ServerFailureNotifyEvent;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class AccountTreeSelectPresenter
        extends AbstractEventListeningPresenter
        implements
        AccountTreeSelectView.Presenter,
        AccountModified.Handler,
        AccountRemoved.Handler {

    final AccountTreeSelectView view;
    final PlaceController placeController;
    final EventBus bus;
    final LedgerServiceAsync service;

    AccountTreeSelectView.Option[] options;
    AccountsQueryCriteria[] preselectedAccounts;
    String accountFilter;

    @Inject
    public AccountTreeSelectPresenter(AccountTreeSelectView view,
                                      PlaceController placeController,
                                      EventBus bus,
                                      LedgerServiceAsync service) {
        this(view, placeController, bus, service, null, null);
    }


    public AccountTreeSelectPresenter(AccountTreeSelectView view,
                                      PlaceController placeController,
                                      EventBus bus,
                                      LedgerServiceAsync service,
                                      AccountTreeSelectView.Option[] options) {
        this(view, placeController, bus, service, options, null);
    }

    public AccountTreeSelectPresenter(AccountTreeSelectView view,
                                      PlaceController placeController,
                                      EventBus bus,
                                      LedgerServiceAsync service,
                                      AccountTreeSelectView.Option[] options,
                                      AccountsQueryCriteria[]  preselectedAccounts) {
        this.view = view;
        this.placeController = placeController;
        this.bus = bus;
        this.service = service;

        view.setPresenter(this);

        this.options = options;
        this.preselectedAccounts = preselectedAccounts;

        addRegistration(bus.addHandler(AccountModified.TYPE, this));
        addRegistration(bus.addHandler(AccountRemoved.TYPE, this));
    }

    @Override
    public void onNewAccount() {
        placeController.goTo(new AccountPlace());
    }

    @Override
    public void onFilter(String filter) {
        if ((this.accountFilter == null && filter != null) ||
                (this.accountFilter != null && filter == null) ||
                (this.accountFilter != null && !this.accountFilter.equals(filter))) {
            this.accountFilter = filter;
            onRefresh();
        } else {
            this.accountFilter = filter;
        }
    }

    @Override
    public void onSingleSelectionChange(Account selectedAccount) {
        bus.fireEvent(new SingleAccountSelected(selectedAccount));
    }

    @Override
    public void onMultiSelectionChange(AccountsQueryCriteria[] selection) {
        bus.fireEvent(new MultipleAccountsSelected(selection));
    }

    @Override
    public void onAccountModified(AccountModified event) {
        onRefresh();
    }

    @Override
    public void onAccountRemoved(AccountRemoved event) {
        onRefresh();
    }

    public AccountTreeSelectView.Option[] getOptions() {
        return options;
    }

    public void onRefresh() {
        AccountsQueryCriteria crit = new AccountsQueryCriteria();

        crit.setOrderBy(Account.Property.groupName);
        crit.setSortAscending(true);

        if (accountFilter != null) {
            crit.setStringFilter(accountFilter);
            crit.setSubstringQuery(true);
        }

        service.getAccounts(crit, new AsyncCallback<List<Account>>() {
            @Override
            public void onFailure(Throwable caught) {
                bus.fireEvent(new ServerFailureNotifyEvent(caught));
            }

            @Override
            public void onSuccess(List<Account> result) {
                view.setAccounts(result, getOptions());
                if (preselectedAccounts != null)
                    view.setSelectedAccounts(preselectedAccounts);
            }
        });
    }
}
