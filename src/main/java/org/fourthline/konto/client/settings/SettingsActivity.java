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

package org.fourthline.konto.client.settings;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.fourthline.konto.client.ledger.event.AccountSelectionModeChange;
import org.fourthline.konto.client.settings.event.GlobalSettingsRefreshedEvent;
import org.fourthline.konto.client.settings.view.SettingsView;
import org.seamless.util.time.DateFormat;
import org.fourthline.konto.shared.entity.settings.GlobalOption;
import org.fourthline.konto.shared.entity.settings.Settings;

import javax.inject.Inject;

public class SettingsActivity extends AbstractActivity
        implements
        SettingsView.Presenter,
        GlobalSettingsRefreshedEvent.Handler {

    final SettingsView view;
    final PlaceController placeController;
    final EventBus bus;

    GlobalSettings globalSettings;

    @Inject
    public SettingsActivity(SettingsView view,
                            PlaceController placeController,
                            EventBus bus,
                            GlobalSettings globalSettings) {
        this.view = view;
        this.placeController = placeController;
        this.bus = bus;

        onSettingsRefreshed(globalSettings);
    }

    public SettingsActivity init(SettingsPlace place) {
        return this;
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, com.google.gwt.event.shared.EventBus activityBus) {

        view.setPresenter(this);

        containerWidget.setWidget(view.asWidget());

        activityBus.addHandler(GlobalSettingsRefreshedEvent.TYPE, this);

        bus.fireEvent(new AccountSelectionModeChange());
    }

    @Override
    public void onSettingsRefreshed(GlobalSettings gs) {
        this.globalSettings = gs;

        view.getDateFormatProperty().set(gs.getValue(GlobalOption.OPT_DATE_FORMAT));
        view.getNewEntrySelectDayProperty().set(gs.getValue(GlobalOption.OPT_NEW_ENTRY_SELECT_DAY));
        view.getRoundFractionsInReportsProperty().set(gs.getValue(GlobalOption.OPT_ROUND_FRACTIONS_IN_REPORTS));
    }

    @Override
    public void save() {

        Settings settings = new Settings(
                new GlobalOption<DateFormat>(GlobalOption.OPT_DATE_FORMAT, view.getDateFormatProperty().get()),
                new GlobalOption<Boolean>(GlobalOption.OPT_NEW_ENTRY_SELECT_DAY, view.getNewEntrySelectDayProperty().get()),
                new GlobalOption<Boolean>(GlobalOption.OPT_ROUND_FRACTIONS_IN_REPORTS, view.getRoundFractionsInReportsProperty().get())
        );

        globalSettings.store(settings);
    }

}
