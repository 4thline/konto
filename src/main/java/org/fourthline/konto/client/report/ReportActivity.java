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

package org.fourthline.konto.client.report;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import org.fourthline.konto.client.ledger.event.MultipleAccountsSelected;
import org.fourthline.konto.client.ledger.event.AccountSelectionModeChange;
import org.fourthline.konto.client.report.view.ReportResultView;
import org.fourthline.konto.client.report.view.ReportSelectView;
import org.fourthline.konto.client.report.view.ReportView;
import org.fourthline.konto.client.service.CurrencyServiceAsync;
import org.fourthline.konto.client.service.LedgerServiceAsync;
import org.fourthline.konto.client.service.ReportServiceAsync;
import org.fourthline.konto.client.settings.GlobalSettings;
import org.fourthline.konto.client.settings.event.GlobalSettingsRefreshedEvent;
import org.fourthline.konto.shared.AccountType;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.fourthline.konto.shared.entity.settings.GlobalOption;
import org.fourthline.konto.shared.query.LineReportCriteria;
import org.fourthline.konto.shared.query.LineReportOption;
import org.fourthline.konto.shared.result.ReportLines;
import org.seamless.gwt.component.client.Print;
import org.seamless.gwt.notify.client.Message;
import org.seamless.gwt.notify.client.NotifyEvent;
import org.seamless.gwt.notify.client.ServerFailureNotifyEvent;
import org.seamless.util.time.DateRange;

import javax.inject.Inject;
import java.util.*;
import java.util.logging.Level;

/**
 * TODO: The view/criteria state management is buggy and a mess.
 * Cleanup when graph feature has been written.
 *
 * @author Christian Bauer
 */
public class ReportActivity extends AbstractActivity
        implements
        ReportView.Presenter,
        ReportSelectView.Presenter,
        ReportResultView.Presenter,
        MultipleAccountsSelected.Handler,
        GlobalSettingsRefreshedEvent.Handler {

    class InitCurrencyCodesCallback implements AsyncCallback<List<MonetaryUnit>> {

        @Override
        public void onFailure(Throwable caught) {
            bus.fireEvent(new ServerFailureNotifyEvent(caught));
        }

        @Override
        public void onSuccess(List<MonetaryUnit> result) {
            List<String> currencyCodes = new ArrayList<String>();
            for (MonetaryUnit monetaryUnit : result) {
                currencyCodes.add(monetaryUnit.getCurrencyCode());
            }
            view.getReportSelectView().setCurrencyCodes(
                currencyCodes.toArray(new String[currencyCodes.size()]),
                criteria != null ? criteria.getCurrencyCode() : view.getReportSelectView().getCurrencyCode()
            );
        }
    }

    final ReportView view;
    final PlaceController placeController;
    final EventBus bus;
    final ReportServiceAsync reportService;
    final LedgerServiceAsync ledgerService;
    final CurrencyServiceAsync currencyService;

    GlobalSettings globalSettings;
    LineReportCriteria criteria;

    @Inject
    public ReportActivity(ReportView view,
                          PlaceController placeController,
                          EventBus bus,
                          ReportServiceAsync reportService,
                          LedgerServiceAsync ledgerService,
                          CurrencyServiceAsync currencyService,
                          GlobalSettings globalSettings) {
        this.view = view;
        this.placeController = placeController;
        this.bus = bus;
        this.reportService = reportService;
        this.ledgerService = ledgerService;
        this.currencyService = currencyService;

        onSettingsRefreshed(globalSettings);
    }

    public ReportActivity init(ReportPlace place) {
        criteria = place.getCriteria();
        return this;
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, com.google.gwt.event.shared.EventBus activityBus) {

        if (criteria == null) {
            resetCriteria(view.getReportSelectView().getReportType(), true);
            // Now update the URL bar in the browser
            new Timer() {
                @Override
                public void run() {
                    goTo(new ReportPlace(criteria));
                }
            }.schedule(1); // TODO: Asynchronous, otherwise URL bar doesn't refresh
            return;
        }

        view.setPresenter(this);
        view.getReportSelectView().setPresenter(this);
        view.getReportSelectView().setCriteria(criteria);
        view.getReportResultWidget().setPresenter(this);

        containerWidget.setWidget(view.asWidget());

        activityBus.addHandler(MultipleAccountsSelected.TYPE, this);
        activityBus.addHandler(GlobalSettingsRefreshedEvent.TYPE, this);

        // We store the account selection in our view because we have no direct
        // connection with the account select tree and can't query its state
        view.setAccountSelection(criteria.getAccountSelection());

        // Init the currencies
        currencyService.getMonetaryUnits(new InitCurrencyCodesCallback());

        // Switch the account tree select widget to whatever is in the criteria
        bus.fireEvent(new AccountSelectionModeChange(
                criteria.getType().getAccountSelectOptions(),
                criteria.getAccountSelection()
        ));

        // Execute the query on start, the only way to "refresh" is to go to a new ReportPlace
        reportService.getReportLines(
                criteria,
                new AsyncCallback<ReportLines[]>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        bus.fireEvent(new ServerFailureNotifyEvent(caught));
                    }

                    @Override
                    public void onSuccess(ReportLines[] result) {
                        Map<AccountType, ReportLines> linesByType = new LinkedHashMap<>();
                        for (ReportLines r : result) {
                            linesByType.put(r.getCriteria().getType(), r);
                        }

                        if (linesByType.size() == 0 ||
                                linesByType.entrySet().iterator().next().getKey() == null) {
                            bus.fireEvent(new NotifyEvent(new Message(
                                    Level.INFO,
                                    "Empty report generated...",
                                    "Please select accounts or change settings!")
                            ));
                            return;
                        } else {
                            bus.fireEvent(new NotifyEvent());
                        }

                        view.getReportResultWidget().setReportLines(
                                criteria,
                                linesByType
                        );
                    }
                }
        );
    }

    @Override
    public void onSettingsRefreshed(GlobalSettings gs) {
        this.globalSettings = gs;
        view.getReportSelectView().setDateFormat(gs.getValue(GlobalOption.OPT_DATE_FORMAT));
        view.getReportResultWidget().setDateFormat(gs.getValue(GlobalOption.OPT_DATE_FORMAT));
        view.getReportResultWidget().setRoundFractions(gs.getValue(GlobalOption.OPT_ROUND_FRACTIONS_IN_REPORTS));
    }

    @Override
    public void onReportTypeSelected(ReportType type) {
        resetCriteria(type, false);
        goTo(new ReportPlace(criteria));
    }

    @Override
    public void onReportOptionsUpdated(LineReportOption reportOptions) {
        criteria = new LineReportCriteria(
                criteria.getAccountSelection(),
                criteria.getCurrencyCode(),
                criteria.getDayOfExchangeRate(),
                criteria.getType(),
                criteria.getRange(),
                reportOptions
        );
        goTo(new ReportPlace(criteria));
    }

    @Override
    public void onMultipleAccountsSelected(MultipleAccountsSelected event) {
        view.setAccountSelection(event.getSelection()); // Store it in the shared view

        criteria = new LineReportCriteria(
                event.getSelection(),
                criteria.getCurrencyCode(),
                criteria.getDayOfExchangeRate(),
                criteria.getType(),
                criteria.getRange(),
                criteria.getOptions()
        );
        goTo(new ReportPlace(criteria));
    }

    @Override
    public void onDateRangeUpdated(DateRange dateRange) {
        criteria = new LineReportCriteria(
                criteria.getAccountSelection(),
                criteria.getCurrencyCode(),
                criteria.getDayOfExchangeRate(),
                criteria.getType(),
                dateRange,
                criteria.getOptions()
        );
        goTo(new ReportPlace(criteria));
    }

    @Override
    public void onCurrencySelected(String currencyCode) {
        criteria = new LineReportCriteria(
                criteria.getAccountSelection(),
                currencyCode,
                criteria.getDayOfExchangeRate(),
                criteria.getType(),
                criteria.getRange(),
                criteria.getOptions()
        );
        goTo(new ReportPlace(criteria));
    }

    @Override
    public void onBookmark() {
        StringBuilder title = new StringBuilder();
        title.append(criteria.getType().getLabel());
        title.append(" | ");
        title.append(criteria.getType().getLabelSub(
                globalSettings.getValue(GlobalOption.OPT_DATE_FORMAT),
                criteria
        ));
        view.promptBookmark(title.toString());
    }

    @Override
    public void onPrint() {
        ReportResultView.Style style = view.getReportResultWidget().getPrintStyle();
        StringBuilder sb = new StringBuilder();
        sb.append("<style type=\"text/css\" media=\"print\">");
        sb.append(style.getText());
        sb.append("body { font-family: sans-serif;}");
        // TODO: Font size smaller!
        sb.append("</style>");
        Print.it(sb.toString(), view.getReportResultWidget().getPrintObject());
    }

    @Override
    public void goTo(Place place) {
        placeController.goTo(place);
    }

    protected void resetCriteria(ReportType type, boolean useViewState) {
        // TODO: Ugh...
        // If the type didn't change we can preserve some existing criteria/view settings
        criteria = new LineReportCriteria(
                // Reset account selection - unless we have a stored one in the view
                view.getAccountSelection() != null && useViewState
                        ? view.getAccountSelection()
                        : type.getDefaultAccountSelection(),
                // Reset currency (always take the selected currency code from view)
                view.getReportSelectView().getCurrencyCode(),
                // Reset day of exchange rate
                new Date(),
                // Reset type
                type,
                // Preserve date range of current view
                view.getReportSelectView().getDateRange(type),
                // Preserve other options of current view, if possible
                useViewState
                        ?
                        new LineReportOption(
                                ReportSelectView.Option.ENABLE_ENTRY_DETAILS.in(type.getReportSelectOptions())
                                        && view.getReportSelectView().getReportOptions().isEntryDetails(),
                                ReportSelectView.Option.ENABLE_ENTRY_DETAILS.in(type.getReportSelectOptions())
                                        && view.getReportSelectView().getReportOptions().isEntryAccounts(),
                                view.getReportSelectView().getReportOptions().isExchangeRates(),
                                view.getReportSelectView().getReportOptions().isZeroBalances()
                        )
                        :
                        type.getDefaultOptions()
        );
    }

}
