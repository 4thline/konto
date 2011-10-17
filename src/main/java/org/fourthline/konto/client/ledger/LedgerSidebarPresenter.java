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

package org.fourthline.konto.client.ledger;

import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import org.seamless.gwt.component.client.AbstractEventListeningPresenter;
import org.fourthline.konto.client.ledger.component.AccountTreeSelectPresenter;
import org.fourthline.konto.client.ledger.component.AccountTreeSelectView;
import org.fourthline.konto.client.ledger.event.AccountSelectionModeChange;
import org.fourthline.konto.client.ledger.view.LedgerSidebarView;
import org.fourthline.konto.client.service.LedgerServiceAsync;
import org.fourthline.konto.client.settings.GlobalSettings;
import org.fourthline.konto.client.settings.event.GlobalSettingsRefreshedEvent;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.query.AccountsQueryCriteria;

import javax.inject.Inject;

/**
 * Delegates dynamically to {@link AccountTreeSelectPresenter} when switching modes.
 *
 * @author Christian Bauer
 */
public class LedgerSidebarPresenter
        extends AbstractEventListeningPresenter
        implements
        LedgerSidebarView.Presenter,
        GlobalSettingsRefreshedEvent.Handler,
        AccountSelectionModeChange.Handler {

    final LedgerSidebarView view;
    final PlaceController placeController;
    final EventBus bus;
    final LedgerServiceAsync service;

    GlobalSettings globalSettings;

    AccountTreeSelectPresenter delegate;
    boolean delegateDefaultState = false;

    @Inject
    public LedgerSidebarPresenter(LedgerSidebarView view,
                                  PlaceController placeController,
                                  EventBus bus,
                                  LedgerServiceAsync service,
                                  GlobalSettings globalSettings) {
        this.view = view;
        this.placeController = placeController;
        this.bus = bus;
        this.service = service;

        addRegistration(bus.addHandler(GlobalSettingsRefreshedEvent.TYPE, this));
        addRegistration(bus.addHandler(AccountSelectionModeChange.TYPE, this));

        resetDelegate();
        delegate.onRefresh();

        onSettingsRefreshed(globalSettings);
    }

    @Override
    public void onSettingsRefreshed(GlobalSettings gs) {
        this.globalSettings = gs;
        //view.setDateFormat(gs.getValue(GlobalOption.OPT_DATE_FORMAT));
    }

    @Override
    public void stop() {
        super.stop();
        if (delegate != null)
            delegate.stop();
    }

    @Override
    public void onNewAccount() {
        if (delegate != null)
            delegate.onNewAccount();
    }

    @Override
    public void onFilter(String filter) {
        if (delegate != null)
            delegate.onFilter(filter);
    }

    @Override
    public void onSingleSelectionChange(Account selectedAccount) {
        if (delegate != null)
            delegate.onSingleSelectionChange(selectedAccount);
    }

    @Override
    public void onMultiSelectionChange(AccountsQueryCriteria[] selectedAccounts) {
        if (delegate != null)
            delegate.onMultiSelectionChange(selectedAccounts);
    }

    @Override
    public void onSelectionModeChange(AccountSelectionModeChange event) {
        // If the event has null options, we need to reset the delegate, unless it's already in
        // that state, then we avoid the refresh roundtrip
        if (event.getOptions() == null) {
            if (delegateDefaultState) {
                return;
            } else {
                resetDelegate();
                delegate.onRefresh();
                return;
            }
        }

        // Are the options the same?
        boolean currentModeMatches =
                AccountTreeSelectView.Option.equals(
                        delegate.getOptions(),
                        event.getOptions()
                );

        if (!currentModeMatches) {
            delegate.stop();
            delegate =
                    new AccountTreeSelectPresenter(
                            view, placeController, bus, service,
                            event.getOptions(),
                            event.getPreselectedAccounts()
                    );
            delegateDefaultState = false;
            delegate.onRefresh();
        }
    }

    protected void resetDelegate() {
        if (delegate != null)
            delegate.stop();
        delegate = new AccountTreeSelectPresenter(
                view, placeController, bus, service,
                new AccountTreeSelectView.Option[]{
                        AccountTreeSelectView.Option.NEW_BUTTON,
                        AccountTreeSelectView.Option.LABEL_FILTER
                }
        );
        delegateDefaultState = true;
    }

}

