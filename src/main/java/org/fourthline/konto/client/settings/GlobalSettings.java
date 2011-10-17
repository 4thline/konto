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

import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.seamless.gwt.notify.client.Message;
import org.seamless.gwt.notify.client.ServerFailureNotifyEvent;
import org.seamless.gwt.notify.client.NotifyEvent;
import org.fourthline.konto.client.service.SettingsServiceAsync;
import org.fourthline.konto.client.settings.event.GlobalSettingsRefreshedEvent;
import org.fourthline.konto.shared.entity.settings.GlobalOption;
import org.fourthline.konto.shared.entity.settings.Option;
import org.fourthline.konto.shared.entity.settings.Settings;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Bauer
 */
public class GlobalSettings {

    final private static Logger log = Logger.getLogger(GlobalSettings.class.getName());

    final EventBus bus;
    final SettingsServiceAsync service;

    Settings<GlobalOption> settings;
    Timer timer;


    @Inject
    public GlobalSettings(SettingsServiceAsync service, final EventBus bus) {
        this.service = service;
        this.bus = bus;

        loadSettings();
    }

    protected void loadSettings() {
        log.fine("Loading settings from server");
        service.getGlobalSettings(new AsyncCallback<Settings<GlobalOption>>() {
            @Override
            public void onFailure(Throwable caught) {
                onSettingsLoadFailure(caught);
            }

            @Override
            public void onSuccess(Settings<GlobalOption> result) {
                onSettingsLoaded(result);
            }
        });
    }

    protected void onSettingsLoadFailure(Throwable caught) {
        bus.fireEvent(new ServerFailureNotifyEvent(caught));
    }

    protected void onSettingsLoaded(Settings<GlobalOption> settings) {
        log.fine("Settings loaded, caching and notifying listeners");
        this.settings = settings;
        bus.fireEvent(new GlobalSettingsRefreshedEvent(this));
    }

    public Settings<GlobalOption> getSettings() {
        return settings;
    }

    public <V> Option<V> getOption(Option.Domain<V> domain) {
        return getSettings() != null ? getSettings().getOption(domain) : null;
    }

    public <V> V getValue(Option.Domain<V> domain) {
        return getSettings() != null ? getSettings().getValue(domain) : null;
    }

    // Stores delayed (buffering rapid changes) only if value changed, doesn't
    // announce success, doesn't notify listeners
    public void storeBackground(final Option option, int milliseconds) {

        log.fine("Storing single option in background: " + option);

        // Update the cache
        boolean sameValue = false;
        for (GlobalOption cachedOption : getSettings()) {
            if (cachedOption.getName().equals(option.getName())) {
                if (cachedOption.isEqualValue(option)) {
                    log.finest("New Option value equal to cached value, skipping storage on server");
                    sameValue = true;
                    break;
                } else {
                    log.finest("Updating outdated cached value: " + cachedOption);
                    cachedOption.setValue(option.getValue());
                    break;
                }
            }
        }
        if (sameValue) return;

        log.finest("Option value changed, starting storage timer: " + option);

        if (timer != null) timer.cancel();
        timer = new Timer() {
            public void run() {
                log.fine("Storing option on server after timer completion");
                service.store(
                        new Settings(option),
                        new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                bus.fireEvent(new ServerFailureNotifyEvent(caught));
                            }

                            @Override
                            public void onSuccess(Void result) {
                                log.fine("Successfully stored option on server in background");
                                // Silent
                            }
                        }
                );
            }
        };
        timer.schedule(milliseconds);
    }

    public void store(final Settings settings) {
        log.fine("Store settings: " + settings);
        service.store(
                settings,
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        bus.fireEvent(new ServerFailureNotifyEvent(caught));
                    }

                    @Override
                    public void onSuccess(Void result) {
                        log.fine("Successfully stored settings on server");
                        bus.fireEvent(new NotifyEvent(
                                new Message(
                                        Level.INFO,
                                        "Settings saved",
                                        "Modifications have been stored."
                                )
                        ));
                        // Update cache and notify listeners
                        onSettingsLoaded(settings);
                    }
                }
        );
    }

}
