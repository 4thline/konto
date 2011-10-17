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

package org.fourthline.konto.client.ledger.entry;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.web.bindery.event.shared.EventBus;
import org.fourthline.konto.client.service.LedgerServiceAsync;
import org.fourthline.konto.client.ledger.entry.view.DescriptionView;
import org.fourthline.konto.shared.entity.Split;

/**
 * @author Christian Bauer
 */
public abstract class DescriptionPresenter implements DescriptionView.Presenter, SelectionHandler {

    final EventBus bus;
    final LedgerServiceAsync ledgerService;

    protected DescriptionPresenter(EventBus bus,
                                   LedgerServiceAsync ledgerService) {
        this.bus = bus;
        this.ledgerService = ledgerService;
    }

    public EventBus getBus() {
        return bus;
    }

    public LedgerServiceAsync getLedgerService() {
        return ledgerService;
    }

    @Override
    public void onSelection(SelectionEvent event) {
        if (event.getSelectedItem() == null) return;
        SplitSuggestion suggestion = (SplitSuggestion) event.getSelectedItem();
        if (suggestion.getSplit() != null)
            suggest(suggestion.getSplit());
    }

    abstract public boolean isNewDescription(String text);

    abstract public void suggest(Split suggestedSplit);

}
