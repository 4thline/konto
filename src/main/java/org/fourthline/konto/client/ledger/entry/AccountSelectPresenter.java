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
import org.seamless.gwt.component.client.suggest.SuggestionSelectPresenter;
import org.seamless.gwt.component.client.suggest.SuggestionSelectView;
import org.fourthline.konto.client.service.LedgerServiceAsync;
import org.seamless.gwt.notify.client.ServerFailureNotifyEvent;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.query.AccountsQueryCriteria;

import java.util.List;


/**
 * @author Christian Bauer
 */
public class AccountSelectPresenter extends SuggestionSelectPresenter<AccountSuggestion> {

    final EventBus bus;
    final LedgerServiceAsync service;

    Account initialAccount;
    Long ignoreAccountId;
    Account account;

    public AccountSelectPresenter(SuggestionSelectView view, EventBus bus, LedgerServiceAsync service) {
        super(view);
        this.bus = bus;
        this.service = service;
    }

    public void startWith(Long ignoreAccountId, Account account) {
        this.ignoreAccountId = ignoreAccountId;
        this.account = account;
        this.initialAccount = account;
        super.startWith(account != null ? account.getName() : null);
    }

    public Account getSelected() {
        return account;
    }

    @Override
    public void nameEntered(final String name) {
        if (account != null && account.getName().equals(name)) return;
        executeQuery(name);
    }

    @Override
    public void deselect() {
        onAccountDeselect();
    }

    @Override
    public void reset() {
        if (initialAccount != null) {
            account = initialAccount;
            getView().setName(account != null ? account.getName() : null);
            onAccountSelection(account);
        }
    }

    @Override
    public void onSelection(AccountSuggestion selected) {
        if (selected.getAccount() != null) {
            onAccountSelection(selected.getAccount());
        }
    }

    protected void onAccountDeselect() {
        this.account = null;
    }

    protected void onAccountSelection(Account selected) {
        this.account = selected;
        getView().setName(account.getName());
    }

    protected void executeQuery(String accountName) {
        AccountsQueryCriteria crit = new AccountsQueryCriteria(
                Account.Property.groupName,
                true,
                accountName,
                true,
                null,
                null,
                null
        );
        service.getAccounts(
                crit,
                new SuggestionCallback<Account>() {

                    @Override
                    protected void handleFailure(Throwable caught) {
                        bus.fireEvent(new ServerFailureNotifyEvent(caught));
                    }

                    @Override
                    protected AccountSuggestion createSuggestion(int index, Account result) {
                        if (result.getId().equals(ignoreAccountId)) return null;
                        return new AccountSuggestion(result);
                    }

                    @Override
                    protected int getSelectedIndex(List<AccountSuggestion> suggestions) {
                        if (account == null) return super.getSelectedIndex(suggestions);
                        for (int i = 0; i < suggestions.size(); i++) {
                            AccountSuggestion suggestion = suggestions.get(i);
                            if (suggestion.getAccount().getId().equals(account.getId())) return i;
                        }
                        return super.getSelectedIndex(suggestions);
                    }
                });
    }

}
