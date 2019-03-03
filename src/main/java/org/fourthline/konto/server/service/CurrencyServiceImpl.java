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

import org.fourthline.konto.client.service.CurrencyService;
import org.fourthline.konto.server.dao.CurrencyDAO;
import org.fourthline.konto.server.download.CurrencyDownloader;
import org.fourthline.konto.server.download.ExchangeRatesAPIDownloader;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.seamless.gwt.server.HibernateRemoteServiceServlet;
import org.seamless.gwt.validation.shared.Validatable;
import org.seamless.gwt.validation.shared.ValidationError;
import org.seamless.gwt.validation.shared.ValidationException;

import java.util.Date;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class CurrencyServiceImpl extends HibernateRemoteServiceServlet implements CurrencyService {

    @Override
    public List<MonetaryUnit> getMonetaryUnits() {
        CurrencyDAO dao = new CurrencyDAO();
        return dao.getMonetaryUnits();
    }

    @Override
    public void store(MonetaryUnit unit) throws ValidationException {
        List<ValidationError> errors = unit.validate(Validatable.GROUP_SERVER);
        if (errors.size() > 0)
            throw new ValidationException(
                    "Can't persist monetary unit, validation errors.", errors
            );

        CurrencyDAO currencyDAO = new CurrencyDAO();
        currencyDAO.persist(unit);
    }

    @Override
    public boolean remove(MonetaryUnit unit) {
        CurrencyDAO currencyDAO = new CurrencyDAO();
        return currencyDAO.delete(unit);
    }

    @Override
    public CurrencyPair getCurrencyPair(MonetaryUnit fromUnit, MonetaryUnit toUnit, Date forDay) {
        return new DefaultCurrencyProvider(new CurrencyDAO())
                .getCurrencyPair(fromUnit, toUnit, forDay);
    }


    @Override
    public List<CurrencyPair> getCurrencyPairs(MonetaryUnit fromUnit, MonetaryUnit toUnit) {
        CurrencyDAO dao = new CurrencyDAO();
        return dao.getCurrencyPairs(fromUnit.getCurrencyCode(), toUnit.getCurrencyCode());
    }

    @Override
    public void store(CurrencyPair pair) throws ValidationException {
        List<ValidationError> errors = pair.validate(Validatable.GROUP_SERVER);
        if (errors.size() > 0)
            throw new ValidationException(
                    "Can't persist exchange rate, validation errors.", errors
            );

        CurrencyDAO currencyDAO = new CurrencyDAO();
        currencyDAO.persist(pair);
    }

    @Override
    public void remove(CurrencyPair pair) {
        CurrencyDAO currencyDAO = new CurrencyDAO();
        currencyDAO.delete(pair);
    }

    @Override
    public void removeAll(CurrencyPair pair) {
        CurrencyDAO currencyDAO = new CurrencyDAO();
        currencyDAO.deleteAll(pair);
    }

    @Override
    public String download(CurrencyPair pair) {
        try {
            CurrencyDAO currencyDAO = new CurrencyDAO();
            CurrencyDownloader downloader = new ExchangeRatesAPIDownloader(currencyDAO);
            downloader.updateCurrencies(pair);
            return null;
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }
}

