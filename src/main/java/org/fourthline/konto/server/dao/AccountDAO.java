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

package org.fourthline.konto.server.dao;

import org.hibernate.Query;
import org.hibernate.transform.ResultTransformer;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.AccountGroup;
import org.fourthline.konto.shared.entity.AccountGroupLink;
import org.fourthline.konto.shared.AccountType;
import org.fourthline.konto.shared.query.AccountsQueryCriteria;
import org.fourthline.konto.shared.entity.AssetAccount;
import org.fourthline.konto.shared.entity.BankAccount;
import org.fourthline.konto.shared.entity.ExpenseAccount;
import org.fourthline.konto.shared.entity.IncomeAccount;
import org.fourthline.konto.shared.entity.LiabilityAccount;
import org.fourthline.konto.shared.entity.MonetaryUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class AccountDAO extends HibernateDAO {

    protected String getAccountEntity(AccountType type) {
        if (type == null) return "Account";
        switch (type) {
            case Asset:
                return AssetAccount.class.getSimpleName();
            case BankAccount:
                return BankAccount.class.getSimpleName();
            case Liability:
                return LiabilityAccount.class.getSimpleName();
            case Income:
                return IncomeAccount.class.getSimpleName();
            case Expense:
                return ExpenseAccount.class.getSimpleName();
        }
        return null;
    }

    public List<AccountGroup> getAccountGroups(AccountType type, String name) {
        return getAccountGroups(type, name, false);
    }

    public List<AccountGroup> getAccountGroups(AccountType type, String name, boolean exactName) {
        StringBuilder sb = new StringBuilder();
        sb.append("select distinct(ag)");
        sb.append(" from ").append(getAccountEntity(type.getRootType())).append(" a,");
        sb.append(" AccountGroupLink agl,");
        sb.append(" AccountGroup ag");
        sb.append(" where agl.accountId = a.id and agl.accountGroupId = ag.id");
        if (name != null) {
            if (exactName) {
                sb.append(" and ag.name = :name");
            } else {
                sb.append(" and lower(ag.name) like :name");
            }
        }
        sb.append(" order by ag.name asc");

        Query q = getCurrentSession().createQuery(sb.toString());

        if (name != null) {
            if (exactName) {
                q.setString("name", name);
            } else {
                q.setString("name", name.toLowerCase() + "%");
            }
        }
        return q.list();
    }

    public Account getAccount(Long id) {
        List<Account> accounts = getAccounts(id);
        return accounts.size() == 1 ? accounts.get(0) : null;
    }

    public List<Account> getAccounts(Long... ids) {
        if (ids == null) return null;
        StringBuilder sb = new StringBuilder();
        sb.append("select a, mu from Account a, MonetaryUnit mu");
        sb.append(" where mu.id = a.monetaryUnitId and a.id in (:ids) order by a.id asc");
        Query q = getCurrentSession().createQuery(sb.toString());
        q.setParameterList("ids", ids);
        q.setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] objects, String[] strings) {
                Account account = (Account) objects[0];
                account.setMonetaryUnit((MonetaryUnit) objects[1]);
                return account;
            }

            @Override
            public List transformList(List list) {
                return list;
            }
        });
        return q.list();
    }

    public List<Account> getAccounts(AccountsQueryCriteria criteria) {

        StringBuilder sb = new StringBuilder();
        sb.append("select a, mu from ");
        sb.append(getAccountEntity(criteria.getType())).append(" a, ");
        sb.append("MonetaryUnit mu");

        sb.append(" where mu.id = a.monetaryUnitId");

        // TODO: SQL IN clause, need to split huge lists of identifiers
        if (!criteria.isListOfIdentifiersEmpty()) {
            sb.append(" and a.id in(:ids)");

        } else if (criteria.getStringFilter() != null) {
            sb.append(" and (lower(a.name) like :nameFilter");
            sb.append(" or lower(a.groupName) like :nameFilter)");
        }

        sb.append(" order by ");
        if (criteria.getOrderBy() != null && !criteria.getOrderBy().equals(Account.Property.name)) {
            if (criteria.getOrderBy().equals(Account.Property.groupName)) {
                sb.append(" lower(a.groupName)");
                sb.append(criteria.isSortAscending() ? " asc" : " desc");
                sb.append(" , lower(a.name)");
                sb.append(criteria.isSortAscending() ? " asc" : " desc");
            }

        } else {
            sb.append(" lower(a.name)");
            sb.append(criteria.isSortAscending() ? " asc" : " desc");
        }

        Query q = getCurrentSession().createQuery(sb.toString());

        if (!criteria.isListOfIdentifiersEmpty()) {
            q.setParameterList("ids", criteria.getListOfIdentifiers());
        } else if (criteria.getStringFilter() != null) {
            q.setString(
                    "nameFilter",
                    (criteria.isSubstringQuery() ? "%" : "") + criteria.getStringFilter().toLowerCase() + "%"
            );
        }

        if (criteria.getMaxResults() != null) {
            q.setMaxResults(criteria.getMaxResults());
        }

        q.setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] objects, String[] strings) {
                Account account = (Account) objects[0];
                account.setMonetaryUnit((MonetaryUnit) objects[1]);
                return account;
            }

            @Override
            public List transformList(List list) {
                return list;
            }
        });

        return q.list();
    }

    public Long persist(Account account) {

        getCurrentSession().saveOrUpdate(account);

        // Remove all existing links between groups and this account
        getCurrentSession().createQuery(
                "delete from AccountGroupLink acl where acl.accountId = :id"
        ).setLong("id", account.getId()).executeUpdate();

        if (account.getGroupName() != null) {

            // Account is in a group, try to find it by name or create it
            List<AccountGroup> groups = getAccountGroups(account.getType(), account.getGroupName(), true);
            AccountGroup group = groups.size() == 1 ? groups.get(0) : new AccountGroup(account.getGroupName());

            if (group.getId() == null) {
                // New group
                getCurrentSession().save(group);
            }

            // Save link between account and group
            getCurrentSession().save(
                    new AccountGroupLink(account.getId(), group.getId())
            );
        }

        // Clean up any groups that have no accounts
        removeEmptyAccountGroups();

        return account.getId();
    }

    public void delete(Account account) {
        getCurrentSession().delete(account);
        removeEmptyAccountGroups();
    }

    protected void removeEmptyAccountGroups() {
        getCurrentSession().createQuery(
                "delete from AccountGroup ag where not ag.id in " +
                        "(select distinct(agl.accountGroupId) from AccountGroupLink agl)"
        ).executeUpdate();

    }

}


