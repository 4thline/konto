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
import org.fourthline.konto.shared.entity.MonetaryUnit;

import java.util.List;

/**
 * @author Christian Bauer
 */
public class MonetaryUnitCellTable extends CellTable<MonetaryUnit> {

    public interface Style extends CssResource {
        String codeColumn();
        String fractionDigitsColumn();
        String prefixColumn();
    }

    protected TextColumn<MonetaryUnit> codeColumn = new TextColumn<MonetaryUnit>() {
        @Override
        public String getValue(MonetaryUnit object) {
            return object.getCurrencyCode();
        }
    };

    protected TextColumn<MonetaryUnit> fractionDigitsColumn = new TextColumn<MonetaryUnit>() {
        @Override
        public String getValue(MonetaryUnit object) {
            return Integer.toString(object.getFractionDigits());
        }
    };

    protected TextColumn<MonetaryUnit> prefixColumn = new TextColumn<MonetaryUnit>() {
        @Override
        public String getValue(MonetaryUnit object) {
            return object.getPrefix();
        }
    };

    public MonetaryUnitCellTable(Resources resources) {
        super(Integer.MAX_VALUE, resources);

        addColumn(codeColumn, new TextHeader("Code"));
        addColumn(fractionDigitsColumn, new TextHeader("Fraction Digits"));
        addColumn(prefixColumn, new TextHeader("Prefix"));
    }

    public void applyStyle(Style style) {
        addColumnStyleName(0, style.codeColumn());
        addColumnStyleName(1, style.fractionDigitsColumn());
        addColumnStyleName(2, style.prefixColumn());
    }

    public void setMonetaryUnits(List<MonetaryUnit> units) {
        setRowCount(units.size(), true);
        setRowData(0, units);
    }

}
