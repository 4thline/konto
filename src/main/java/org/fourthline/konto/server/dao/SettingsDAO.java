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
import org.fourthline.konto.shared.entity.settings.AccountOption;
import org.fourthline.konto.shared.entity.settings.GlobalOption;
import org.fourthline.konto.shared.entity.settings.Option;
import org.fourthline.konto.shared.entity.settings.Settings;

/**
 * @author Christian Bauer
 */
public class SettingsDAO extends HibernateDAO {

    public Settings<GlobalOption> getGlobalOptions(Long userId) {

        Query q = getCurrentSession().createQuery(
                "select go from GlobalOption go where go.userId = :userId"
        ).setLong("userId", userId);

        return new Settings(q.list());
    }

    public Settings<AccountOption> getAccountOptions(Long userId, Long accountId) {

        Query q = getCurrentSession().createQuery(
                "select ao from AccountOption ao where ao.userId = :userId and ao.accountId = :accountId"
        ).setLong("userId", userId).setLong("accountId", accountId);

        return new Settings(q.list());
    }

    public void persist(Option option) {
        getCurrentSession().saveOrUpdate(option);
    }

    public void delete(Option option) {
        getCurrentSession().delete(option);
    }
}
