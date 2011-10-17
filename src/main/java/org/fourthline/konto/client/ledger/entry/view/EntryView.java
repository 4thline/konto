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

package org.fourthline.konto.client.ledger.entry.view;

import com.google.gwt.user.client.ui.IsWidget;
import org.seamless.gwt.component.client.EventListeningPresenter;
import org.seamless.gwt.component.client.binding.ValidatableViewProperty;
import org.seamless.util.time.DateFormat;
import org.fourthline.konto.shared.LedgerEntry;
import org.fourthline.konto.shared.entity.Account;

import java.util.Date;

/**
 * @author Christian Bauer
 */
public interface EntryView extends IsWidget {

    public interface Presenter extends EventListeningPresenter {

        void startWith(Account currentAccount, LedgerEntry ledgerEntry, Date lastLedgerEntryDate);

        EntryView getView();

        void dateEntered(Date date);

        void addSplit();

        void removeSplit(int index);

        void saveEntry();

        void deleteEntry();

        void cancel();

    }

    void focus(Boolean selectDay);

    void setPresenter(Presenter presenter);

    void setDateFormat(DateFormat dateFormat);

    void reset();

    ValidatableViewProperty<Date> getEffectiveOnProperty();

    void showEntrySummaryView(EntrySummaryView summaryView);

    void removeEntrySummaryView();

    void hideSplitDelete();

    void showSplitDelete();

    void hideSplitAdd();

    void showSplitAdd();

    void addSplitView(SplitView splitView);

    void removeSplitView(int index);


}
