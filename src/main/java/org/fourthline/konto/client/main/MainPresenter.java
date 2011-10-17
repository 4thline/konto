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

package org.fourthline.konto.client.main;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import org.fourthline.konto.client.main.view.MainView;
import org.fourthline.konto.client.settings.GlobalSettings;
import org.fourthline.konto.client.settings.event.GlobalSettingsRefreshedEvent;
import org.fourthline.konto.shared.entity.settings.GlobalOption;

import javax.inject.Inject;

/**
 * @author Christian Bauer
 */
public class MainPresenter implements MainView.Presenter, GlobalSettingsRefreshedEvent.Handler {

    final MainView view;
    final EventBus bus;
    final PlaceController placeController;
    final GlobalSettings globalSettings;

    // Injecting the GlobalSettings here loads the settings for the first time when the app starts!
    @Inject
    public MainPresenter(MainView view,
                         PlaceController placeController,
                         EventBus bus,
                         GlobalSettings globalSettings) {
        this.view = view;
        this.placeController = placeController;
        this.bus = bus;
        this.globalSettings = globalSettings;

        view.setPresenter(this);

        bus.addHandler(GlobalSettingsRefreshedEvent.TYPE, this);
    }

    @Override
    public MainView getView() {
        return view;
    }

    @Override
    public void sidebarResized(int width) {
        globalSettings.storeBackground(
                new GlobalOption<Integer>(GlobalOption.OPT_SIDEBAR_WIDTH, width),
                500
        );
    }

    public void goTo(Place place) {
        placeController.goTo(place);
    }

    @Override
    public void onSettingsRefreshed(GlobalSettings gs) {
        Integer sidebarWidth = gs.getValue(GlobalOption.OPT_SIDEBAR_WIDTH);
        if (sidebarWidth != null)
            view.setSidebarSize(sidebarWidth);
    }
}
