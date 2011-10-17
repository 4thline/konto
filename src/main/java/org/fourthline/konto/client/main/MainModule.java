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

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import org.fourthline.konto.client.NavigationMapper;
import org.fourthline.konto.client.bundle.Bundle;
import org.fourthline.konto.client.dashboard.DashboardPlace;
import org.fourthline.konto.client.main.view.MainView;
import org.fourthline.konto.client.main.view.MainViewImpl;
import org.seamless.gwt.notify.client.NotificationDisplay;
import org.seamless.gwt.notify.client.PopupNotificationDisplay;
import org.seamless.gwt.theme.shared.client.ThemeBundle;

/**
 * @author Christian Bauer
 */
public class MainModule extends AbstractGinModule {

    @Override
    protected void configure() {

        bind(EventBus.class)
                .to(SimpleEventBus.class)
                .in(Singleton.class);

        bind(PlaceHistoryMapper.class)
                .to(NavigationMapper.History.class)
                .in(Singleton.class);

        bind(ActivityMapper.class)
                .to(NavigationMapper.class)
                .in(Singleton.class);

        bind(MainView.Presenter.class)
                .to(MainPresenter.class)
                .in(Singleton.class);

        bind(MainView.class)
                .to(MainViewImpl.class)
                .in(Singleton.class);

    }

    @Provides
    @Singleton
    public PlaceController getPlaceController(EventBus eventBus) {
        return new PlaceController(eventBus);
    }

    @Provides
    @Singleton
    public PlaceHistoryHandler getHistoryHandler(PlaceController placeController,
                                                 PlaceHistoryMapper historyMapper,
                                                 EventBus eventBus,
                                                 ActivityManager activityManager) {
        // Yes, the unused ActivityManager argument here is necessary for init order
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        historyHandler.register(placeController, eventBus, new DashboardPlace());
        return historyHandler;
    }


    @Provides
    @Singleton
    public ActivityManager getActivityManager(ActivityMapper mapper,
                                              EventBus eventBus,
                                              MainView mainView) {
        ActivityManager activityManager = new ActivityManager(mapper, eventBus);
        activityManager.setDisplay(mainView.getContentPanel());
        return activityManager;
    }

    @Provides
    @Singleton
    NotificationDisplay getNotificationDisplay(Bundle bundle) {
        return new PopupNotificationDisplay(bundle.themeBundle().create());
    }


}
