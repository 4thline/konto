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

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Provider;
import org.fourthline.konto.client.dashboard.DashboardPlace;
import org.fourthline.konto.client.ledger.entry.event.EntryEditCanceled;
import org.fourthline.konto.client.ledger.entry.event.EntryEditStarted;
import org.fourthline.konto.client.ledger.entry.event.EntryModified;
import org.fourthline.konto.client.ledger.entry.event.EntryRemoved;
import org.fourthline.konto.client.ledger.entry.view.EntryView;
import org.fourthline.konto.client.ledger.event.AccountSelectionModeChange;
import org.fourthline.konto.client.ledger.view.LedgerView;
import org.seamless.gwt.notify.client.Message;
import org.seamless.gwt.notify.client.ServerFailureNotifyEvent;
import org.seamless.gwt.notify.client.NotifyEvent;
import org.fourthline.konto.client.service.LedgerServiceAsync;
import org.fourthline.konto.client.settings.GlobalSettings;
import org.fourthline.konto.client.settings.event.GlobalSettingsRefreshedEvent;
import org.seamless.util.time.DateRange;
import org.fourthline.konto.shared.LedgerCoordinates;
import org.fourthline.konto.shared.LedgerEntry;
import org.fourthline.konto.shared.entity.Entry;
import org.fourthline.konto.shared.entity.settings.GlobalOption;
import org.fourthline.konto.shared.query.LedgerLinesQueryCriteria;
import org.fourthline.konto.shared.result.LedgerLine;
import org.fourthline.konto.shared.result.LedgerLines;

import javax.inject.Inject;
import java.util.Date;
import java.util.logging.Level;

/**
 * @author Christian Bauer
 */
public class LedgerActivity extends AbstractActivity implements
        LedgerView.Presenter,
        GlobalSettingsRefreshedEvent.Handler,
        EntryEditStarted.Handler,
        EntryEditCanceled.Handler,
        EntryModified.Handler,
        EntryRemoved.Handler {

    final LedgerView view;
    final PlaceController placeController;
    final EventBus bus;
    final LedgerServiceAsync ledgerService;

    // Sub-presenter
    final Provider<EntryView.Presenter> entryPresenterProvider;
    EntryView.Presenter entryPresenter;

    LedgerCoordinates coordinates;
    LedgerLines lines;
    DateRange effectiveOnFilter;
    String descriptionFilter;

    @Inject
    public LedgerActivity(LedgerView view,
                          Provider<EntryView.Presenter> entryPresenterProvider,
                          PlaceController placeController,
                          EventBus bus,
                          LedgerServiceAsync ledgerService,
                          GlobalSettings globalSettings) {
        this.view = view;
        this.entryPresenterProvider = entryPresenterProvider;
        this.placeController = placeController;
        this.bus = bus;
        this.ledgerService = ledgerService;

        onSettingsRefreshed(globalSettings);
    }

    public LedgerActivity init(LedgerPlace place) {
        this.coordinates = place.getLedgerCoordinates();
        return this;
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, com.google.gwt.event.shared.EventBus activityBus) {
        view.setPresenter(this);

        activityBus.addHandler(GlobalSettingsRefreshedEvent.TYPE, this);
        activityBus.addHandler(EntryEditStarted.TYPE, this);
        activityBus.addHandler(EntryEditCanceled.TYPE, this);
        activityBus.addHandler(EntryModified.TYPE, this);
        activityBus.addHandler(EntryRemoved.TYPE, this);

        bus.fireEvent(new AccountSelectionModeChange());

        if (coordinates.getEntryId() != null) {
            loadLedgerLines(containerWidget, true, coordinates.getEntryId(), coordinates.getSplitId()
            );
        } else {
            loadLedgerLines(containerWidget, true, null, null);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        view.hideEntryEditContainer(); // Memorizing its size!

        if (entryPresenter != null) {
            entryPresenter.stop();
            entryPresenter = null;
        }
    }

    @Override
    public void onSettingsRefreshed(GlobalSettings gs) {
        view.setDateFormat(gs.getValue(GlobalOption.OPT_DATE_FORMAT));
    }

    @Override
    public void selectLine(LedgerLine line) {
        editEntry(line.getLedgerEntry());
    }

    @Override
    public void addLine() {
        editEntry(null);
    }

    protected void editEntry(LedgerEntry ledgerEntry) {
        if (entryPresenter != null) {
            entryPresenter.stop();
        }
        entryPresenter = entryPresenterProvider.get();

        // If this was a filtered ledger view, we might not have loaded all the
        // splits of an entry, so load them now if necessary
        if (ledgerEntry != null && ledgerEntry instanceof Entry
                && descriptionFilter != null && descriptionFilter.length() > 0) {
            ledgerService.populateSplits((Entry) ledgerEntry, new AsyncCallback<Entry>() {
                @Override
                public void onFailure(Throwable caught) {
                    bus.fireEvent(new ServerFailureNotifyEvent(caught));
                }

                @Override
                public void onSuccess(Entry result) {
                    entryPresenter.startWith(lines.getAccount(), result, getLastEntryDate());
                }
            });
        } else {
            entryPresenter.startWith(lines.getAccount(), ledgerEntry, getLastEntryDate());
        }
    }

    @Override
    public void filterEffectiveOn(DateRange dateRange) {
        if ((effectiveOnFilter == null && dateRange != null) ||
                (effectiveOnFilter != null && dateRange == null) ||
                (effectiveOnFilter != null && !effectiveOnFilter.equals(dateRange))) {
            effectiveOnFilter = dateRange;
            loadLedgerLines(false);
        } else {
            effectiveOnFilter = dateRange;
        }

    }

    @Override
    public void filterDescription(String filter) {
        if ((descriptionFilter == null && filter != null) ||
                (descriptionFilter != null && filter == null) ||
                (descriptionFilter != null && !descriptionFilter.equals(filter))) {
            this.descriptionFilter = filter;
            loadLedgerLines(false);
        } else {
            descriptionFilter = filter;
        }
    }

    @Override
    public void onEntryModified(EntryModified event) {
        bus.fireEvent(new NotifyEvent(
                new Message("Entry saved", "Modifications have been stored in the ledger.")
        ));
        hideEntryEditContainer();
        loadLedgerLines(true);
    }

    @Override
    public void onEntryRemoved(EntryRemoved event) {
        bus.fireEvent(new NotifyEvent(
                new Message("Entry removed", "The entry has been permanently removed from the ledger.")
        ));
        hideEntryEditContainer();
        loadLedgerLines(true);
    }

    @Override
    public void onEntryEditStarted(EntryEditStarted event) {
        view.getEntryEditContainer().setWidget(null);
        view.getEntryEditContainer().setWidget(entryPresenter.getView().asWidget());
        view.showEntryEditContainer(event.isLargeEdit(), event.isScrollBottom());
    }

    @Override
    public void onEntryEditCanceled(EntryEditCanceled event) {
        entryPresenter.stop();
        hideEntryEditContainer();
        loadLedgerLines(true);
    }

    @Override
    public void goTo(Place place) {
        placeController.goTo(place);
    }

    protected void hideEntryEditContainer() {
        view.hideEntryEditContainer();
        view.getEntryEditContainer().setWidget(null);
    }

    protected Date getLastEntryDate() {
        // Find the last entry line with a date not in the future
        if (lines == null || lines.size() == 0)
            return new Date();
        long currentTime = new Date().getTime();
        for (LedgerLine line : lines) {
            if (line.getDate().getTime()  < currentTime)
                return line.getDate();
        }
        return lines.get(0).getDate();
    }

    protected void loadLedgerLines(final boolean init) {
        loadLedgerLines(null, init, null, null);
    }

    protected void loadLedgerLines(final AcceptsOneWidget container,
                                   final boolean init, final Long selectEntryId, final Long selectSplitId) {
        bus.fireEvent(new NotifyEvent(
                new Message(
                        true,
                        Level.INFO,
                        "Loading entries of account",
                        "Please wait..."
                )
        ));
        ledgerService.getLedgerLines(
                new LedgerLinesQueryCriteria(descriptionFilter, true, coordinates.getAccountId(), effectiveOnFilter, selectEntryId),
                new AsyncCallback<LedgerLines>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        bus.fireEvent(new ServerFailureNotifyEvent(caught));
                    }

                    @Override
                    public void onSuccess(LedgerLines result) {
                        bus.fireEvent(new NotifyEvent());

                        if (result != null) {
                            if (container != null)
                                container.setWidget(view.asWidget());

                            view.setLedgerLines(result, selectEntryId, selectSplitId);
                            if (init) {
                                LedgerActivity.this.lines = result;
                                LedgerActivity.this.effectiveOnFilter = result.getEffectiveOn();
                                view.hideEntryEditContainer();
                                view.focus();
                            }
                        } else {
                            bus.fireEvent(new NotifyEvent(
                                    new Message(
                                            Level.WARNING,
                                            "Account or entry has been removed",
                                            "You have been redirected to the dashboard."
                                    )
                            ));
                            placeController.goTo(new DashboardPlace());
                        }
                    }
                }
        );
    }

}

