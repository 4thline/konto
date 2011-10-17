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

package org.fourthline.konto.client.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.entity.MonetaryUnit;

import java.util.Date;
import java.util.List;

public interface CurrencyServiceAsync {
    void getMonetaryUnits(AsyncCallback<List<MonetaryUnit>> async);

    void getCurrencyPair(MonetaryUnit fromUnit, MonetaryUnit toUnit, Date forDay, AsyncCallback<CurrencyPair> async);

    void store(MonetaryUnit unit, AsyncCallback<Void> async);

    void remove(MonetaryUnit unit, AsyncCallback<Boolean> async);

    void getCurrencyPairs(MonetaryUnit fromUnit, MonetaryUnit toUnit, AsyncCallback<List<CurrencyPair>> async);

    void store(CurrencyPair pair, AsyncCallback<Void> async);

    void remove(CurrencyPair pair, AsyncCallback<Void> async);

    void removeAll(CurrencyPair pair, AsyncCallback<Void> async);

    void download(CurrencyPair pair, AsyncCallback<String> async);
}
