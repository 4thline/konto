package org.fourthline.konto.test.ledger;

import org.fourthline.konto.server.dao.AccountDAO;
import org.fourthline.konto.shared.AccountType;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.AccountGroup;
import org.fourthline.konto.shared.query.AccountsQueryCriteria;
import org.fourthline.konto.test.HibernateTest;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;


/**
 * @author Christian Bauer
 */
public class AccountTest extends HibernateTest {

    @Test
    public void loadAccountGroups() {
        getCurrentSession().beginTransaction();

        AccountDAO dao = new AccountDAO();

        List<AccountGroup> assetGroups = dao.getAccountGroups(AccountType.Asset, null);

        assertEquals(assetGroups.size(), 2);
        assertEquals(assetGroups.get(0).getName(), "Bank Accounts");
        assertEquals(assetGroups.get(1).getName(), "More Bank Accounts");

        assetGroups = dao.getAccountGroups(AccountType.Asset, "ba");
        assertEquals(assetGroups.size(), 1);
        assertEquals(assetGroups.get(0).getName(), "Bank Accounts");

        assetGroups = dao.getAccountGroups(AccountType.Asset, "foo");
        assertEquals(assetGroups.size(), 0);

        List<AccountGroup> liabilityGroups = dao.getAccountGroups(AccountType.Liability, null);
        assertEquals(liabilityGroups.size(), 0);

        List<AccountGroup> incomeGroups = dao.getAccountGroups(AccountType.Income, null);
        assertEquals(incomeGroups.size(), 0);

        List<AccountGroup> expenseGroups = dao.getAccountGroups(AccountType.Expense, null);
        assertEquals(expenseGroups.size(), 1);
        assertEquals(expenseGroups.get(0).getName(), "Automobile");

        getCurrentSession().getTransaction().commit();

    }

    @Test
    public void loadAccount() {
        getCurrentSession().beginTransaction();

        AccountDAO dao = new AccountDAO();

        Account account = dao.getAccount(1l);

        assertEquals(account.getName(), "My USD account");
        assertEquals(account.getGroupName(), "Bank Accounts");
        assertEquals(account.getMonetaryUnit().getCurrencyCode(), "USD");
    }

    @Test
    public void loadAccounts() {
        getCurrentSession().beginTransaction();

        AccountDAO dao = new AccountDAO();

        AccountsQueryCriteria criteria = new AccountsQueryCriteria();

        criteria.setSortAscending(true);
        criteria.setOrderBy(Account.Property.groupName);

        List<Account> accounts = dao.getAccounts(criteria);

        assertEquals(accounts.size(), 13);

        assertEquals(accounts.get(0).getName(), "Cash in Wallet");
        assertEquals(accounts.get(0).getGroupName(), null);
        assertEquals(accounts.get(1).getName(), "Entertainment");
        assertEquals(accounts.get(1).getGroupName(), null);
        assertEquals(accounts.get(2).getName(), "Groceries");
        assertEquals(accounts.get(2).getGroupName(), null);
        assertEquals(accounts.get(3).getName(), "Mortgage");
        assertEquals(accounts.get(3).getGroupName(), null);
        assertEquals(accounts.get(4).getName(), "Mortgage Interest");
        assertEquals(accounts.get(4).getGroupName(), null);
        assertEquals(accounts.get(5).getName(), "My House");
        assertEquals(accounts.get(5).getGroupName(), null);
        assertEquals(accounts.get(6).getName(), "Phone/Internet");
        assertEquals(accounts.get(6).getGroupName(), null);
        assertEquals(accounts.get(7).getName(), "Salary");
        assertEquals(accounts.get(7).getGroupName(), null);
        assertEquals(accounts.get(8).getName(), "Gas");
        assertEquals(accounts.get(8).getGroupName(), "Automobile");
        assertEquals(accounts.get(9).getName(), "Maintenance");
        assertEquals(accounts.get(9).getGroupName(), "Automobile");
        assertEquals(accounts.get(10).getName(), "My CHF account");
        assertEquals(accounts.get(10).getGroupName(), "Bank Accounts");

        criteria.setType(AccountType.Asset);
        accounts = dao.getAccounts(criteria);

        assertEquals(accounts.size(), 5);

        assertEquals(accounts.get(0).getName(), "Cash in Wallet");
        assertEquals(accounts.get(0).getGroupName(), null);
        assertEquals(accounts.get(1).getName(), "My House");
        assertEquals(accounts.get(1).getGroupName(), null);
        assertEquals(accounts.get(2).getName(), "My CHF account");
        assertEquals(accounts.get(2).getGroupName(), "Bank Accounts");
        assertEquals(accounts.get(3).getName(), "My USD account");
        assertEquals(accounts.get(3).getGroupName(), "Bank Accounts");
        assertEquals(accounts.get(4).getName(), "My other account");
        assertEquals(accounts.get(4).getGroupName(), "More Bank Accounts");

        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void loadAccountsFiltered() {
        getCurrentSession().beginTransaction();

        AccountDAO dao = new AccountDAO();

        AccountsQueryCriteria criteria = new AccountsQueryCriteria(
                "gage", true, null
        );

        List<Account> accounts = dao.getAccounts(criteria);

        assertEquals(accounts.size(), 2);

        assertEquals(accounts.get(0).getName(), "Mortgage Interest");
        assertEquals(accounts.get(0).getMonetaryUnit().getCurrencyCode(), "USD");
        assertEquals(accounts.get(1).getName(), "Mortgage");
        assertEquals(accounts.get(1).getMonetaryUnit().getCurrencyCode(), "USD");
        assertEquals(accounts.get(1).getInitialBalance().getString(), "-30000.00");
    }
}
