package org.fourthline.konto.test.ledger.mock;

import com.google.gwt.user.client.ui.Widget;
import org.seamless.gwt.component.client.binding.ValidatableViewProperty;
import org.fourthline.konto.client.ledger.entry.view.ExchangeView;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.MonetaryAmount;
import org.seamless.gwt.component.client.binding.MockValidatableViewProperty;

import java.math.BigDecimal;

/**
 * @author Christian Bauer
 */
public class MockExchangeView implements ExchangeView {

    public Presenter presenter;
    public CurrencyPair currencyPair;
    public MockValidatableViewProperty<BigDecimal> rateProperty = new MockValidatableViewProperty<BigDecimal>();
    public MockValidatableViewProperty<MonetaryAmount> amountProperty = new MockValidatableViewProperty<MonetaryAmount>();

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void reset(CurrencyPair pair, MonetaryAmount amount) {
        this.currencyPair = pair;
        rateProperty.set(pair.getExchangeRate());
        amountProperty.set(amount);
    }

    @Override
    public ValidatableViewProperty<BigDecimal> getRateProperty() {
        return rateProperty;
    }

    @Override
    public ValidatableViewProperty<MonetaryAmount> getAmountProperty() {
        return amountProperty;
    }

    @Override
    public Widget asWidget() {
        return null;
    }
}
