package org.fourthline.konto.test.ledger;

import com.google.gwt.user.client.ui.SuggestOracle;
import org.fourthline.konto.client.ledger.entry.AccountSelectPresenter;
import org.fourthline.konto.client.ledger.entry.AccountSuggestion;
import org.fourthline.konto.client.ledger.entry.EntryPresenter;
import org.fourthline.konto.client.ledger.entry.LedgerLineSuggestOracle;
import org.fourthline.konto.client.ledger.entry.SplitPresenter;
import org.fourthline.konto.client.ledger.entry.SplitSuggestion;
import org.fourthline.konto.client.ledger.entry.event.EntryEditStarted;
import org.fourthline.konto.client.ledger.entry.event.EntryModified;
import org.fourthline.konto.shared.LedgerEntry;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.entity.Entry;
import org.fourthline.konto.shared.query.LedgerLinesQueryCriteria;
import org.fourthline.konto.shared.result.LedgerLines;
import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.entity.Split;
import org.fourthline.konto.test.ledger.mock.MockEntryView;
import org.fourthline.konto.test.ledger.mock.MockSplitView;
import org.fourthline.konto.test.ledger.mock.MockSuggestionSelectView;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author Christian Bauer
 */
public class EntryPresenterTest extends LedgerIntegrationTest {

    public class SimpleEntryCreate {

        public Account currentAccount;

        public EntryPresenter entryPresenter;
        public MockEntryView entryView;

        public SplitPresenter splitPresenter;
        public MockSplitView splitView;

        public AccountSelectPresenter accountSelectPresenter;
        public MockSuggestionSelectView<AccountSuggestion> accountSelectView;

        public SimpleEntryCreate(Long accountId) {

            currentAccount = accountDAO.getAccount(accountId);

            entryPresenter = entryPresenterProvider.get();

            eventBus.events.clear();

            entryPresenter.startWith(currentAccount, null, new Date());

            entryView = (MockEntryView) entryPresenter.getView();
            assertEquals(entryView.presenter, entryPresenter);

            splitPresenter = (SplitPresenter) entryPresenter.getSplitPresenters().get(0);
            splitView = (MockSplitView) splitPresenter.getView();
            assertEquals(splitView.presenter, splitPresenter);

            accountSelectPresenter = splitPresenter.getAccountSelectPresenter();
            accountSelectView = (MockSuggestionSelectView<AccountSuggestion>) splitView.getAccountSelectView();
            assertEquals(accountSelectView.presenter, accountSelectPresenter);

            assertEquals(eventBus.events.size(), 1);
            assert ((EntryEditStarted) eventBus.events.get(0)).getLedgerEntry() != null;
            assert ((EntryEditStarted) eventBus.events.get(0)).getLedgerEntry().getId() == null;
            eventBus.events.clear();
        }
    }

    public class SimpleEntryEdit {

        public Account currentAccount;
        public LedgerLines lines;
        public LedgerEntry entry;

        public EntryPresenter entryPresenter;
        public MockEntryView entryView;

        public SplitPresenter splitPresenter;
        public MockSplitView splitView;

        public AccountSelectPresenter accountSelectPresenter;
        public MockSuggestionSelectView<AccountSuggestion> accountSelectView;

        public SimpleEntryEdit(Long accountId, int ledgerLineIndex) {

            currentAccount = accountDAO.getAccount(accountId);
            lines = entryDAO.getLedgerLines(currentAccount, new LedgerLinesQueryCriteria());
            entry = lines.get(ledgerLineIndex).getLedgerEntry();

            entryPresenter = entryPresenterProvider.get();

            eventBus.events.clear();

            if (entry instanceof Entry) {
                entryPresenter.startWith(currentAccount, entry, new Date());
            } else {
                entryPresenter.startWith(currentAccount, entry, new Date());
            }

            entryView = (MockEntryView) entryPresenter.getView();
            assertEquals(entryView.presenter, entryPresenter);

            splitPresenter = (SplitPresenter) entryPresenter.getSplitPresenters().get(0);
            splitView = (MockSplitView) splitPresenter.getView();
            assertEquals(splitView.presenter, splitPresenter);

            accountSelectPresenter = splitPresenter.getAccountSelectPresenter();
            accountSelectView = (MockSuggestionSelectView<AccountSuggestion>) splitView.getAccountSelectView();
            assertEquals(accountSelectView.presenter, accountSelectPresenter);

            assertEquals(eventBus.events.size(), 1);
            assertEquals(((EntryEditStarted) eventBus.events.get(0)).getLedgerEntry(), entry);
            eventBus.events.clear();
        }
    }

    @Test
    public void createEntrySuggest() throws Exception {

        getCurrentSession().beginTransaction();

        final SimpleEntryCreate create = new SimpleEntryCreate(1l);

        assertEquals(create.splitPresenter.isNewDescription("fi"), true);

        final boolean[] tests = new boolean[1];
        LedgerLineSuggestOracle llOracle = new LedgerLineSuggestOracle(ledgerServiceAsync, 1l);
        SuggestOracle.Request request = new SuggestOracle.Request("fi");
        SuggestOracle.Callback callback = new SuggestOracle.Callback() {
            @Override
            public void onSuggestionsReady(SuggestOracle.Request request, SuggestOracle.Response response) {
                Collection<SplitSuggestion> suggestions = (Collection<SplitSuggestion>) response.getSuggestions();
                assertEquals(suggestions.size(), 2);
                Split suggestion = suggestions.iterator().next().getSplit();
                create.splitView.description = suggestion.getDescription();
                create.splitPresenter.suggest(suggestion);
                tests[0] = true;
            }
        };
        llOracle.executeQuery(request, callback);

        for (boolean test : tests) {
            assert test;
        }

        create.entryPresenter.saveEntry();

        Entry entry = create.entryPresenter.getEntry();
        assertEquals(((EntryModified) eventBus.events.get(0)).getLedgerEntry(), entry);

        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();
        Entry storedEntry = entryDAO.getEntry(entry.getId());
        assertEquals(storedEntry.getDescription(), "Filling up the car");
        List<Split> splits = entryDAO.populateSplits(storedEntry).getSplits();
        assertEquals(splits.size(), 1);
        assertEquals(splits.get(0).getDescription(), "Filling up the car");
        assertEquals(splits.get(0).getEntryAmount().getString(), "-50.00");
        assertEquals(splits.get(0).getAmount().getString(), "50.00");
        assertEquals(splits.get(0).getAccount().getName(), "Gas");
        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void createEntryDebit() throws Exception {

        getCurrentSession().beginTransaction();

        SimpleEntryCreate create = new SimpleEntryCreate(1l);

        create.splitView.description = "Some Description";
        create.splitView.debit = new MonetaryAmount(create.splitView.monetaryUnit, 100);

        create.accountSelectPresenter.onSelection(
                new AccountSuggestion(accountDAO.getAccount(3l))
        );

        create.entryPresenter.saveEntry();

        Entry entry = create.entryPresenter.getEntry();
        assertEquals(((EntryModified) eventBus.events.get(0)).getLedgerEntry(), entry);

        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();
        Entry storedEntry = entryDAO.getEntry(entry.getId());
        assertEquals(storedEntry.getDescription(), "Some Description");
        List<Split> splits = entryDAO.populateSplits(storedEntry).getSplits();

        assertEquals(splits.size(), 1);
        assertEquals(splits.get(0).getEntryAmount().getString(), "-100.00");
        assertEquals(splits.get(0).getAmount().getString(), "100.00");
        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void createEntryCredit() throws Exception {

        getCurrentSession().beginTransaction();

        SimpleEntryCreate create = new SimpleEntryCreate(1l);

        create.splitView.description = "Some Description";
        create.splitView.credit = new MonetaryAmount(create.splitView.monetaryUnit, 100);

        create.accountSelectPresenter.onSelection(
                new AccountSuggestion(accountDAO.getAccount(6l))
        );

        create.entryPresenter.saveEntry();

        Entry entry = create.entryPresenter.getEntry();
        assertEquals(((EntryModified) eventBus.events.get(0)).getLedgerEntry(), entry);

        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();
        Entry storedEntry = entryDAO.getEntry(entry.getId());
        assertEquals(storedEntry.getDescription(), "Some Description");
        List<Split> splits = entryDAO.populateSplits(storedEntry).getSplits();

        assertEquals(splits.size(), 1);
        assertEquals(splits.get(0).getEntryAmount().getString(), "100.00");
        assertEquals(splits.get(0).getAmount().getString(), "-100.00");
        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void createEntryDebitExchange() throws Exception {

        getCurrentSession().beginTransaction();

        SimpleEntryCreate create = new SimpleEntryCreate(1l);

        create.splitView.description = "Some Description";
        create.splitView.debit = new MonetaryAmount(create.splitView.monetaryUnit, 100);

        assertEquals(create.splitView.exchangeFieldsVisible, false);

        create.accountSelectPresenter.onSelection(
                new AccountSuggestion(accountDAO.getAccount(5l))
        );

        assertEquals(create.splitView.exchangeFieldsVisible, true);

        assertEquals(create.splitView.exchangeView.currencyPair.getFromUnit().getCurrencyCode(), "USD");
        assertEquals(create.splitView.exchangeView.currencyPair.getToUnit().getCurrencyCode(), "EUR");
        assertEquals(create.splitView.exchangeView.amountProperty.get().getUnit().getCurrencyCode(), "EUR");
        assertEquals(create.splitView.exchangeView.amountProperty.get().getString(), "100.00");
        assertEquals(create.splitView.exchangeView.rateProperty.get().toString(), CurrencyPair.DEFAULT_EXCHANGE_RATE.toString());

        create.splitView.exchangeView.rateProperty.set(new BigDecimal("0.75"));
        create.splitPresenter.getExchangePresenter().rateUpdated();
        assertEquals(create.splitView.exchangeView.amountProperty.get().getString(), "75.00");

        create.splitView.exchangeView.amountProperty.set(
                new MonetaryAmount(create.splitView.exchangeView.amountProperty.get().getUnit(), new BigDecimal("74"))
        );
        create.splitPresenter.getExchangePresenter().exchangedAmountUpdated();
        assertEquals(create.splitView.exchangeView.rateProperty.get().toString(), "0.740000");

        create.entryPresenter.saveEntry();

        Entry entry = create.entryPresenter.getEntry();
        assertEquals(((EntryModified) eventBus.events.get(0)).getLedgerEntry(), entry);

        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();
        Entry storedEntry = entryDAO.getEntry(entry.getId());
        assertEquals(storedEntry.getDescription(), "Some Description");
        List<Split> splits = entryDAO.populateSplits(storedEntry).getSplits();

        assertEquals(splits.size(), 1);
        assertEquals(splits.get(0).getEntryAmount().getString(), "-100.00");
        assertEquals(splits.get(0).getAmount().getString(), "74.00");
        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void createEntryCreditExchange() throws Exception {

        getCurrentSession().beginTransaction();

        SimpleEntryCreate create = new SimpleEntryCreate(1l);

        create.splitView.description = "Some Description";
        create.splitView.credit = new MonetaryAmount(create.splitView.monetaryUnit, 100);

        assertEquals(create.splitView.exchangeFieldsVisible, false);

        create.accountSelectPresenter.onSelection(
                new AccountSuggestion(accountDAO.getAccount(5l))
        );

        create.splitView.exchangeView.rateProperty.set(new BigDecimal("0.73"));
        create.splitPresenter.getExchangePresenter().rateUpdated();

        create.entryPresenter.saveEntry();

        Entry entry = create.entryPresenter.getEntry();
        assertEquals(((EntryModified) eventBus.events.get(0)).getLedgerEntry(), entry);

        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();
        Entry storedEntry = entryDAO.getEntry(entry.getId());
        assertEquals(storedEntry.getDescription(), "Some Description");
        List<Split> splits = entryDAO.populateSplits(storedEntry).getSplits();

        assertEquals(splits.size(), 1);
        assertEquals(splits.get(0).getEntryAmount().getString(), "100.00");
        assertEquals(splits.get(0).getAmount().getString(), "-73.00");
        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void createEntryUpdateExchangeInputs() throws Exception {

        getCurrentSession().beginTransaction();

        SimpleEntryCreate create = new SimpleEntryCreate(1l);

        create.splitView.description = "Some Description";
        create.splitView.credit = new MonetaryAmount(create.splitView.monetaryUnit, 100);

        assertEquals(create.splitView.exchangeFieldsVisible, false);

        create.accountSelectPresenter.onSelection(
                new AccountSuggestion(accountDAO.getAccount(5l))
        );

        create.splitView.exchangeView.rateProperty.set(new BigDecimal("0"));
        create.splitPresenter.getExchangePresenter().rateUpdated();
        assertEquals(create.splitView.exchangeView.amountProperty.get().getString(), "100.00");

        create.splitView.exchangeView.rateProperty.set(new BigDecimal("-12345"));
        create.splitPresenter.getExchangePresenter().rateUpdated();
        assertEquals(create.splitView.exchangeView.amountProperty.get().getString(), "100.00");

        create.splitView.exchangeView.rateProperty.set(new BigDecimal("0.73"));
        create.splitPresenter.getExchangePresenter().rateUpdated();
        assertEquals(create.splitView.exchangeView.amountProperty.get().getString(), "73.00");

        create.splitView.exchangeView.rateProperty.set(new BigDecimal("0.74"));
        create.splitPresenter.getExchangePresenter().rateUpdated();
        assertEquals(create.splitView.exchangeView.amountProperty.get().getString(), "74.00");

        MonetaryAmount exchangedAmount = create.splitView.exchangeView.amountProperty.get();
        create.splitView.exchangeView.amountProperty.set(
                new MonetaryAmount(exchangedAmount.getUnit(), "72")
        );
        create.splitPresenter.getExchangePresenter().exchangedAmountUpdated();
        assertEquals(create.splitView.exchangeView.rateProperty.get().toString(), "0.720000");
        assertEquals(create.splitView.exchangeView.amountProperty.get().getString(), "72.00");

        create.splitView.exchangeView.amountProperty.set(
                new MonetaryAmount(exchangedAmount.getUnit(), "-72")
        );
        create.splitPresenter.getExchangePresenter().exchangedAmountUpdated();
        assertEquals(create.splitView.exchangeView.rateProperty.get().toString(), "0.720000");
        assertEquals(create.splitView.exchangeView.amountProperty.get().getString(), "72.00");

        create.splitView.exchangeView.amountProperty.set(
                new MonetaryAmount(exchangedAmount.getUnit())
        );
        create.splitPresenter.getExchangePresenter().exchangedAmountUpdated();
        assertEquals(create.splitView.exchangeView.rateProperty.get().toString(), "0.720000");
        assertEquals(create.splitView.exchangeView.amountProperty.get().getString(), "0.00");

        create.splitView.credit = new MonetaryAmount(create.splitView.monetaryUnit, 200);
        create.splitPresenter.creditUpdated();
        assertEquals(create.splitView.exchangeView.amountProperty.get().getString(), "144.00");

        create.splitView.credit = null;
        create.splitView.debit = new MonetaryAmount(create.splitView.monetaryUnit, 200);
        create.splitPresenter.debitUpdated();
        assertEquals(create.splitView.exchangeView.amountProperty.get().getString(), "144.00");

        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void editEntryDescription() throws Exception {

        getCurrentSession().beginTransaction();

        SimpleEntryEdit edit = new SimpleEntryEdit(1l, 9);

        assertEquals(
                edit.entryView.getEffectiveOnProperty().get().getTime(),
                new SimpleDateFormat("yyyy-MM-dd").parse("2010-12-21").getTime()
        );

        assertEquals(edit.splitView.description, "Filling up the car");
        assertEquals(edit.splitView.debit.getReportString(false, false, true), "50.00");
        assertEquals(edit.splitView.credit, null);
        assertEquals(edit.accountSelectView.name, "Gas");
        assertEquals(edit.splitView.exchangeFieldsVisible, false);

        edit.splitView.setDescription("Some new description");
        edit.entryPresenter.saveEntry();

        assertEquals(eventBus.events.size(), 1);
        assertEquals(((EntryModified) eventBus.events.get(0)).getLedgerEntry(), edit.entry);

        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();
        Entry storedEntry = entryDAO.getEntry(1l);
        assertEquals(storedEntry.getDescription(), "Some new description");
        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void editEntryNoDescription() throws Exception {

        getCurrentSession().beginTransaction();
        SimpleEntryEdit edit = new SimpleEntryEdit(1l, 9);
        edit.splitView.description = "";
        edit.entryPresenter.saveEntry();
        assertEquals(edit.splitView.descriptionValidationErrorVisible, true);
        assert eventBus.events.size() > 0;
        eventBus.events.clear();
        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();
        Entry storedEntry = entryDAO.getEntry(1l);
        assertEquals(storedEntry.getDescription(), "Filling up the car");
        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();
        edit.splitView.setDescription("Some new description");
        edit.entryPresenter.saveEntry();
        assertEquals(edit.splitView.descriptionValidationErrorVisible, false);
        assertEquals(eventBus.events.size(), 1);
        assertEquals(((EntryModified) eventBus.events.get(0)).getLedgerEntry(), edit.entry);
        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();
        storedEntry = entryDAO.getEntry(1l);
        assertEquals(storedEntry.getDescription(), "Some new description");
        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void editEntryNoAmount() throws Exception {

        getCurrentSession().beginTransaction();
        SimpleEntryEdit edit = new SimpleEntryEdit(1l, 9);
        edit.splitView.debit = new MonetaryAmount(edit.splitView.monetaryUnit);
        edit.splitView.credit = new MonetaryAmount(edit.splitView.monetaryUnit);
        edit.entryPresenter.saveEntry();
        assertEquals(edit.splitView.amountValidationErrorVisible, true);
        assert eventBus.events.size() > 0;
        eventBus.events.clear();
        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();
        Split storedSplit = entryDAO.getSplit(1l);
        assertEquals(storedSplit.getAmount().getString(), "50.00");
        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();
        edit.splitView.debit = new MonetaryAmount(edit.splitView.monetaryUnit, "55");
        edit.entryPresenter.saveEntry();
        assertEquals(edit.splitView.amountValidationErrorVisible, false);
        assertEquals(eventBus.events.size(), 1);
        assertEquals(((EntryModified) eventBus.events.get(0)).getLedgerEntry(), edit.entry);
        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();
        storedSplit = entryDAO.getSplit(1l);
        assertEquals(storedSplit.getAmount().getString(), "55.00");
        getCurrentSession().getTransaction().commit();
    }

}
