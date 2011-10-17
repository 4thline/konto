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

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.TextHeader;
import org.seamless.gwt.component.client.widget.DateColumn;
import org.seamless.util.time.DateFormat;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.entity.MonetaryUnit;

import java.util.Date;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class CurrencyPairCellTable extends CellTable {

    public interface Style extends CssResource {
        String createdOnColumn();
        String rateColumn();
    }

    protected DateColumn<CurrencyPair> createdOnColumn = new DateColumn<CurrencyPair>() {
        @Override
        protected Date getDate(CurrencyPair object) {
            return object.getCreatedOn();
        }
    };

    protected TextColumn<CurrencyPair> rateColumn = new TextColumn<CurrencyPair>() {
        @Override
        public String getValue(CurrencyPair object) {
            return object.getExchangeRate().toString();
        }
    };

    protected MonetaryUnit currentUnit;

    public CurrencyPairCellTable(Resources resources) {
        super(Integer.MAX_VALUE, resources);

        addColumn(createdOnColumn, new TextHeader("Date"));
        addColumn(rateColumn, new TextHeader("Rate"));
    }

    public void applyStyle(Style style) {
        addColumnStyleName(0, style.createdOnColumn());
        addColumnStyleName(1, style.rateColumn());
    }

    public void setCurrencyPairs(MonetaryUnit currentUnit, List<CurrencyPair> pairs) {
        this.currentUnit = currentUnit;
        setRowCount(pairs.size(), true);
        setRowData(0, pairs);
    }

    public void setDateFormat(DateFormat df) {
        if (df != null)
            createdOnColumn.setDateFormat(df.getPattern());
    }

}
