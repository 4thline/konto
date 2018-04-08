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

package org.fourthline.konto.shared.result;

import org.fourthline.konto.shared.entity.MonetaryUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class ChartDataPoints extends ArrayList<ChartDataPoint> {

    protected String accountLabel;
    protected MonetaryUnit unit;

    public ChartDataPoints() {
    }

    public ChartDataPoints(String accountLabel,
                           MonetaryUnit unit,
                           List<ChartDataPoint> dataPoints) {
        super(dataPoints);
        this.accountLabel = accountLabel;
        this.unit = unit;
    }

    public String getAccountLabel() {
        return accountLabel;
    }

    public MonetaryUnit getUnit() {
        return unit;
    }

}


