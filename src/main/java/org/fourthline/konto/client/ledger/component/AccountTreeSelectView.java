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

package org.fourthline.konto.client.ledger.component;

import com.google.gwt.user.client.ui.IsWidget;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.query.AccountsQueryCriteria;

import java.util.List;

/**
 * @author Christian Bauer
 */
public interface AccountTreeSelectView extends IsWidget {

    public enum Option {
        NEW_BUTTON,                 // Show the "New Account" button
        LABEL_FILTER,               // Show the filter input box
        MULTISELECT,                // Allow multiple account selection
        SELECT_ALL,                 // Select all accounts immediately
        SELECT_NONE,                // Deselect all accounts immediately
        HIDE_ASSET,                 // Hide certain types of accounts in the display and from selection
        HIDE_LIABILITY,
        HIDE_INCOME,
        HIDE_EXPENSE;

        public boolean in(Option[] haystack) {
            if (haystack == null) return false;
            for (Option o : haystack) {
                if (o.equals(this)) return true;
            }
            return false;
        }

        public static boolean equals(Option[] setA, Option[] setB) {
            if (setA == null || setB == null || setA.length != setB.length) return false;
            for (Option a : setA)
                if (!a.in(setB)) return false;
            return true;
        }
    }

    public interface Presenter {

        void onNewAccount();

        // Is null when filter has to be cleared
        void onFilter(String filter);

        void onSingleSelectionChange(Account selectedAccount);

        void onMultiSelectionChange(AccountsQueryCriteria[] selection);

    }

    void setPresenter(Presenter presenter);

    void setAccounts(List<Account> value, Option... options);

    void setSelectedAccounts(AccountsQueryCriteria[] selection);

    AccountsQueryCriteria[] getSelectedAccounts();

}
