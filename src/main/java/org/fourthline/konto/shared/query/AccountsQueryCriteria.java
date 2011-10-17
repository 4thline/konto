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

package org.fourthline.konto.shared.query;

import org.fourthline.konto.shared.AccountType;
import org.seamless.gwt.validation.shared.EntityProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * If the list of identifiers is null or zero size, all accounts of the given type are retrieved!
 *
 * @author Christian Bauer
 */
public class AccountsQueryCriteria extends QueryCriteria {

    protected static final String STRING_REPRESENTATION_PREFIX = "aqc-";

    AccountType type = null;

    public AccountsQueryCriteria() {
    }

    public AccountsQueryCriteria(AccountType type) {
        this.type = type;
    }

    public AccountsQueryCriteria(List<Long> listOfIdentifiers, AccountType type) {
        super(listOfIdentifiers);
        this.type = type;
    }

    public AccountsQueryCriteria(String stringFilter, boolean substringQuery, AccountType type) {
        super(stringFilter, substringQuery);
        this.type = type;
    }

    public AccountsQueryCriteria(EntityProperty orderBy, boolean sortAscending, AccountType type) {
        super(orderBy, sortAscending);
        this.type = type;
    }

    public AccountsQueryCriteria(EntityProperty orderBy, boolean sortAscending, Integer firstResult, Integer maxResults, AccountType type) {
        super(orderBy, sortAscending, firstResult, maxResults);
        this.type = type;
    }

    public AccountsQueryCriteria(EntityProperty orderBy, boolean sortAscending, String stringFilter, boolean substringQuery, Integer firstResult, Integer maxResults, AccountType type) {
        super(orderBy, sortAscending, stringFilter, substringQuery, firstResult, maxResults);
        this.type = type;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public static AccountsQueryCriteria get(AccountsQueryCriteria[] criteria, AccountType type) {
        for (AccountsQueryCriteria crit : criteria) {
            if (type.equals(crit.getType())) return crit;
        }
        return null;
    }

    public static AccountsQueryCriteria[] valueOf(String s) {
        List<AccountsQueryCriteria> list = new ArrayList();
        while (s.indexOf(STRING_REPRESENTATION_PREFIX) != -1) {
            try {
                s = s.substring(s.indexOf(STRING_REPRESENTATION_PREFIX) + STRING_REPRESENTATION_PREFIX.length());
                String as = s.substring(0, s.indexOf(";"));

                String at = as.substring(0, as.indexOf("="));
                AccountType accountType = AccountType.valueOf(at);

                String[] ids = as.substring(as.indexOf("=") + 1).split(",");
                List<Long> identifiers = new ArrayList();
                for (String id : ids) {
                    if (id.length() > 0)
                        identifiers.add(Long.valueOf(id));
                }
                // Use 'null' instead of an empty identifier list, it means "all of that type"
                list.add(new AccountsQueryCriteria(identifiers.size() == 0 ? null : identifiers, accountType));
            } catch (Exception ex) {
                // Ignore
            }
        }
        return list.toArray(new AccountsQueryCriteria[list.size()]);
    }

    public static String toString(AccountsQueryCriteria[] criteria) {
        StringBuilder sb = new StringBuilder();
        for (AccountsQueryCriteria c : criteria) {
            sb.append(c);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        if (type == null) return STRING_REPRESENTATION_PREFIX + "UNKNOWN_TYPE";
        StringBuilder sb = new StringBuilder();
        sb.append(STRING_REPRESENTATION_PREFIX).append(getType().getRootType().name()).append("=");
        List<Long> ids = getListOfIdentifiers();
        if (ids != null) {
            for (Long id : ids) {
                sb.append(id).append(",");
            }
            if (getListOfIdentifiers().size() > 0)
                sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(";");
        return sb.toString();
    }
}
