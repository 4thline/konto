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

import org.fourthline.konto.server.dao.CurrencyDAO;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.CurrencyProvider;
import org.fourthline.konto.shared.entity.MonetaryUnit;

import java.util.Date;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class DefaultCurrencyProvider implements CurrencyProvider {

    protected CurrencyDAO currencyDAO;

    public DefaultCurrencyProvider(CurrencyDAO currencyDAO) {
        this.currencyDAO = currencyDAO;
    }

    @Override
    public CurrencyPair getCurrencyPair(MonetaryUnit fromUnit, MonetaryUnit toUnit, Date forDay) {
        List<CurrencyPair> pairs = currencyDAO.getCurrencyPairs(
                fromUnit.getCurrencyCode(), toUnit.getCurrencyCode()
        );

        /*
        // TODO: We can't get historical exchange rates for free... as in money.
        if (pairs.size() == 0) {
            // Is the latest stored rate the exchange rate for the desired day?
            Calendar forDayCal = new GregorianCalendar();
            forDayCal.setTime(forDay);
            Calendar newestPairCal = new GregorianCalendar();
            newestPairCal.setTime(pairs.size() > 0 ? pairs.get(0).getCreatedOn() : new Date(0));
            boolean havePairForDay =
                    forDayCal.get(Calendar.ERA) == newestPairCal.get(Calendar.ERA) &&
                            forDayCal.get(Calendar.YEAR) == newestPairCal.get(Calendar.YEAR) &&
                            forDayCal.get(Calendar.DAY_OF_YEAR) == newestPairCal.get(Calendar.DAY_OF_YEAR);


            if (!havePairForDay) {
                if (download(new CurrencyPair(fromUnit, toUnit, forDay)) == null) // No error, refresh the pairs
                    pairs = currencyDAO.getCurrencyPairs(
                            fromUnit.getCurrencyCode(),
                            toUnit.getCurrencyCode()
                    );
            }
        }
        */

        // Don't have anything, return null
        if (pairs.size() == 0)
            return null;

        // Try to find a better rate near the given day (collection is stored by date, newest first)
        CurrencyPair afterOrOnGivenDay = pairs.get(0);
        CurrencyPair beforeGivenDay = null;
        for (CurrencyPair history : pairs) {
            if (history.getCreatedOn().getTime() < forDay.getTime()) {
                beforeGivenDay = history;
                break; // We are past the given day, stop here
            }
            afterOrOnGivenDay = history;
        }
        CurrencyPair nearestGivenDay = afterOrOnGivenDay;
        if (beforeGivenDay != null) {
            long deltaAfter = afterOrOnGivenDay.getCreatedOn().getTime() - forDay.getTime();
            long deltaBefore = beforeGivenDay.getCreatedOn().getTime() - forDay.getTime();
            nearestGivenDay =
                    Math.abs(deltaBefore) < Math.abs(deltaAfter)
                            ? beforeGivenDay
                            : afterOrOnGivenDay;
        }
        return nearestGivenDay;
    }
}
