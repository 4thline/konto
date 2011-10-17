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

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import org.fourthline.konto.client.service.LedgerServiceAsync;
import org.fourthline.konto.shared.LedgerEntry;
import org.fourthline.konto.shared.entity.Entry;
import org.fourthline.konto.shared.result.LedgerLine;
import org.fourthline.konto.shared.result.LedgerLines;
import org.fourthline.konto.shared.query.LedgerLinesQueryCriteria;
import org.fourthline.konto.shared.entity.Split;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Christian Bauer
 */
public class LedgerLineSuggestOracle extends SuggestOracle {

    public static final int SUGGESTION_DELAY_MILLIS = 500;

    final LedgerServiceAsync service;
    final Long accountId;

    Timer suggestionTimer;

    public LedgerLineSuggestOracle(LedgerServiceAsync service) {
        this.service = service;
        this.accountId = null;
    }

    public LedgerLineSuggestOracle(LedgerServiceAsync service, Long accountId) {
        this.service = service;
        this.accountId = accountId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public boolean isQueryEnabled(String query) {
        return true;
    }

    @Override
    public void requestSuggestions(final Request request, final Callback callback) {

        if (getAccountId() == null
                || request.getQuery().length() < 2
                || !isQueryEnabled(request.getQuery())) {
            callback.onSuggestionsReady(request, new Response());
            return;
        }

        scheduleSuggestionRequest(new Timer() {
            public void run() {
                executeQuery(request, callback);
            }
        });
    }

    public void executeQuery(Request request, Callback callback) {

        // TODO: configurable limit?
        service.getLedgerLines(
                new LedgerLinesQueryCriteria(
                        null,
                        false,
                        request.getQuery(),
                        true,
                        null,
                        5,
                        getAccountId()
                ),
                new SplitSuggestionCallback(request, callback)
        );
    }

    protected void scheduleSuggestionRequest(Timer timer) {
        if (suggestionTimer != null) {
            suggestionTimer.cancel();
        }
        suggestionTimer = timer;
        suggestionTimer.schedule(SUGGESTION_DELAY_MILLIS);
    }

    static public class SplitSuggestionCallback implements AsyncCallback<LedgerLines> {

        Request request;
        Callback callback;

        public SplitSuggestionCallback(Request request, Callback callback) {
            this.request = request;
            this.callback = callback;
        }

        @Override
        public void onFailure(Throwable caught) {
            //.TODO: Error message on bus?
            callback.onSuggestionsReady(request, new Response());
        }

        @Override
        public void onSuccess(LedgerLines result) {
            Set<SplitSuggestion> suggestions = new HashSet(result.size());
            for (LedgerLine ledgerLine : result) {

                LedgerEntry ledgerEntry = ledgerLine.getLedgerEntry();

                // We need to find _some_ split to suggest

                if (ledgerEntry instanceof Split) {
                    Split split = (Split) ledgerEntry;
                    suggestions.add(new SplitSuggestion(split.getDescription(), split));
                } else if (ledgerEntry instanceof Entry) {
                    Entry entry = (Entry) ledgerEntry;
                    if (entry.getDescription().toLowerCase().startsWith(request.getQuery().toLowerCase()))
                        suggestions.add(new SplitSuggestion(entry.getDescription(), entry.getSplits().get(0)));
                    for (Split split : entry.getSplits()) {
                        if (split.getDescription().toLowerCase().startsWith(request.getQuery().toLowerCase()))
                            suggestions.add(new SplitSuggestion(split.getDescription(), split));
                    }
                }
            }

            Response resp = new Response(suggestions);
            callback.onSuggestionsReady(request, resp);
        }
    }

}
