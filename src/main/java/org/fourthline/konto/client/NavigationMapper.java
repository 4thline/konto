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

package org.fourthline.konto.client;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import javax.inject.Inject;
import com.google.inject.Provider;
import org.fourthline.konto.client.currency.CurrencyActivity;
import org.fourthline.konto.client.currency.CurrencyPlace;
import org.fourthline.konto.client.dashboard.DashboardActivity;
import org.fourthline.konto.client.dashboard.DashboardPlace;
import org.fourthline.konto.client.ledger.LedgerActivity;
import org.fourthline.konto.client.ledger.LedgerPlace;
import org.fourthline.konto.client.ledger.account.AccountActivity;
import org.fourthline.konto.client.ledger.account.AccountPlace;
import org.fourthline.konto.client.main.MainPresenter;
import org.seamless.gwt.notify.client.Notifications;
import org.seamless.gwt.notify.client.NotifyEvent;
import org.fourthline.konto.client.report.ReportActivity;
import org.fourthline.konto.client.report.ReportPlace;
import org.fourthline.konto.client.settings.SettingsActivity;
import org.fourthline.konto.client.settings.SettingsPlace;

/**
 * These are two mapping layers of the MVP navigation system in GWT 2.1.
 * <p>
 * Yes, this is shockingly bad. I understand that strings
 * suck and you want to avoid string-keyed maps. I also understand
 * the limitations of no reflection and GWT rebinding. What I don't
 * understand is how you can't see that you are on the wrong track
 * if your design results in names like
 * "ActivityFactoryMapperFactoryManagerProvider". Hired any Spring
 * guys recently? You need to ask yourself why you are doing all
 * of this. Nobody believes you are trying to improve testability
 * here, the bugs you introduce with these layers of junk will
 * never be tested.
 * </p>
 * <p>
 * The problem you are trying to solve is really simple: The first
 * mapping layer has to map 1:1 between a URL fragment and a Place
 * instance. The second layer maps Place n:1 Activity. Now, even
 * if you want to expose URL rewriters, URL manipulators,
 * and other APIs that allow more fine-grained control of these
 * mapping procedures, I doubt it has to be as ugly as it is now.
 * </p>
 *
 * @author Christian Bauer
 */
public class NavigationMapper implements ActivityMapper {

    @WithTokenizers(
            {
                    DashboardPlace.Tokenizer.class,
                    LedgerPlace.Tokenizer.class,
                    AccountPlace.Tokenizer.class,
                    CurrencyPlace.Tokenizer.class,
                    ReportPlace.Tokenizer.class,
                    SettingsPlace.Tokenizer.class
            }
    )
    static public interface History extends PlaceHistoryMapper {
    }

    final MainPresenter mainPresenter;
    final Provider<DashboardActivity> dashboardActivityProvider;
    final Provider<LedgerActivity> ledgerActivityProvider;
    final Provider<AccountActivity> accountActivityProvider;
    final Provider<CurrencyActivity> currencyActivityProvider;
    final Provider<ReportActivity> reportActivityProvider;
    final Provider<SettingsActivity> settingsActivityProvider;

    @Inject
    public NavigationMapper(MainPresenter mainPresenter,
                            Provider<DashboardActivity> dashboardActivityProvider,
                            Provider<LedgerActivity> ledgerActivityProvider,
                            Provider<AccountActivity> accountActivityProvider,
                            Provider<CurrencyActivity> currencyActivityProvider,
                            Provider<ReportActivity> reportActivityProvider,
                            Provider<SettingsActivity> settingsActivityProvider,
                            EventBus bus,
                            Notifications notifications) {
        super();
        this.mainPresenter = mainPresenter;
        this.dashboardActivityProvider = dashboardActivityProvider;
        this.ledgerActivityProvider = ledgerActivityProvider;
        this.accountActivityProvider = accountActivityProvider;
        this.currencyActivityProvider = currencyActivityProvider;
        this.reportActivityProvider = reportActivityProvider;
        this.settingsActivityProvider = settingsActivityProvider;

        bus.addHandler(NotifyEvent.TYPE, notifications);
    }

    @Override
    public Activity getActivity(Place place) {

        if (place instanceof DashboardPlace) {
            return dashboardActivityProvider.get().init((DashboardPlace) place);
        } else if (place instanceof LedgerPlace) {
            return ledgerActivityProvider.get().init((LedgerPlace) place);
        } else if (place instanceof AccountPlace) {
            return accountActivityProvider.get().init((AccountPlace) place);
        } else if (place instanceof CurrencyPlace) {
            return currencyActivityProvider.get().init((CurrencyPlace) place);
        } else if (place instanceof ReportPlace) {
            return reportActivityProvider.get().init((ReportPlace) place);
        } else if (place instanceof SettingsPlace) {
            return settingsActivityProvider.get().init((SettingsPlace) place);
        }

        return null;
    }


}
