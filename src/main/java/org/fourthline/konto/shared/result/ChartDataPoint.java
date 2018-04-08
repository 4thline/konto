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

import org.fourthline.konto.shared.MonetaryAmount;

import java.io.Serializable;

/**
 * @author Christian Bauer
 */
public class ChartDataPoint implements Serializable {

    protected Integer year;
    protected Integer month;
    protected MonetaryAmount monetaryAmount;

    public ChartDataPoint() {

    }

    public ChartDataPoint(Integer year, Integer month, MonetaryAmount monetaryAmount) {
        this.year = year;
        this.month = month;
        this.monetaryAmount = monetaryAmount;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public MonetaryAmount getMonetaryAmount() {
        return monetaryAmount;
    }

    public void setMonetaryAmount(MonetaryAmount monetaryAmount) {
        this.monetaryAmount = monetaryAmount;
    }

    @Override
    public String toString() {
        return "year=" + year +
            ", month=" + month +
            ", monetaryAmount=" + monetaryAmount;
    }
}
