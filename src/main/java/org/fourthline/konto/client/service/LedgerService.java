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

package org.fourthline.konto.client.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.fourthline.konto.shared.AccountType;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.AccountGroup;
import org.fourthline.konto.shared.query.AccountsQueryCriteria;
import org.fourthline.konto.shared.entity.Entry;
import org.fourthline.konto.shared.result.LedgerLines;
import org.fourthline.konto.shared.query.LedgerLinesQueryCriteria;
import org.seamless.gwt.validation.shared.ValidationException;

import java.util.List;

@RemoteServiceRelativePath("ledger")
public interface LedgerService extends RemoteService {

    List<AccountGroup> getAccountGroups(AccountType type, String name);

    Account getAccount(Long id);

    List<Account> getAccounts(AccountsQueryCriteria criteria);

    Long store(Account account) throws ValidationException;

    void remove(Account account);

    LedgerLines getLedgerLines(LedgerLinesQueryCriteria criteria);

    Entry populateSplits(Entry entry);

    void store(Entry entry) throws ValidationException;

    void remove(Entry entry);

}
