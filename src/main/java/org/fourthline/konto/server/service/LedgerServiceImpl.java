/*
 * Copyright (C) 2011 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.fourthline.konto.server.service;

import org.fourthline.konto.client.service.LedgerService;
import org.fourthline.konto.server.dao.AccountDAO;
import org.fourthline.konto.server.dao.EntryDAO;
import org.fourthline.konto.server.dao.SettingsDAO;
import org.fourthline.konto.shared.AccountType;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.AccountGroup;
import org.fourthline.konto.shared.entity.Entry;
import org.fourthline.konto.shared.entity.Split;
import org.fourthline.konto.shared.entity.settings.AccountOption;
import org.fourthline.konto.shared.entity.settings.Settings;
import org.fourthline.konto.shared.query.AccountsQueryCriteria;
import org.fourthline.konto.shared.query.LedgerLinesQueryCriteria;
import org.fourthline.konto.shared.result.LedgerLines;
import org.seamless.gwt.server.HibernateRemoteServiceServlet;
import org.seamless.gwt.validation.shared.Validatable;
import org.seamless.gwt.validation.shared.ValidationError;
import org.seamless.gwt.validation.shared.ValidationException;
import org.seamless.util.time.DateRange;

import java.util.List;

/**
 * @author Christian Bauer
 */
public class LedgerServiceImpl extends HibernateRemoteServiceServlet implements LedgerService {

    @Override
    public List<AccountGroup> getAccountGroups(AccountType type, String name) {
        AccountDAO dao = new AccountDAO();
        return dao.getAccountGroups(type, name);
    }

    @Override
    public Account getAccount(Long id) {
        AccountDAO dao = new AccountDAO();
        return dao.getAccount(id);
    }

    @Override
    public List<Account> getAccounts(AccountsQueryCriteria criteria) {
        AccountDAO dao = new AccountDAO();
        return dao.getAccounts(criteria);
    }

    @Override
    public Long store(Account account) throws ValidationException {
        List<ValidationError> errors = account.validate(Validatable.GROUP_SERVER);
        if (errors.size() > 0)
            throw new ValidationException(
                    "Can't persist account, validation errors.", errors
            );

        AccountDAO dao = new AccountDAO();
        return dao.persist(account);
    }

    @Override
    public void remove(Account account) {
        AccountDAO accountDAO = new AccountDAO();
        accountDAO.delete(account);
        EntryDAO entryDAO = new EntryDAO();
        entryDAO.deleteOrphanedEntries();
    }

    @Override
    public LedgerLines getLedgerLines(LedgerLinesQueryCriteria criteria) {
        EntryDAO entryDAO = new EntryDAO();
        AccountDAO accountDAO = new AccountDAO();
        SettingsDAO settingsDAO = new SettingsDAO();

        Account account = accountDAO.getAccount(criteria.getAccountId());
        if (account == null) return null;

        // Get the stored setting for the date range
        Settings<AccountOption> accountSettings = settingsDAO.getAccountOptions(1l, account.getId());
        AccountOption<DateRange> storedRangeSetting =
                accountSettings.getOption(AccountOption.OPT_ENTRIES_DATE_RANGE);

        boolean storeRangeSetting = true;

        // The users wants to see the given entry, so make sure it's in the visible range of ledger lines
        if (criteria.getSelectEntryId() != null) {

            Entry entry = entryDAO.getEntry(criteria.getSelectEntryId());
            entryDAO.populateSplits(entry);
            if (entry == null) return null;

            // Check that the entry or one of its splits is related to the given account
            boolean splitOfAccount = false;
            for (Split split : entry.getSplits()) {
                if (split.getAccountId().equals(account.getId())) {
                    splitOfAccount = true;
                    break;
                }
            }
            if (!entry.getAccountId().equals(account.getId()) && !splitOfAccount) {
                return null;
            }

            // Use a temporary view of the entry's month if the date wasn't in the visible range
            if (storedRangeSetting != null && !storedRangeSetting.getValue().isInRange(entry.getEffectiveOn())) {
                criteria.setEffectiveOn(DateRange.getMonthOf(entry.getEffectiveOn()));
                storeRangeSetting = false;
            } else if (criteria.getEffectiveOn() != null && !criteria.getEffectiveOn().isInRange(entry.getEffectiveOn())) {
                criteria.setEffectiveOn(DateRange.getMonthOf(entry.getEffectiveOn()));
                storeRangeSetting = false;
            }
        }

        if (storedRangeSetting != null && criteria.getEffectiveOn() == null) {

            // No selection by user, have stored setting
            criteria.setEffectiveOn(storedRangeSetting.getValue());

        } else if (criteria.getEffectiveOn() != null) {
            // User has made a selection

            if (storedRangeSetting != null && !criteria.getEffectiveOn().hasStartOrEnd()) {

                // Unlimited range, remove the stored setting
                settingsDAO.delete(storedRangeSetting);

            } else if (storeRangeSetting) {
                // Store users selection
                if (storedRangeSetting == null) {
                    // If there is no stored setting, create an empty one
                    storedRangeSetting =
                            new AccountOption<DateRange>(
                                    AccountOption.OPT_ENTRIES_DATE_RANGE,
                                    1l,
                                    account.getId()
                            );
                }
                storedRangeSetting.setValue(criteria.getEffectiveOn());
                settingsDAO.persist(storedRangeSetting);
            }
        }

        return entryDAO.getLedgerLines(account, criteria);
    }

    @Override
    public Entry populateSplits(Entry entry) {
        if (entry == null) return null;
        EntryDAO entryDAO = new EntryDAO();
        entryDAO.populateSplits(entry);
        return entry;
    }

    @Override
    public void store(Entry entry) throws ValidationException {
        List<ValidationError> errors = entry.validate(Validatable.GROUP_SERVER);
        if (errors.size() > 0)
            throw new ValidationException(
                    "Can't persist ledger entry, validation errors.", errors
            );

        EntryDAO dao = new EntryDAO();
        dao.persist(entry);
    }

    @Override
    public void remove(Entry entry) {
        EntryDAO dao = new EntryDAO();
        dao.delete(entry);
    }
}

