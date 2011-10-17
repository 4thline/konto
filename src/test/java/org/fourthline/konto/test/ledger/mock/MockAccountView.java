package org.fourthline.konto.test.ledger.mock;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.seamless.gwt.component.client.binding.MockValidatableViewProperty;
import org.seamless.gwt.component.client.binding.ValidatableViewProperty;
import org.seamless.gwt.component.client.suggest.SuggestionSelectView;
import org.fourthline.konto.client.ledger.account.AccountGroupSuggestion;
import org.fourthline.konto.client.ledger.account.view.AccountView;
import org.seamless.util.time.DateFormat;
import org.seamless.gwt.validation.shared.ValidationError;
import org.fourthline.konto.shared.entity.MonetaryUnit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class MockAccountView implements AccountView {

    public Presenter presenter;
    public DateFormat dateFormat;
    public boolean focus = false;
    public MockSuggestionSelectView<AccountGroupSuggestion> accountGroupSelectView;
    public boolean accountGroupValidationErrorVisible = false;
    public boolean createMode = true;
    public MockValidatableViewProperty<String> nameProperty = new MockValidatableViewProperty<String>();
    public MockValidatableViewProperty<Date> effectiveOnProperty = new MockValidatableViewProperty<Date>();
    public MockValidatableViewProperty<BigDecimal> initialBalanceProperty = new MockValidatableViewProperty<BigDecimal>();
    public String currency;
    public List<MonetaryUnit> currencies = new ArrayList();
    public List<IsWidget> formPanelRows = new ArrayList();

    public MockAccountView(MockSuggestionSelectView<AccountGroupSuggestion> accountGroupSelectView) {
        this.accountGroupSelectView = accountGroupSelectView;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public void reset() {
        focus = false;
        accountGroupValidationErrorVisible = false;
        accountGroupSelectView.reset();
        nameProperty.reset();
        effectiveOnProperty.reset();
        initialBalanceProperty.reset();
        currency = null;
        currencies.clear();
        formPanelRows.clear();
    }

    @Override
    public void focus() {
        focus = true;
    }

    @Override
    public SuggestionSelectView<AccountGroupSuggestion> getAccountGroupSelectView() {
        return accountGroupSelectView;
    }

    @Override
    public void showValidationErrorAccountGroup(ValidationError error) {
        accountGroupValidationErrorVisible = true;
    }

    @Override
    public void clearValidationErrorAccountGroup() {
        accountGroupValidationErrorVisible = false;
    }

    @Override
    public void setCreateMode(boolean createMode) {
        this.createMode = createMode;
    }

    @Override
    public ValidatableViewProperty<String> getNameProperty() {
        return nameProperty;
    }

    @Override
    public ValidatableViewProperty<Date> getEffectiveOnProperty() {
        return effectiveOnProperty;
    }

    @Override
    public ValidatableViewProperty<BigDecimal> getInitialBalanceProperty() {
        return initialBalanceProperty;
    }

    @Override
    public void setCurrency(String string) {
        this.currency = string;
    }

    @Override
    public void setCurrencies(List<MonetaryUnit> currencies) {
        this.currencies = currencies;
    }

    @Override
    public void addFormPanelRow(IsWidget widget) {
        formPanelRows.add(widget);
    }

    @Override
    public void removeFormPanelRow(IsWidget widget) {
        formPanelRows.remove(widget);
    }

    @Override
    public Widget asWidget() {
        return null;
    }
}
