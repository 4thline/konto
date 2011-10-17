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

package org.fourthline.konto.shared.query;

import java.io.Serializable;

/**
 * @author Christian Bauer
 */
public class LineReportOption implements Serializable {

    boolean entryDetails;
    boolean entryAccounts;
    boolean exchangeRates;
    boolean zeroBalances;

    public LineReportOption() {
    }

    public LineReportOption(boolean entryDetails, boolean entryAccounts, boolean exchangeRates, boolean zeroBalances) {
        this.entryDetails = entryDetails;
        this.entryAccounts = entryAccounts;
        this.exchangeRates = exchangeRates;
        this.zeroBalances = zeroBalances;
    }

    public boolean isEntryDetails() {
        return entryDetails;
    }

    public void setEntryDetails(boolean entryDetails) {
        this.entryDetails = entryDetails;
    }

    public boolean isEntryAccounts() {
        return entryAccounts;
    }

    public void setEntryAccounts(boolean entryAccounts) {
        this.entryAccounts = entryAccounts;
    }

    public boolean isExchangeRates() {
        return exchangeRates;
    }

    public void setExchangeRates(boolean exchangeRates) {
        this.exchangeRates = exchangeRates;
    }

    public boolean isZeroBalances() {
        return zeroBalances;
    }

    public void setZeroBalances(boolean zeroBalances) {
        this.zeroBalances = zeroBalances;
    }

    public static LineReportOption valueOf(String s) {
        if (!s.contains("ro=")) return null;
        String ro = s.substring(s.indexOf("ro=") + 3);
        ro = ro.substring(0, ro.indexOf(";"));
        String[] split = ro.split(",");
        if (split.length != 4) return null;
        try {
            return new LineReportOption(
                    Boolean.valueOf(split[0]),
                    Boolean.valueOf(split[1]),
                    Boolean.valueOf(split[2]),
                    Boolean.valueOf(split[3])
            );
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "ro="
                + isEntryDetails() + ","
                + isEntryAccounts() + ","
                + isExchangeRates() + ","
                + isZeroBalances() + ";";
    }

}
