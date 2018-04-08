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

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.web.bindery.event.shared.EventBus;
import org.fourthline.konto.client.bundle.Bundle;
import org.fourthline.konto.client.chart.ChartModule;
import org.fourthline.konto.client.currency.CurrencyModule;
import org.fourthline.konto.client.dashboard.DashboardModule;
import org.fourthline.konto.client.ledger.LedgerModule;
import org.fourthline.konto.client.main.MainModule;
import org.fourthline.konto.client.main.MainPresenter;
import org.fourthline.konto.client.report.ReportModule;
import org.fourthline.konto.client.settings.SettingsModule;
import org.seamless.gwt.notify.client.NotifyModule;

@GinModules(
    {
        NotifyModule.class,

        MainModule.class,
        SettingsModule.class,
        DashboardModule.class,
        CurrencyModule.class,
        LedgerModule.class,
        ReportModule.class,
        ChartModule.class
    }
)
public interface KontoGinjector extends Ginjector {

    Bundle getBundle();

    PlaceHistoryHandler getPlaceHistoryHandler();

    MainPresenter getMainPresenter();

    EventBus getEventBus();
}
