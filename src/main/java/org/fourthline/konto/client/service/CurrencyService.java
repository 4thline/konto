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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.seamless.gwt.validation.shared.ValidationException;

import java.util.Date;
import java.util.List;

@RemoteServiceRelativePath("currency")
public interface CurrencyService extends RemoteService {

    List<MonetaryUnit> getMonetaryUnits();

    void store(MonetaryUnit unit) throws ValidationException;

    boolean remove(MonetaryUnit unit);

    CurrencyPair getCurrencyPair(MonetaryUnit fromUnit, MonetaryUnit toUnit, Date forDay);

    List<CurrencyPair> getCurrencyPairs(MonetaryUnit fromUnit, MonetaryUnit toUnit);

    void store(CurrencyPair pair) throws ValidationException;

    void remove(CurrencyPair pair);

    void removeAll(CurrencyPair pair);

    String download(CurrencyPair pair);

}
