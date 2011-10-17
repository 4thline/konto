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

package org.fourthline.konto.shared.entity.settings;

import org.seamless.util.time.DateRange;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "ACCOUNT_OPTION")
public class AccountOption<V> extends Option<V> {

    public static final Domain<DateRange> OPT_ENTRIES_DATE_RANGE =
            new Domain("ENTRIES_DATE_RANGE", new DateRangeOptionDatatype());

    protected static final Map<String, OptionDatatype> DATATYPES = new HashMap<String, OptionDatatype>() {{
        put(OPT_ENTRIES_DATE_RANGE.getName(), OPT_ENTRIES_DATE_RANGE.getDatatype());
    }};

    @Id
    @Column(name = "ACCOUNT_ID")
    protected Long accountId;

    public AccountOption() {
        super(DATATYPES);
    }

    public AccountOption(Domain<V> domain, Long userId, Long accountId) {
        this(domain.getName(), userId, accountId);
    }

    public AccountOption(String name, Long userId, Long accountId) {
        super(name, userId, DATATYPES);
        this.accountId = accountId;
    }

    public Long getAccountId() {
        return accountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AccountOption that = (AccountOption) o;

        if (accountId != null ? !accountId.equals(that.accountId) : that.accountId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return super.toString() + ", Account ID: " + getAccountId();
    }
}
