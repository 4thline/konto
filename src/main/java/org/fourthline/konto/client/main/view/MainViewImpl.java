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

package org.fourthline.konto.client.main.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import org.fourthline.konto.client.bundle.Bundle;
import org.fourthline.konto.client.currency.CurrencyPlace;
import org.fourthline.konto.client.dashboard.DashboardPlace;
import org.fourthline.konto.client.ledger.view.LedgerSidebarView;
import org.fourthline.konto.client.report.ReportPlace;
import org.fourthline.konto.client.settings.SettingsPlace;
import org.seamless.gwt.component.client.widget.ImageTextButton;
import org.seamless.gwt.component.client.widget.ResizableSplitLayoutPanel;
import org.seamless.gwt.component.client.widget.SimpleLayoutPanel;
import org.seamless.gwt.theme.shared.client.ThemeBundle;

import javax.inject.Inject;


/**
 * @author Christian Bauer
 */
public class MainViewImpl extends Composite implements MainView {

    interface UI extends UiBinder<DockLayoutPanel, MainViewImpl> {
    }

    private UI ui = GWT.create(UI.class);

    @UiField(provided = true)
    final Bundle bundle;

    @UiField(provided = true)
    final ThemeBundle themeBundle;

    @UiField(provided = true)
    SplitLayoutPanel mainSplitPanel;

    @UiField(provided = true)
    DockLayoutPanel sidebarPanel;

    @UiField
    ImageTextButton dashboardButton;
    @UiField
    ImageTextButton reportButton;
    @UiField
    ImageTextButton currencyButton;
    @UiField
    ImageTextButton settingsButton;

    @UiField(provided = true)
    LedgerSidebarView ledgerSidebarView;

    @UiField
    SimpleLayoutPanel contentPanel;

    Presenter presenter;

    @Inject
    public MainViewImpl(Bundle bundle,
                        LedgerSidebarView ledgerSidebarView) {
        this.bundle = bundle;
        this.themeBundle = bundle.themeBundle().create();

        this.mainSplitPanel = new ResizableSplitLayoutPanel();

        this.sidebarPanel = new DockLayoutPanel(Style.Unit.EM) {
            @Override
            public void onResize() {
                super.onResize();

                if (sidebarPanel.getOffsetWidth() > 0) {
                    presenter.sidebarResized(sidebarPanel.getOffsetWidth());
                }
            }
        };

        this.ledgerSidebarView = ledgerSidebarView;

        initWidget(ui.createAndBindUi(this));
    }

    public ResizableSplitLayoutPanel getMainSplitPanel() {
        // The UIBinder parser of DockLayoutPanel is buggy and prevents us from
        // having this in XML. We need to instantiate and cast it.
        return (ResizableSplitLayoutPanel) mainSplitPanel;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public AcceptsOneWidget getContentPanel() {
        return contentPanel;
    }

    @Override
    public void setSidebarSize(int width) {
        getMainSplitPanel().setSplitPosition(sidebarPanel, width, false);
    }

    @UiHandler("dashboardButton")
    void onClickDashboard(ClickEvent e) {
        presenter.goTo(new DashboardPlace());
    }

    @UiHandler("currencyButton")
    void onClickCurrency(ClickEvent e) {
        presenter.goTo(new CurrencyPlace());
    }

    @UiHandler("reportButton")
    void onClickReport(ClickEvent e) {
        presenter.goTo(new ReportPlace());
    }

    @UiHandler("settingsButton")
    void onClickSettings(ClickEvent e) {
        presenter.goTo(new SettingsPlace());
    }
}
