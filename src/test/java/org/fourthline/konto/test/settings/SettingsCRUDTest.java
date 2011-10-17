package org.fourthline.konto.test.settings;

import org.fourthline.konto.server.dao.AccountDAO;
import org.fourthline.konto.server.dao.SettingsDAO;
import org.fourthline.konto.server.dao.UserDAO;
import org.seamless.util.time.DateFormat;
import org.seamless.util.time.DateRange;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.User;
import org.fourthline.konto.shared.entity.settings.AccountOption;
import org.fourthline.konto.shared.entity.settings.GlobalOption;
import org.fourthline.konto.shared.entity.settings.Settings;
import org.fourthline.konto.test.HibernateTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Christian Bauer
 */
public class SettingsCRUDTest extends HibernateTest {

    @Test
    public void createStoreLoadGlobalSetting() {
        getCurrentSession().beginTransaction();

        SettingsDAO settingsDAO = new SettingsDAO();

        Settings<GlobalOption> settings = settingsDAO.getGlobalOptions(1l);

        assertEquals(settings.getValue(GlobalOption.OPT_DATE_FORMAT), DateFormat.Preset.YYYY_MM_DD_DOT.getDateFormat());
        assertEquals(settings.getValue(GlobalOption.OPT_NEW_ENTRY_SELECT_DAY), Boolean.TRUE);

        // Create
        GlobalOption<Integer> sidebarWidth = settings.getOption(GlobalOption.OPT_SIDEBAR_WIDTH);
        assertEquals(sidebarWidth, null);
        sidebarWidth = new GlobalOption<Integer>(GlobalOption.OPT_SIDEBAR_WIDTH, 1l);
        sidebarWidth.setValue(123);
        settingsDAO.persist(sidebarWidth);

        // Modify
        GlobalOption<DateFormat> dateFormat = settings.getOption(GlobalOption.OPT_DATE_FORMAT);
        dateFormat.setValue(DateFormat.Preset.DD_MM_YYYY_DOT.getDateFormat());

        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();

        settings = settingsDAO.getGlobalOptions(1l);

        assertEquals(settings.getValue(GlobalOption.OPT_DATE_FORMAT), DateFormat.Preset.DD_MM_YYYY_DOT.getDateFormat());
        assertEquals(settings.getValue(GlobalOption.OPT_SIDEBAR_WIDTH), new Integer(123));

        getCurrentSession().getTransaction().commit();

    }

    @Test
    public void storeLoadAccountSettings() {
        getCurrentSession().beginTransaction();

        SettingsDAO settingsDAO = new SettingsDAO();

        Settings<AccountOption> settings = settingsDAO.getAccountOptions(1l, 2l);

        assertEquals(settings.size(), 1);

        AccountOption<DateRange> entriesDateRangeSetting =
                settings.getOption(AccountOption.OPT_ENTRIES_DATE_RANGE);

        assertEquals(
                entriesDateRangeSetting.getValue(),
                DateRange.Preset.LAST_YEAR.getDateRange()
        );

        entriesDateRangeSetting.setValue(DateRange.Preset.YEAR_TO_DATE.getDateRange());

        settingsDAO.persist(entriesDateRangeSetting);

        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();
        settings = settingsDAO.getAccountOptions(1l, 2l);
        assertEquals(
                settings.getValue(AccountOption.OPT_ENTRIES_DATE_RANGE),
                DateRange.Preset.YEAR_TO_DATE.getDateRange()
        );
        settingsDAO.delete(settings.getOption(AccountOption.OPT_ENTRIES_DATE_RANGE));
        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();
        settings = settingsDAO.getAccountOptions(1l, 2l);
        assertEquals(settings.size(), 0);
        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void deleteAccountCascadeSettings() {

        getCurrentSession().beginTransaction();
        AccountDAO accountDAO = new AccountDAO();
        Account account = accountDAO.getAccount(2l);
        accountDAO.delete(account);
        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();
        SettingsDAO settingsDAO = new SettingsDAO();
        Settings<AccountOption> accountSettings = settingsDAO.getAccountOptions(1l, 2l);
        assertEquals(accountSettings.size(), 0);
        getCurrentSession().getTransaction().commit();
    }

    @Test
    public void deleteUserCascadeSettings() {

        getCurrentSession().beginTransaction();
        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUser(1l);
        userDAO.delete(user);
        getCurrentSession().getTransaction().commit();

        getCurrentSession().beginTransaction();
        SettingsDAO settingsDAO = new SettingsDAO();
        Settings<AccountOption> accountSettings = settingsDAO.getAccountOptions(1l, 2l);
        assertEquals(accountSettings.size(), 0);
        getCurrentSession().getTransaction().commit();
    }

}
