package org.fourthline.konto.test.ledger.mock;

import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import org.fourthline.konto.client.ledger.entry.view.EntrySummaryView;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.MonetaryAmount;
import org.seamless.gwt.validation.shared.ValidationError;

/**
 * @author Christian Bauer
 */
public class MockEntrySummaryView implements EntrySummaryView {

    @Override
    public void setPresenter(Presenter presenter) {

    }

    @Override
    public void focus() {

    }

    @Override
    public void setCurrentAccount(Account currentAccount) {

    }

    @Override
    public void setDebit(MonetaryAmount debitAmount) {

    }

    @Override
    public void setCredit(MonetaryAmount creditAmount) {

    }

    @Override
    public void setSplitSuggestionHandler(SelectionHandler<SuggestOracle.Suggestion> handler) {

    }

    @Override
    public void setDescription(String description) {

    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void showValidationErrorDescription(ValidationError error) {

    }

    @Override
    public void clearValidationErrorDescription() {

    }

    @Override
    public Widget asWidget() {
        return null;
    }
}
