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

package org.fourthline.konto.client.dashboard;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.fourthline.konto.client.dashboard.view.DashboardView;
import org.fourthline.konto.client.ledger.event.AccountSelectionModeChange;
import org.seamless.gwt.notify.client.ServerFailureNotifyEvent;
import org.fourthline.konto.client.report.LineReportType;
import org.fourthline.konto.client.service.ReportServiceAsync;
import org.fourthline.konto.client.settings.GlobalSettings;
import org.fourthline.konto.client.settings.event.GlobalSettingsRefreshedEvent;
import org.fourthline.konto.shared.Constants;
import org.seamless.util.time.DateRange;
import org.fourthline.konto.shared.entity.settings.GlobalOption;
import org.fourthline.konto.shared.query.LineReportCriteria;
import org.fourthline.konto.shared.query.LineReportOption;
import org.fourthline.konto.shared.result.ReportLines;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author Christian Bauer
 */
public class DashboardActivity
    extends AbstractActivity
    implements DashboardView.Presenter, GlobalSettingsRefreshedEvent.Handler {

    final DashboardView view;
    final PlaceController placeController;
    final EventBus bus;
    final ReportServiceAsync service;

    @Inject
    public DashboardActivity(DashboardView view,
                             PlaceController placeController,
                             EventBus bus,
                             ReportServiceAsync service,
                             GlobalSettings globalSettings) {
        this.view = view;
        this.placeController = placeController;
        this.bus = bus;
        this.service = service;

        onSettingsRefreshed(globalSettings);
    }

    public DashboardActivity init(DashboardPlace place) {
        return this;
    }

    @Override
    public void start(AcceptsOneWidget container, com.google.gwt.event.shared.EventBus activityBus) {
        view.setPresenter(this);
        container.setWidget(view.asWidget());

        activityBus.addHandler(GlobalSettingsRefreshedEvent.TYPE, this);

        bus.fireEvent(new AccountSelectionModeChange());

        service.getReportLines(
            new LineReportCriteria(
                LineReportType.BS.getDefaultAccountSelection(),
                Constants.SYSTEM_BASE_CURRENCY_CODE,
                new Date(),
                LineReportType.BS,
                new DateRange(null, new Date()),
                new LineReportOption(false, false, true, false)
            ),
            new AsyncCallback<ReportLines[]>() {
                @Override
                public void onFailure(Throwable caught) {
                    bus.fireEvent(new ServerFailureNotifyEvent(caught));
                }

                @Override
                public void onSuccess(ReportLines[] result) {
                    view.setReportLines(
                        result[0],
                        result[1],
                        result[0].getTotal().add(result[1].getTotal())
                    );
                }
            }
        );
    }

    @Override
    public void onSettingsRefreshed(GlobalSettings gs) {
        view.setDateFormat(gs.getValue(GlobalOption.OPT_DATE_FORMAT));
    }

    @Override
    public void goTo(Place place) {
        placeController.goTo(place);
    }
}
