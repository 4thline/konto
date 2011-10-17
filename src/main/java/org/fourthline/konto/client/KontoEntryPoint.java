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

package org.fourthline.konto.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class KontoEntryPoint implements EntryPoint {

    protected final KontoGinjector injector = GWT.create(KontoGinjector.class);

    public void onModuleLoad() {

        RootLayoutPanel.get().add(injector.getMainPresenter().getView());

        if (Window.Location.getParameter("demo") != null)
            enableDemo();

        injector.getPlaceHistoryHandler().handleCurrentHistory();
    }

    protected void enableDemo() {
        final PopupPanel panel = new PopupPanel(true, true);
        panel.setGlassEnabled(true);

        VerticalPanel msgPanel = new VerticalPanel();
        SafeHtmlBuilder msg = new SafeHtmlBuilder();
        msg.appendHtmlConstant("<p>")
            .appendEscaped("This is a demo instance of Konto.")
            .appendHtmlConstant("</p>");
        msg.appendHtmlConstant("<p>")
            .appendEscaped("You can make any changes you like.")
            .appendHtmlConstant("</p>");
        msg.appendHtmlConstant("<p>")
            .appendEscaped("This instance will reset itself every hour.")
            .appendHtmlConstant("</p>");

        msgPanel.add(new HTMLPanel(msg.toSafeHtml()));

        panel.add(msgPanel);
        panel.setWidth("250px");
        panel.setHeight("120px");
        panel.setPopupPosition(Window.getClientWidth() / 2 - 250, Window.getClientHeight() / 2 - 120);
        panel.show();
    }
}
