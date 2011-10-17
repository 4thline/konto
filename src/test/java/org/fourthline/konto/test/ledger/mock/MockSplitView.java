package org.fourthline.konto.test.ledger.mock;

import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import org.seamless.gwt.component.client.suggest.SuggestionSelectView;
import org.fourthline.konto.client.ledger.entry.AccountSuggestion;
import org.fourthline.konto.client.ledger.entry.view.ExchangeView;
import org.fourthline.konto.client.ledger.entry.view.SplitView;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.seamless.gwt.validation.shared.ValidationError;

/**
 * @author Christian Bauer
 */
public class MockSplitView implements SplitView {

    public Presenter presenter;
    public boolean focus = false;
    public boolean switchEnabled = false;
    public Account currentAccount;
    public MockSuggestionSelectView<AccountSuggestion> accountSelectView;
    public boolean exchangeFieldsVisible = false;
    public boolean amountValidationErrorVisible = false;
    public boolean accountValidationErrorVisible = false;
    public MonetaryUnit monetaryUnit;
    public MonetaryAmount debit;
    public MonetaryAmount credit;
    public SelectionHandler<SuggestOracle.Suggestion> splitSuggestionHandler;
    public String description;
    public boolean descriptionValidationErrorVisible = false;
    public MockExchangeView exchangeView;

    public MockSplitView(MockSuggestionSelectView<AccountSuggestion> accountSelectView, MockExchangeView exchangeView) {
        this.accountSelectView = accountSelectView;
        this.exchangeView = exchangeView;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void focus() {
        focus = true;
    }

    @Override
    public void enableSwitch(boolean enabled) {
        switchEnabled = enabled;
    }

    @Override
    public void setCurrentAccount(Account currentAccount) {
        this.currentAccount = currentAccount;
    }

    @Override
    public SuggestionSelectView<AccountSuggestion> getAccountSelectView() {
        return accountSelectView;
    }

    @Override
    public ExchangeView getExchangeView() {
        return exchangeView;
    }

    @Override
    public void showExchangeView(boolean show) {
        exchangeFieldsVisible = show;
    }

    @Override
    public void showValidationErrorAmount(ValidationError error) {
        amountValidationErrorVisible = true;
    }

    @Override
    public void clearValidationErrorAmount() {
        amountValidationErrorVisible = false;
    }

    @Override
    public void showValidationErrorAccount(ValidationError error) {
        accountValidationErrorVisible = true;
    }

    @Override
    public void clearValidationErrorAccount() {
        accountValidationErrorVisible = false;
    }

    @Override
    public MonetaryUnit getMonetaryUnit() {
        return monetaryUnit;
    }

    @Override
    public void setMonetaryUnit(MonetaryUnit unit) {
        this.monetaryUnit = unit;
    }

    @Override
    public MonetaryAmount getDebit() {
        return debit;
    }

    @Override
    public void setDebit(MonetaryAmount amount) {
        if (amount == null || amount.getValue().signum() == 0) amount = null;
        this.debit = amount;
    }

    @Override
    public MonetaryAmount getCredit() {
        return credit;
    }

    @Override
    public void setCredit(MonetaryAmount amount) {
        if (amount == null || amount.getValue().signum() == 0) amount = null;
        this.credit = amount;
    }

    @Override
    public void setSplitSuggestionHandler(SelectionHandler<SuggestOracle.Suggestion> handler) {
        this.splitSuggestionHandler = handler;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void showValidationErrorDescription(ValidationError error) {
        this.descriptionValidationErrorVisible = true;
    }

    @Override
    public void clearValidationErrorDescription() {
        this.descriptionValidationErrorVisible = false;
    }

    @Override
    public Widget asWidget() {
        return null;
    }
}
