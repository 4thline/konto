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

package org.fourthline.konto.shared.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "ACCOUNT_GROUP_LINK")
public class AccountGroupLink implements Serializable {

    @Id
    @Column(name = "ACCOUNT_ID", unique = true)
    private Long accountId;

    @Id
    @Column(name = "ACCOUNT_GROUP_ID")
    private Long accountGroupId;

    public AccountGroupLink() {
    }

    public AccountGroupLink(Long accountId, Long accountGroupId) {
        this.accountId = accountId;
        this.accountGroupId = accountGroupId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getAccountGroupId() {
        return accountGroupId;
    }

    public void setAccountGroupId(Long accountGroupId) {
        this.accountGroupId = accountGroupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountGroupLink that = (AccountGroupLink) o;

        if (!accountGroupId.equals(that.accountGroupId)) return false;
        if (!accountId.equals(that.accountId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = accountId.hashCode();
        result = 31 * result + accountGroupId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getAccountId() + ", " + getAccountGroupId();
    }
}
