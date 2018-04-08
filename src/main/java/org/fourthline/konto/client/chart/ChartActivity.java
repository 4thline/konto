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

package org.fourthline.konto.client.chart;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import org.fourthline.konto.client.chart.view.ChartResultView;
import org.fourthline.konto.client.chart.view.ChartSelectView;
import org.fourthline.konto.client.chart.view.ChartView;
import org.fourthline.konto.client.ledger.LedgerPlace;
import org.fourthline.konto.client.ledger.component.AccountTreeSelectView;
import org.fourthline.konto.client.ledger.event.AccountSelectionModeChange;
import org.fourthline.konto.client.ledger.event.SingleAccountSelected;
import org.fourthline.konto.client.service.ReportServiceAsync;
import org.fourthline.konto.client.settings.GlobalSettings;
import org.fourthline.konto.client.settings.event.GlobalSettingsRefreshedEvent;
import org.fourthline.konto.shared.LedgerCoordinates;
import org.fourthline.konto.shared.entity.settings.GlobalOption;
import org.fourthline.konto.shared.query.AccountsQueryCriteria;
import org.fourthline.konto.shared.query.ChartCriteria;
import org.fourthline.konto.shared.result.ChartDataPoints;
import org.seamless.gwt.notify.client.Message;
import org.seamless.gwt.notify.client.NotifyEvent;
import org.seamless.gwt.notify.client.ServerFailureNotifyEvent;
import org.seamless.util.time.DateRange;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO: The view/criteria state management is buggy and a mess.
 * Cleanup when graph feature has been written.
 *
 * @author Christian Bauer
 */
public class ChartActivity extends AbstractActivity
    implements
    ChartView.Presenter,
    ChartSelectView.Presenter,
    ChartResultView.Presenter,
    SingleAccountSelected.Handler,
    GlobalSettingsRefreshedEvent.Handler {

    private static final Logger LOG = Logger.getLogger(ChartActivity.class.getName());


    final ChartView view;
    final PlaceController placeController;
    final EventBus bus;
    final ReportServiceAsync reportService;

    GlobalSettings globalSettings;
    ChartCriteria criteria;

    @Inject
    public ChartActivity(ChartView view,
                         PlaceController placeController,
                         EventBus bus,
                         ReportServiceAsync reportService,
                         GlobalSettings globalSettings) {
        this.view = view;
        this.placeController = placeController;
        this.bus = bus;
        this.reportService = reportService;

        onSettingsRefreshed(globalSettings);
    }

    public ChartActivity init(ChartPlace place) {
        criteria = place.getCriteria();
        return this;
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, com.google.gwt.event.shared.EventBus activityBus) {

        if (criteria == null) {
            resetCriteria();
            // Now update the URL bar in the browser
            new Timer() {
                @Override
                public void run() {
                    goTo(new ChartPlace(criteria));
                }
            }.schedule(1); // TODO: Asynchronous, otherwise URL bar doesn't refresh
            return;
        }

        view.setPresenter(this);
        view.getChartSelectView().setPresenter(this);
        view.getChartSelectView().setCriteria(criteria);
        view.getChartResultView().setPresenter(this);

        containerWidget.setWidget(view.asWidget());

        activityBus.addHandler(SingleAccountSelected.TYPE, this);
        activityBus.addHandler(GlobalSettingsRefreshedEvent.TYPE, this);

        // We store the account selection in our view because we have no direct
        // connection with the account select tree and can't query its state
        view.setAccountSelection(criteria.getAccountId());

        // Switch the account tree select widget to whatever is in the criteria
        bus.fireEvent(new AccountSelectionModeChange(
            new AccountTreeSelectView.Option[]{
                AccountTreeSelectView.Option.NEW_BUTTON,
                AccountTreeSelectView.Option.LABEL_FILTER,
                AccountTreeSelectView.Option.SELECT_NONE
            },
            new AccountsQueryCriteria[]{new AccountsQueryCriteria(criteria.getAccountId())}
        ));

        // Execute the query on start, the only way to "refresh" is to go to a new ChartPlace
        reportService.getChartDataPoints(
            criteria,
            new AsyncCallback<ChartDataPoints>() {
                @Override
                public void onFailure(Throwable caught) {
                    bus.fireEvent(new ServerFailureNotifyEvent(caught));
                }

                @Override
                public void onSuccess(ChartDataPoints result) {
                    if (result == null) {
                        bus.fireEvent(new NotifyEvent(new Message(
                            Level.INFO,
                            "Empty chart generated...",
                            "Please select accounts or change settings!")
                        ));
                        return;
                    } else {
                        bus.fireEvent(new NotifyEvent());
                    }

                    view.setHeadline(result.getAccountLabel());
                    view.getChartResultView().setDataPoints(result);
                }
            }
        );
    }

    @Override
    public void onSettingsRefreshed(GlobalSettings gs) {
        this.globalSettings = gs;
        view.getChartSelectView().setDateFormat(gs.getValue(GlobalOption.OPT_DATE_FORMAT));
        view.getChartResultView().setDateFormat(gs.getValue(GlobalOption.OPT_DATE_FORMAT));
        view.getChartResultView().setRoundFractions(gs.getValue(GlobalOption.OPT_ROUND_FRACTIONS_IN_REPORTS));
    }

    @Override
    public void onSingleAccountSelected(SingleAccountSelected event) {
        view.setAccountSelection(event.getSelection().getId()); // Store it in the shared view

        criteria = new ChartCriteria(
            event.getSelection().getId(),
            criteria.getRange(),
            criteria.getGroupOption()
        );
        goTo(new ChartPlace(criteria));
    }

    @Override
    public void onDateRangeUpdated(DateRange dateRange) {
        criteria = new ChartCriteria(
            criteria.getAccountId(),
            dateRange,
            criteria.getGroupOption()
        );
        goTo(new ChartPlace(criteria));
    }

    @Override
    public void onGroupOptionSelected(ChartCriteria.GroupOption groupOption) {
        criteria = new ChartCriteria(
            criteria.getAccountId(),
            criteria.getRange(),
            groupOption
        );
        goTo(new ChartPlace(criteria));
    }

    @Override
    public void showLedger() {
        goTo(new LedgerPlace(new LedgerCoordinates(criteria.getAccountId())));
    }

    @Override
    public void goTo(Place place) {
        placeController.goTo(place);
    }

    protected void resetCriteria() {
        // If the type didn't change we can preserve some existing criteria/view settings
        criteria = new ChartCriteria(
            // Reset account selection - unless we have a stored one in the view
            view.getAccountSelection(),
            // Preserve date range of current view
            view.getChartSelectView().getDateRange(),
            ChartCriteria.GroupOption.MONTHLY
        );
    }

}
