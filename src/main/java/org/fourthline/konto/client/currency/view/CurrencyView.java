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

package org.fourthline.konto.client.currency.view;

import com.google.gwt.user.client.ui.IsWidget;
import org.seamless.util.time.DateFormat;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.entity.MonetaryUnit;

import java.util.List;

/**
 * @author Christian Bauer
 */
public interface CurrencyView extends IsWidget {

    public interface Presenter {

        void monetaryUnitSelected(MonetaryUnit unit);

        void exchangeMonetaryUnitSelected(int index);

        void currencyPairSelected(CurrencyPair pair);

        void downloadAll();

        void download();

        void removeAll();
    }

    void setPresenter(Presenter presenter);

    void setDateFormat(DateFormat dateFormat);

    void reset();

    void setMonetaryUnits(List<MonetaryUnit> units);

    void setExchangeMonetaryUnits(MonetaryUnit selectedUnit, List<MonetaryUnit> units);

    void setCurrencyPairs(MonetaryUnit selectedUnit, List<CurrencyPair> pairs);

}

