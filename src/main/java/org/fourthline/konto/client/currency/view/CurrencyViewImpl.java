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

package org.fourthline.konto.client.currency.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import javax.inject.Inject;
import org.fourthline.konto.client.bundle.Bundle;
import org.seamless.util.time.DateFormat;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.entity.MonetaryUnit;

import java.util.Collections;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class CurrencyViewImpl extends Composite implements CurrencyView {

    interface UI extends UiBinder<DockLayoutPanel, CurrencyViewImpl> {
    }

    private UI ui = GWT.create(UI.class);

    interface Style extends CssResource {
        String infoLabel();
    }

    @UiField(provided = true)
    Bundle bundle;
    @UiField
    Style style;
    @UiField
    MonetaryUnitCellTable.Style monetaryUnitStyle;
    @UiField
    CurrencyPairCellTable.Style currencyPairStyle;

    @UiField
    Button downloadAllButton;

    @UiField(provided = true)
    MonetaryUnitCellTable monetaryUnitCellTable;
    @UiField
    SimplePanel unitView;

    @UiField
    DockLayoutPanel exchangeRatePanel;
    @UiField
    ListBox exchangeUnitListBox;
    @UiField
    Button removeAllButton;
    @UiField
    Button downloadButton;

    @UiField
    ScrollPanel currencyPairsPanel;
    CurrencyPairCellTable currencyPairCellTable;
    @UiField
    SimplePanel currencyPairView;

    final SingleSelectionModel<MonetaryUnit> unitSelectionModel;
    final SingleSelectionModel<CurrencyPair> pairSelectionModel;

    Presenter presenter;

    @Inject
    public CurrencyViewImpl(Bundle bundle,
                            MonetaryUnitView unitView,
                            CurrencyPairView currencyPairView) {
        this.bundle = bundle;

        CellTable.Resources cellTableResource =
                bundle.themeBundle().create().cellTableResources().create();
        this.monetaryUnitCellTable = new MonetaryUnitCellTable(cellTableResource);
        this.currencyPairCellTable = new CurrencyPairCellTable(cellTableResource);

        initWidget(ui.createAndBindUi(this));

        monetaryUnitCellTable.applyStyle(monetaryUnitStyle);
        currencyPairCellTable.applyStyle(currencyPairStyle);

        unitSelectionModel = new SingleSelectionModel();
        unitSelectionModel.addSelectionChangeHandler(
                new SelectionChangeEvent.Handler() {
                    @Override
                    public void onSelectionChange(SelectionChangeEvent event) {
                        MonetaryUnit selected;
                        if ((selected = unitSelectionModel.getSelectedObject()) != null
                                && presenter != null) {
                            presenter.monetaryUnitSelected(selected);
                        }
                    }
                }
        );
        monetaryUnitCellTable.setSelectionModel(unitSelectionModel);


        pairSelectionModel = new SingleSelectionModel();
        pairSelectionModel.addSelectionChangeHandler(
                new SelectionChangeEvent.Handler() {
                    @Override
                    public void onSelectionChange(SelectionChangeEvent event) {
                        CurrencyPair selected;
                        if ((selected = pairSelectionModel.getSelectedObject()) != null
                                && presenter != null) {
                            presenter.currencyPairSelected(selected);
                        }
                    }
                }
        );
        currencyPairCellTable.setSelectionModel(pairSelectionModel);

        this.unitView.setWidget(unitView);
        this.currencyPairView.setWidget(currencyPairView);

        exchangeUnitListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                presenter.exchangeMonetaryUnitSelected(
                        exchangeUnitListBox.getSelectedIndex()
                );
            }
        });

    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setDateFormat(DateFormat dateFormat) {
        currencyPairCellTable.setDateFormat(dateFormat);
    }

    @Override
    public void reset() {
        setMonetaryUnits(Collections.EMPTY_LIST);
        downloadAllButton.setEnabled(false);
        exchangeRatePanel.setVisible(false);
    }

    @Override
    public void setMonetaryUnits(List<MonetaryUnit> units) {
        monetaryUnitCellTable.setMonetaryUnits(units);
        if (units.size() > 0) {
            downloadAllButton.setEnabled(true);
        }
    }

    @Override
    public void setExchangeMonetaryUnits(MonetaryUnit selectedUnit, List<MonetaryUnit> units) {
        monetaryUnitCellTable.getSelectionModel().setSelected(selectedUnit, true);

        if (units.size() > 0) {
            exchangeRatePanel.setVisible(true);
            exchangeUnitListBox.clear();
            for (MonetaryUnit unit : units) {
                exchangeUnitListBox.addItem(unit.getCurrencyCode());
            }
        }
    }

    @Override
    public void setCurrencyPairs(MonetaryUnit selectedUnit, List<CurrencyPair> pairs) {
        currencyPairCellTable.setCurrencyPairs(selectedUnit, pairs);
        if (pairs.size() == 0) {
            Label noPairsLabel = new Label("Please enter exchange rates.");
            noPairsLabel.addStyleName(style.infoLabel());
            currencyPairsPanel.setWidget(noPairsLabel);
            removeAllButton.setEnabled(false);
        } else {
            currencyPairsPanel.setWidget(currencyPairCellTable);
            removeAllButton.setEnabled(true);
        }
    }

    @UiHandler("removeAllButton")
    void onClickRemoveAll(ClickEvent e) {
        if (presenter != null && Window.confirm("Are you are you want to remove all exchange rates?")) {
            presenter.removeAll();
        }
    }

    @UiHandler("downloadButton")
    void onClickDownload(ClickEvent e) {
        if (presenter != null) {
            presenter.download();
        }
    }

    @UiHandler("downloadAllButton")
    void onClickDownloadAll(ClickEvent e) {
        if (presenter != null) {
            presenter.downloadAll();
        }
    }


}