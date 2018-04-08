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

import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.hibernate.transform.ResultTransformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Bauer
 */
public class CurrencyDAO extends HibernateDAO {

    final protected Map<CurrencyPair, List<CurrencyPair>> currencyPairsCache = new HashMap<>();

    public List<MonetaryUnit> getMonetaryUnits() {
        return getCurrentSession()
            .createQuery("select mu from MonetaryUnit mu order by mu.currencyCode asc")
            .list();
    }

    @SuppressWarnings("unchecked")
    public List<CurrencyPair> getCurrencyPairs(String fromCode, String toCode) {
        synchronized (currencyPairsCache) {
            List<CurrencyPair> result = null;
            for (CurrencyPair currencyPair : currencyPairsCache.keySet()) {
                if (currencyPair.equalsFromToCodes(fromCode, toCode)) {
                    result = currencyPairsCache.get(currencyPair);
                }
            }
            if (result == null) {
                result = getCurrentSession()
                    .createQuery(
                        "select cp, fromUnit, toUnit from CurrencyPair cp, " +
                            "MonetaryUnit fromUnit, MonetaryUnit toUnit " +
                            "where " +
                            "cp.fromCode = :from and cp.toCode = :to " +
                            "and fromUnit.currencyCode = cp.fromCode " +
                            "and toUnit.currencyCode = cp.toCode " +
                            "order by cp.createdOn desc"
                    )
                    .setString("from", fromCode)
                    .setString("to", toCode)
                    .setResultTransformer(new ResultTransformer() {
                        @Override
                        public Object transformTuple(Object[] objects, String[] strings) {
                            CurrencyPair pair = (CurrencyPair) objects[0];
                            pair.setFromUnit((MonetaryUnit) objects[1]);
                            pair.setToUnit((MonetaryUnit) objects[2]);
                            return pair;
                        }

                        @Override
                        public List transformList(List list) {
                            return list;
                        }
                    })
                    .list();
                currencyPairsCache.put(new CurrencyPair(fromCode, toCode), result);
            }
            return result;
        }
    }

    public MonetaryUnit getMonetaryUnit(String currencyCode) {
        if (currencyCode == null) return null;
        return (MonetaryUnit) getCurrentSession().createQuery(
            "select mu from MonetaryUnit mu where mu.currencyCode = :cc"
        ).setString("cc", currencyCode).uniqueResult();
    }

    public void persist(MonetaryUnit mu) {
        synchronized (currencyPairsCache) {
            currencyPairsCache.clear();
            getCurrentSession().saveOrUpdate(mu);
        }
    }

    public boolean delete(MonetaryUnit mu) {
        synchronized (currencyPairsCache) {
            currencyPairsCache.clear();
            boolean inUse = getCurrentSession().createQuery(
                "select a from Account a where a.monetaryUnitId = :id"
            ).setLong("id", mu.getId()).list().size() > 0;
            if (inUse) return false;

            getCurrentSession().delete(mu);
            return true;
        }
    }

    public void persist(CurrencyPair pair) {
        synchronized (currencyPairsCache) {
            currencyPairsCache.clear();

            // Delete (effectively overwrite) an existing rate with the same date
            getCurrentSession().createQuery(
                "delete from CurrencyPair cp where " +
                    "cp.fromCode = :from and " +
                    "cp.toCode = :to and " +
                    "cp.createdOn = :date"
            ).setString("from", pair.getFromCode())
                .setString("to", pair.getToCode())
                .setDate("date", pair.getCreatedOn())
                .executeUpdate();

            getCurrentSession().save(pair);
        }
    }

    public void delete(CurrencyPair pair) {
        synchronized (currencyPairsCache) {
            currencyPairsCache.clear();
            getCurrentSession().delete(pair);
        }
    }

    public void deleteAll(CurrencyPair pair) {
        synchronized (currencyPairsCache) {
            currencyPairsCache.clear();
            getCurrentSession().createQuery(
                "delete from CurrencyPair cp where " +
                    "cp.fromCode = :from and cp.toCode = :to"
            ).setString("from", pair.getFromCode())
                .setString("to", pair.getToCode())
                .executeUpdate();
        }
    }

}


