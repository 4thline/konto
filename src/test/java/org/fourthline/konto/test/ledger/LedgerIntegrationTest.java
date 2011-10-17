package org.fourthline.konto.test.ledger;

import com.google.inject.Provider;
import org.fourthline.konto.client.ledger.entry.AccountSuggestion;
import org.fourthline.konto.client.ledger.entry.EntryPresenter;
import org.fourthline.konto.client.ledger.entry.EntrySummaryPresenter;
import org.fourthline.konto.client.ledger.entry.SplitPresenter;
import org.fourthline.konto.client.ledger.entry.view.EntrySummaryView;
import org.fourthline.konto.client.ledger.entry.view.SplitView;
import org.fourthline.konto.client.settings.GlobalSettings;
import org.fourthline.konto.test.IntegrationTest;
import org.fourthline.konto.test.ledger.mock.MockEntrySummaryView;
import org.fourthline.konto.test.ledger.mock.MockEntryView;
import org.fourthline.konto.test.ledger.mock.MockExchangeView;
import org.fourthline.konto.test.ledger.mock.MockSplitView;
import org.fourthline.konto.test.ledger.mock.MockSuggestionSelectView;

/**
 * @author Christian Bauer
 */
public class LedgerIntegrationTest extends IntegrationTest {

    public Provider<EntryPresenter> entryPresenterProvider = new Provider<EntryPresenter>() {
        MockEntryView entryView = new MockEntryView();
        @Override
        public EntryPresenter get() {
            return new EntryPresenter(
                    entryView,
                    splitPresenterProvider,
                    entrySummaryPresenterProvider,
                    eventBus,
                    ledgerServiceAsync,
                    new GlobalSettings(settingsServiceAsync, eventBus)
            );
        }
    };

    public Provider<SplitView.Presenter> splitPresenterProvider = new Provider<SplitView.Presenter>() {
        @Override
        public SplitView.Presenter get() {

            // These views are not singletons!
            MockSuggestionSelectView<AccountSuggestion> accountSelectView =
                    new MockSuggestionSelectView<AccountSuggestion>();

            MockExchangeView exchangeView =
                    new MockExchangeView();

            MockSplitView splitView =
                    new MockSplitView(accountSelectView, exchangeView);

            return new SplitPresenter(splitView, eventBus, ledgerServiceAsync, currencyServiceAsync, placeController);
        }
    };

    public Provider<EntrySummaryView.Presenter> entrySummaryPresenterProvider = new Provider<EntrySummaryView.Presenter>() {
        MockEntrySummaryView entrySummaryView = new MockEntrySummaryView();

        @Override
        public EntrySummaryView.Presenter get() {
            return new EntrySummaryPresenter(entrySummaryView, eventBus, ledgerServiceAsync);
        }
    };


}
