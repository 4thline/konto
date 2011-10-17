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

package org.fourthline.konto.client.settings.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import org.fourthline.konto.client.bundle.Bundle;
import org.seamless.gwt.component.client.binding.CheckBoxViewProperty;
import org.seamless.gwt.component.client.binding.DateFormatViewProperty;
import org.seamless.gwt.component.client.binding.ViewProperty;
import org.seamless.util.time.DateFormat;

import javax.inject.Inject;

/**
 * @author Christian Bauer
 */
public class SettingsViewImpl extends Composite implements SettingsView {

    interface UI extends UiBinder<DockLayoutPanel, SettingsViewImpl> {
    }

    private UI ui = GWT.create(UI.class);

    interface Style extends CssResource {
    }

    @UiField(provided = true)
    Bundle bundle;

    @UiField
    Style style;

    @UiField
    Button saveButton;

    @UiField
    ListBox dateFormatListBox;
    @UiField
    CheckBox newEntrySelectDayCheckBox;
    @UiField
    CheckBox roundFractionsInReportsCheckBox;

    Presenter presenter;

    final ViewProperty<DateFormat> dateFormatProperty;
    final ViewProperty<Boolean> newEntrySelectDayProperty;
    final ViewProperty<Boolean> roundFractionsInReportsProperty;

    @Inject
    public SettingsViewImpl(Bundle bundle) {
        this.bundle = bundle;

        initWidget(ui.createAndBindUi(this));

        dateFormatProperty = new DateFormatViewProperty(dateFormatListBox);
        newEntrySelectDayProperty = new CheckBoxViewProperty(newEntrySelectDayCheckBox);
        roundFractionsInReportsProperty = new CheckBoxViewProperty(roundFractionsInReportsCheckBox);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public ViewProperty<DateFormat> getDateFormatProperty() {
        return dateFormatProperty;
    }

    @Override
    public ViewProperty<Boolean> getNewEntrySelectDayProperty() {
        return newEntrySelectDayProperty;
    }

    @Override
    public ViewProperty<Boolean> getRoundFractionsInReportsProperty() {
        return roundFractionsInReportsProperty;
    }

    @UiHandler("saveButton")
    void onClickSave(ClickEvent e) {
        if (presenter != null) {
            presenter.save();
        }
    }

}
