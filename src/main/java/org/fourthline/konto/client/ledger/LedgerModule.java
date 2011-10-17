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
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.place.shared.PlaceController;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.fourthline.konto.client.bundle.Bundle;
import org.fourthline.konto.client.ledger.account.AccountActivity;
import org.fourthline.konto.client.ledger.account.BankAccountPresenter;
import org.fourthline.konto.client.ledger.account.view.AccountView;
import org.fourthline.konto.client.ledger.account.view.AccountViewImpl;
import org.fourthline.konto.client.ledger.account.view.BankAccountView;
import org.fourthline.konto.client.ledger.account.view.BankAccountViewImpl;
import org.fourthline.konto.client.ledger.entry.EntryPresenter;
import org.fourthline.konto.client.ledger.entry.EntrySummaryPresenter;
import org.fourthline.konto.client.ledger.entry.SplitPresenter;
import org.fourthline.konto.client.ledger.entry.view.AccountSelectView;
import org.fourthline.konto.client.ledger.entry.view.EntrySummaryView;
import org.fourthline.konto.client.ledger.entry.view.EntrySummaryViewImpl;
import org.fourthline.konto.client.ledger.entry.view.EntryView;
import org.fourthline.konto.client.ledger.entry.view.EntryViewImpl;
import org.fourthline.konto.client.ledger.entry.view.ExchangeView;
import org.fourthline.konto.client.ledger.entry.view.ExchangeViewImpl;
import org.fourthline.konto.client.ledger.entry.view.SplitView;
import org.fourthline.konto.client.ledger.entry.view.SplitViewImpl;
import org.fourthline.konto.client.ledger.component.AccountTreeSelectView;
import org.fourthline.konto.client.ledger.component.AccountTreeSelectViewImpl;
import org.fourthline.konto.client.ledger.view.LedgerSidebarView;
import org.fourthline.konto.client.ledger.view.LedgerSidebarViewImpl;
import org.fourthline.konto.client.ledger.view.LedgerView;
import org.fourthline.konto.client.ledger.view.LedgerViewImpl;
import org.fourthline.konto.client.service.LedgerServiceAsync;
import org.fourthline.konto.client.settings.GlobalSettings;
import org.seamless.gwt.theme.shared.client.ThemeBundle;

/**
 * @author Christian Bauer
 */
public class LedgerModule extends AbstractGinModule {

    @Override
    protected void configure() {

        bind(AccountTreeSelectView.class)
                .to(AccountTreeSelectViewImpl.class);

        bind(LedgerView.class)
                .to(LedgerViewImpl.class)
                .in(Singleton.class);

        bind(LedgerActivity.class);

        bind(EntryView.class)
                .to(EntryViewImpl.class)
                .in(Singleton.class);

        bind(EntryView.Presenter.class)
                .to(EntryPresenter.class);

        bind(EntrySummaryView.class)
                .to(EntrySummaryViewImpl.class)
                .in(Singleton.class);

        bind(EntrySummaryView.Presenter.class)
                .to(EntrySummaryPresenter.class);

        bind(SplitView.class)
                .to(SplitViewImpl.class);
        // NOT A SINGLETON!

        bind(AccountSelectView.class);
        // NOT A SINGLETON!

        bind(SplitView.Presenter.class)
                .to(SplitPresenter.class);

        bind(ExchangeView.class)
                .to(ExchangeViewImpl.class);
        // NOT A SINGLETON!

        bind(AccountView.class)
                .to(AccountViewImpl.class)
                .in(Singleton.class);

        bind(AccountActivity.class);

        bind(BankAccountView.class)
                .to(BankAccountViewImpl.class);
        // NOT A SINGLETON!

        bind(BankAccountPresenter.class);

    }

    // Eternal view & presenter have to be started right away

    @Provides
    @Singleton
    public LedgerSidebarView createLedgerSidebarView(Bundle bundle,
                                                     PlaceController placeController,
                                                     EventBus bus,
                                                     LedgerServiceAsync service,
                                                     GlobalSettings globalSettings) {
        LedgerSidebarView view = new LedgerSidebarViewImpl(bundle);
        LedgerSidebarView.Presenter presenter =
                new LedgerSidebarPresenter(view, placeController, bus, service, globalSettings);
        view.setPresenter(presenter);
        return view;
    }

}
