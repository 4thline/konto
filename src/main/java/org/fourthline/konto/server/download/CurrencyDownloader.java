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

package org.fourthline.konto.server.download;

import org.fourthline.konto.server.dao.CurrencyDAO;
import org.fourthline.konto.shared.entity.CurrencyPair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Christian Bauer
 */
public abstract class CurrencyDownloader {

    final protected CurrencyDAO currencyDAO;

    protected CurrencyDownloader(CurrencyDAO currencyDAO) {
        this.currencyDAO = currencyDAO;
    }

    public CurrencyDAO getCurrencyDAO() {
        return currencyDAO;
    }

    public void updateCurrencies() throws Exception {
        updateCurrencies(null);
    }

    public void updateCurrencies(CurrencyPair pair) throws Exception {

        List<CurrencyPair> pairs;
        if (pair != null) {
            pairs = Arrays.asList(pair);
        } else {
            pairs = CurrencyPair.getPairs(getCurrencyDAO().getMonetaryUnits());
        }

        List<CurrencyPair> batch = new ArrayList();
        for (CurrencyPair p : pairs) {
            if (batch.size() == ExchangeRatesAPIDownloader.BATCH_SIZE) {
                updateExchangeRates(batch);
                persist(batch);
                batch.clear();
            }
            batch.add(p);
        }
        updateExchangeRates(batch);
        persist(batch);
    }

    protected void persist(List<CurrencyPair> pairs) {
        for (CurrencyPair pair : pairs) {
            getCurrencyDAO().persist(pair);
        }
    }

    abstract protected void updateExchangeRates(List<CurrencyPair> pairs) throws Exception;

}
