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

import org.fourthline.konto.client.service.SettingsService;
import org.seamless.gwt.server.HibernateRemoteServiceServlet;
import org.fourthline.konto.server.dao.SettingsDAO;
import org.seamless.gwt.validation.shared.ValidationException;
import org.fourthline.konto.shared.entity.settings.AccountOption;
import org.fourthline.konto.shared.entity.settings.GlobalOption;
import org.fourthline.konto.shared.entity.settings.Option;
import org.fourthline.konto.shared.entity.settings.Settings;

/**
 * TODO: Multiuser support
 *
 * @author Christian Bauer
 */
public class SettingsServiceImpl extends HibernateRemoteServiceServlet implements SettingsService {

    public static final long DEFAULT_USER_ID = 1l;

    @Override
    public Settings<GlobalOption> getGlobalSettings() {
        SettingsDAO dao = new SettingsDAO();
        return dao.getGlobalOptions(DEFAULT_USER_ID);
    }

    @Override
    public Settings<AccountOption> getAccountSettings(Long accountId) {
        SettingsDAO dao = new SettingsDAO();
        return dao.getAccountOptions(DEFAULT_USER_ID, accountId);
    }

    @Override
    public void store(Settings<Option> settings) throws ValidationException {
        SettingsDAO dao = new SettingsDAO();
        for (Option option : settings) {
            option.setUserId(DEFAULT_USER_ID); // Security! Override whatever the client sent
            dao.persist(option);
        }
    }

    @Override
    public void remove(Settings<Option> settings) {
        SettingsDAO dao = new SettingsDAO();
        for (Option option : settings) {
            option.setUserId(DEFAULT_USER_ID); // Security! Override whatever the client sent
            dao.delete(option);
        }
    }
}
