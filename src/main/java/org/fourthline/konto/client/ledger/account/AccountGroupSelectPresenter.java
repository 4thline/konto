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

import com.google.web.bindery.event.shared.EventBus;
import org.seamless.gwt.component.client.suggest.SuggestionSelectPresenter;
import org.seamless.gwt.component.client.suggest.SuggestionSelectView;
import org.fourthline.konto.client.service.LedgerServiceAsync;
import org.seamless.gwt.notify.client.ServerFailureNotifyEvent;
import org.fourthline.konto.shared.AccountType;
import org.fourthline.konto.shared.entity.AccountGroup;


/**
 * @author Christian Bauer
 */
public class AccountGroupSelectPresenter extends SuggestionSelectPresenter<AccountGroupSuggestion> {

    final EventBus bus;
    final LedgerServiceAsync service;

    String name;
    AccountType type;
    String initalName;

    public AccountGroupSelectPresenter(SuggestionSelectView view, EventBus bus, LedgerServiceAsync service) {
        super(view);
        this.bus = bus;
        this.service = service;
    }

    public void startWith(AccountType type, String groupName) {
        this.type = type;
        this.name = groupName;
        this.initalName = groupName;
        super.startWith(null);
    }

    public void setAccountType(AccountType type) {
        this.type = type;
    }

    public AccountType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public void nameEntered(final String name) {
        if (this.name != null && this.name.equals(name)) return;
        if (name != null && name.equals(initalName)) return;
        this.name = name;
        executeQuery(name);
    }

    @Override
    public void deselect() {
        onAccountGroupDeselect();
    }

    @Override
    public void reset() {
        getView().setName(initalName);
        onAccountGroupDeselect();
    }

    @Override
    public void onSelection(AccountGroupSuggestion selected) {
        onAccountGroupSelection(selected.getAccountGroup());
    }

    protected void onAccountGroupDeselect() {
        this.name = null;
    }

    protected void onAccountGroupSelection(AccountGroup selected) {
        this.name = selected.getName();
        getView().setName(name);
    }

    protected void executeQuery(String groupName) {
        service.getAccountGroups(type, groupName, new SuggestionCallback<AccountGroup>() {
            @Override
            protected void handleFailure(Throwable caught) {
                bus.fireEvent(new ServerFailureNotifyEvent(caught));
            }

            @Override
            protected AccountGroupSuggestion createSuggestion(int index, AccountGroup result) {
                return new AccountGroupSuggestion(result);
            }
        });
    }

}
