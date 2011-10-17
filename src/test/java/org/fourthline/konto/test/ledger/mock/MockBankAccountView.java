package org.fourthline.konto.test.ledger.mock;

import com.google.gwt.user.client.ui.Widget;
import org.seamless.gwt.component.client.binding.MockValidatableViewProperty;
import org.seamless.gwt.component.client.binding.ValidatableViewProperty;
import org.fourthline.konto.client.ledger.account.view.BankAccountView;

/**
 * @author Christian Bauer
 */
public class MockBankAccountView implements BankAccountView {

    public MockValidatableViewProperty<String> bankNameProperty = new MockValidatableViewProperty<String>();
    public MockValidatableViewProperty<String> numberProperty = new MockValidatableViewProperty<String>();
    public MockValidatableViewProperty<String> routingProperty = new MockValidatableViewProperty<String>();

    @Override
    public ValidatableViewProperty<String> getBankNameProperty() {
        return bankNameProperty;
    }

    @Override
    public ValidatableViewProperty<String> getNumberProperty() {
        return numberProperty;
    }

    @Override
    public ValidatableViewProperty<String> getRoutingProperty() {
        return routingProperty;
    }

    @Override
    public Widget asWidget() {
        return null;
    }
}
