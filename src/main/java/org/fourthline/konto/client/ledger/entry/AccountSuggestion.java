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

package org.fourthline.konto.client.ledger.entry;

import org.seamless.gwt.component.client.suggest.Suggestion;
import org.fourthline.konto.shared.entity.Account;

/**
 * @author Christian Bauer
 */
public class AccountSuggestion implements Suggestion {

    String label;
    Account account;

    public AccountSuggestion(Account account) {
        this(account.getLabel(true, false, true, true), account);
    }

    public AccountSuggestion(String label, Account account) {
        this.label = label;
        this.account = account;
    }

    public String getLabel() {
        return label;
    }

    public Account getAccount() {
        return account;
    }

    public String getDisplayString() {
        return label;
    }

}
