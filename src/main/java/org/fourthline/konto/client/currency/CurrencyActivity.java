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

package org.fourthline.konto.client.currency;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.fourthline.konto.client.currency.event.CurrencyPairModified;
import org.fourthline.konto.client.currency.event.MonetaryUnitModified;
import org.fourthline.konto.client.currency.view.CurrencyView;
import org.fourthline.konto.client.ledger.event.AccountSelectionModeChange;
import org.seamless.gwt.notify.client.Message;
import org.seamless.gwt.notify.client.ServerFailureNotifyEvent;
import org.seamless.gwt.notify.client.NotifyEvent;
import org.fourthline.konto.client.service.CurrencyServiceAsync;
import org.fourthline.konto.client.settings.GlobalSettings;
import org.fourthline.konto.client.settings.event.GlobalSettingsRefreshedEvent;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.fourthline.konto.shared.entity.settings.GlobalOption;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Christian Bauer
 */
public class CurrencyActivity extends AbstractActivity
        implements
        CurrencyView.Presenter,
        GlobalSettingsRefreshedEvent.Handler,
        MonetaryUnitModified.Handler,
        CurrencyPairModified.Handler {

    class InitMonetaryUnitsCallback implements AsyncCallback<List<MonetaryUnit>> {

        protected Long editMonetaryUnitId;

        InitMonetaryUnitsCallback(Long editMonetaryUnitId) {
            this.editMonetaryUnitId = editMonetaryUnitId;
        }

        @Override
        public void onFailure(Throwable caught) {
            bus.fireEvent(new ServerFailureNotifyEvent(caught));
        }

        @Override
        public void onSuccess(List<MonetaryUnit> result) {
            monetaryUnits = result;

            view.reset();
            view.setMonetaryUnits(result);

            boolean edit = false;
            if (editMonetaryUnitId != null) {
                for (MonetaryUnit monetaryUnit : result) {
                    if (monetaryUnit.getId().equals(editMonetaryUnitId)) {
                        monetaryUnitSelected(monetaryUnit);
                        edit = true;
                        break;
                    }
                }
            }
            if (!edit) {
                unitPresenter.startWith(null);
            }
        }
    }

    final CurrencyView view;

    final PlaceController placeController;
    final EventBus bus;
    final CurrencyServiceAsync service;

    final MonetaryUnitPresenter unitPresenter;
    final CurrencyPairPresenter currencyPairPresenter;

    Long monetaryUnitId;
    List<MonetaryUnit> monetaryUnits;
    MonetaryUnit selectedUnit;
    List<MonetaryUnit> exchangeUnits;
    MonetaryUnit exchangeUnit;
    List<CurrencyPair> currencyPairs;

    @Inject
    public CurrencyActivity(CurrencyView view,
                            MonetaryUnitPresenter unitPresenter,
                            CurrencyPairPresenter currencyPairPresenter,
                            PlaceController placeController,
                            EventBus bus,
                            CurrencyServiceAsync service,
                            GlobalSettings globalSettings) {
        this.view = view;
        this.unitPresenter = unitPresenter;
        this.currencyPairPresenter = currencyPairPresenter;
        this.placeController = placeController;
        this.bus = bus;
        this.service = service;

        onSettingsRefreshed(globalSettings);
    }

    public CurrencyActivity init(CurrencyPlace place) {
        this.monetaryUnitId = place.getMonetaryUnitId();
        return this;
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, com.google.gwt.event.shared.EventBus activityBus) {
        view.setPresenter(this);
        containerWidget.setWidget(view.asWidget());

        activityBus.addHandler(GlobalSettingsRefreshedEvent.TYPE, this);
        activityBus.addHandler(MonetaryUnitModified.TYPE, this);
        activityBus.addHandler(CurrencyPairModified.TYPE, this);

        bus.fireEvent(new AccountSelectionModeChange());

        service.getMonetaryUnits(
                new InitMonetaryUnitsCallback(monetaryUnitId)
        );
    }

    @Override
    public void onSettingsRefreshed(GlobalSettings gs) {
        view.setDateFormat(gs.getValue(GlobalOption.OPT_DATE_FORMAT));
        currencyPairPresenter.onSettingsRefreshed(gs);
    }

    @Override
    public void monetaryUnitSelected(MonetaryUnit selected) {
        this.selectedUnit = selected;

        unitPresenter.startWith(selectedUnit);

        refreshExchangeUnits();
        refreshCurrencyPairs();
    }

    @Override
    public void exchangeMonetaryUnitSelected(int index) {
        exchangeUnit = exchangeUnits.get(index);
        refreshCurrencyPairs();
    }

    @Override
    public void currencyPairSelected(CurrencyPair pair) {
        currencyPairPresenter.startWith(selectedUnit, exchangeUnit, pair);
    }

    @Override
    public void onMonetaryUnitModified(MonetaryUnitModified event) {
        service.getMonetaryUnits(
                new InitMonetaryUnitsCallback(
                        event.getUnit() != null ? event.getUnit().getId() : null
                )
        );
    }

    @Override
    public void onCurrencyPairModified(CurrencyPairModified event) {
        refreshCurrencyPairs();
    }

    @Override
    public void downloadAll() {
        showDownloadBusyMessage();
        service.download(null, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                bus.fireEvent(new ServerFailureNotifyEvent(caught));
            }

            @Override
            public void onSuccess(String result) {
                if (result == null) {
                    showDownloadCompleteMessage();
                } else {
                    showDownloadFailedMessage(result);
                }
                refreshCurrencyPairs();
            }
        });
    }

    @Override
    public void download() {
        CurrencyPair selectedPair = getSelectedPair();
        if (selectedPair == null) return;

        showDownloadBusyMessage();
        service.download(selectedPair, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                bus.fireEvent(new ServerFailureNotifyEvent(caught));
            }

            @Override
            public void onSuccess(String result) {
                if (result == null) {
                    showDownloadCompleteMessage();
                } else {
                    showDownloadFailedMessage(result);
                }
                refreshCurrencyPairs();
            }
        });
    }

    @Override
    public void removeAll() {
        CurrencyPair selectedPair = getSelectedPair();
        if (selectedPair == null) return;

        service.removeAll(
                selectedPair,
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        bus.fireEvent(new ServerFailureNotifyEvent(caught));
                    }

                    @Override
                    public void onSuccess(Void result) {
                        bus.fireEvent(new NotifyEvent(
                                new Message(
                                        Level.INFO,
                                        "Exchange rates deleted",
                                        "All exchange rates have been permanently removed."
                                )
                        ));
                        refreshCurrencyPairs();
                    }
                }
        );

    }

    protected CurrencyPair getSelectedPair() {
        if (selectedUnit == null || exchangeUnit == null) return null;
        return new CurrencyPair(selectedUnit, exchangeUnit);
    }

    protected void refreshExchangeUnits() {
        List<MonetaryUnit> list = new ArrayList();
        for (MonetaryUnit monetaryUnit : monetaryUnits) {
            if (!monetaryUnit.equals(selectedUnit)) {
                list.add(monetaryUnit);
            }
        }
        exchangeUnits = list;

        // Take the first one, we assume this is what thew view shows as "selected"
        exchangeUnit = exchangeUnits.size() > 0 ? exchangeUnits.get(0) : null;

        view.setExchangeMonetaryUnits(selectedUnit, exchangeUnits);
    }

    protected void refreshCurrencyPairs() {
        if (selectedUnit == null || exchangeUnit == null) return;

        currencyPairPresenter.startWith(selectedUnit, exchangeUnit, null);

        service.getCurrencyPairs(
                selectedUnit,
                exchangeUnit,
                new AsyncCallback<List<CurrencyPair>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        bus.fireEvent(new ServerFailureNotifyEvent(caught));
                    }

                    @Override
                    public void onSuccess(List<CurrencyPair> result) {
                        currencyPairs = result;
                        view.setCurrencyPairs(selectedUnit, result);
                    }
                }
        );
    }

    protected void showDownloadBusyMessage() {
        bus.fireEvent(new NotifyEvent(
                new Message(
                        true,
                        Level.INFO,
                        "Downloading exchange rates",
                        "Please wait until download is complete..."
                )
        ));
    }

    protected void showDownloadFailedMessage(String msg) {
        bus.fireEvent(new NotifyEvent(
                new Message(
                        Level.WARNING,
                        "Downloading exchange rates failed",
                        msg
                )
        ));
    }

    protected void showDownloadCompleteMessage() {
        bus.fireEvent(new NotifyEvent(
                new Message(
                        Level.INFO,
                        "Download complete",
                        "Exchange rates have been downloaded."
                )
        ));
    }
}

