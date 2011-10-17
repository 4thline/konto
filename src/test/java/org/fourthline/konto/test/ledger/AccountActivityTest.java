package org.fourthline.konto.test.ledger;

import com.google.inject.Provider;
import org.fourthline.konto.client.ledger.account.AccountActivity;
import org.fourthline.konto.client.ledger.account.AccountGroupSuggestion;
import org.fourthline.konto.client.ledger.account.AccountPlace;
import org.fourthline.konto.client.ledger.account.BankAccountPresenter;
import org.fourthline.konto.client.settings.GlobalSettings;
import org.fourthline.konto.shared.AccountType;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.AccountGroup;
import org.fourthline.konto.shared.entity.BankAccount;
import org.fourthline.konto.shared.Constants;
import org.fourthline.konto.test.IntegrationTest;
import org.fourthline.konto.test.ledger.mock.MockAccountView;
import org.fourthline.konto.test.ledger.mock.MockBankAccountView;
import org.fourthline.konto.test.ledger.mock.MockSuggestionSelectView;
import org.seamless.mock.gwt.MockAcceptsOneWidget;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author Christian Bauer
 */
public class AccountActivityTest extends IntegrationTest {

    protected MockBankAccountView bankAccountView = new MockBankAccountView();
    protected final BankAccountPresenter bankAccountPresenter = new BankAccountPresenter(bankAccountView);
    protected Provider<BankAccountPresenter> bankAccountPresenterProvider = new
            Provider<BankAccountPresenter>() {
                @Override
                public BankAccountPresenter get() {
                    return bankAccountPresenter;
                }
            };

    protected MockSuggestionSelectView<AccountGroupSuggestion> groupSelectView =
            new MockSuggestionSelectView<AccountGroupSuggestion>();

    protected MockAccountView accountView =
            new MockAccountView(groupSelectView);


    protected AccountActivity createAccountActivity() {
        return new AccountActivity(accountView,
                                   bankAccountPresenterProvider,
                                   placeController,
                                   eventBus,
                                   ledgerServiceAsync,
                                   currencyServiceAsync,
                                   new GlobalSettings(settingsServiceAsync, eventBus)
        );
    }

    @Test
    public void createBankAccount() throws Exception {
        getCurrentSession().beginTransaction();

        AccountActivity accountActivity = createAccountActivity();
        accountActivity.init(new AccountPlace());
        accountActivity.start(new MockAcceptsOneWidget(), eventBus);

        assertEquals(accountView.currencies.size(), 5);
        assertEquals(accountView.currency, Constants.SYSTEM_BASE_CURRENCY_CODE);
        assert accountView.getEffectiveOnProperty().get() != null;
        assertEquals(accountView.getInitialBalanceProperty().get().toString(), new BigDecimal("0").toString());
        assertEquals(accountView.createMode, true);
        assertEquals(accountView.focus, true);

        accountActivity.typeSelected(AccountType.BankAccount);

        assertEquals(accountView.formPanelRows.get(0), bankAccountView);

        accountView.nameProperty.set("Some Bankaccount");
        bankAccountView.bankNameProperty.set("My Bank");

        accountActivity.save();

        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();

        BankAccount acc = (BankAccount) accountDAO.getAccount(accountActivity.getAccount().getId());
        assertEquals(acc.getName(), "Some Bankaccount");
        assertEquals(acc.getGroupId(), null);
        assertEquals(acc.getGroupName(), null);
        assertEquals(acc.getInitialBalance().getReportString(false, false, true), "");
        assertEquals(acc.getMonetaryUnit().getCurrencyCode(), "CHF");
        assertEquals(acc.getBankName(), "My Bank");
        assertEquals(acc.getNumber(), null);
        assertEquals(acc.getRouting(), null);

        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void createAccountCreateGroup() throws Exception {
        getCurrentSession().beginTransaction();

        AccountActivity accountActivity = createAccountActivity();
        accountActivity.init(new AccountPlace());
        accountActivity.start(new MockAcceptsOneWidget(), eventBus);

        accountActivity.typeSelected(AccountType.Expense);

        accountView.nameProperty.set("Some Account");

        groupSelectView.presenter.nameEntered("Some Group");

        accountActivity.save();

        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();

        Account acc = accountDAO.getAccount(accountActivity.getAccount().getId());
        assert acc.getGroupId() != null;
        assertEquals(acc.getGroupName(), "Some Group");

        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void createAccountExistingGroup() throws Exception {
        getCurrentSession().beginTransaction();

        AccountActivity accountActivity = createAccountActivity();
        accountActivity.init(new AccountPlace());
        accountActivity.start(new MockAcceptsOneWidget(), eventBus);

        accountActivity.typeSelected(AccountType.BankAccount);

        accountView.nameProperty.set("Some Account");

        groupSelectView.presenter.nameEntered("Automobile");

        accountActivity.save();

        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();

        Account acc = accountDAO.getAccount(accountActivity.getAccount().getId());
        assert acc.getGroupId() != null;
        assertEquals(acc.getGroupName(), "Automobile");

        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void editAccount() throws Exception {
        getCurrentSession().beginTransaction();

        AccountActivity accountActivity = createAccountActivity();
        accountActivity.init(new AccountPlace(3l));
        accountActivity.start(new MockAcceptsOneWidget(), eventBus);

        accountView.nameProperty.set("Gasoline");

        accountActivity.save();

        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();

        Account acc = accountDAO.getAccount(accountActivity.getAccount().getId());
        assertEquals(acc.getName(), "Gasoline");
        assert acc.getGroupId() != null;
        assertEquals(acc.getGroupName(), "Automobile");

        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void editAccountNewGroup() throws Exception {
        getCurrentSession().beginTransaction();

        AccountActivity accountActivity = createAccountActivity();
        accountActivity.init(new AccountPlace(3l));
        accountActivity.start(new MockAcceptsOneWidget(), eventBus);

        accountView.nameProperty.set("Gasoline");
        groupSelectView.presenter.nameEntered("Car");

        accountActivity.save();

        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();

        Account acc = accountDAO.getAccount(accountActivity.getAccount().getId());
        assertEquals(acc.getName(), "Gasoline");
        assert acc.getGroupId() != null;
        assertEquals(acc.getGroupName(), "Car");

        List<AccountGroup> groups =  accountDAO.getAccountGroups(AccountType.Expense, "Automobile", true);
        assertEquals(groups.size(), 1);

        groups =  accountDAO.getAccountGroups(AccountType.Expense, "Car", true);
        assertEquals(groups.size(), 1);

        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void editAccountMoveTwoNewGroup() throws Exception {

        getCurrentSession().beginTransaction();
        AccountActivity accountActivity = createAccountActivity();
        accountActivity.init(new AccountPlace(3l));
        accountActivity.start(new MockAcceptsOneWidget(), eventBus);
        groupSelectView.presenter.nameEntered("Car");
        accountActivity.save();
        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();
        accountActivity = createAccountActivity();
        accountActivity.init(new AccountPlace(4l));
        accountActivity.start(new MockAcceptsOneWidget(), eventBus);
        groupSelectView.presenter.nameEntered("Car");
        accountActivity.save();
        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();

        List<AccountGroup> groups =  accountDAO.getAccountGroups(AccountType.Expense, "Automobile", true);
        assertEquals(groups.size(), 0);

        groups =  accountDAO.getAccountGroups(AccountType.Expense, "Car", true);
        assertEquals(groups.size(), 1);

        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void createAccountMixedGroup() throws Exception {
        getCurrentSession().beginTransaction();
        AccountActivity accountActivity = createAccountActivity();
        accountActivity.init(new AccountPlace());
        accountActivity.start(new MockAcceptsOneWidget(), eventBus);
        accountActivity.typeSelected(AccountType.Asset);
        accountView.nameProperty.set("Some Asset");
        groupSelectView.presenter.nameEntered("Some Group");
        accountActivity.save();
        getCurrentSession().getTransaction().commit();
        Long firstId = accountActivity.getAccount().getId();

        // Different sub-type, same group!
        getCurrentSession().beginTransaction();
        accountActivity = createAccountActivity();
        accountActivity.init(new AccountPlace());
        accountActivity.start(new MockAcceptsOneWidget(), eventBus);
        accountActivity.typeSelected(AccountType.BankAccount);
        accountView.nameProperty.set("Some (Bank) Asset");
        groupSelectView.presenter.nameEntered("Some Group");
        accountActivity.save();
        getCurrentSession().getTransaction().commit();
        Long secondId = accountActivity.getAccount().getId();

        getCurrentSession().beginTransaction();
        assertEquals(accountDAO.getAccount(firstId).getGroupName(), "Some Group");
        assertEquals(accountDAO.getAccount(secondId).getGroupName(), "Some Group");
        getCurrentSession().getTransaction().commit();
    }

}
