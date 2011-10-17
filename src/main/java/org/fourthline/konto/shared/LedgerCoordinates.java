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

package org.fourthline.konto.shared;

import java.io.Serializable;

/**
 * @author Christian Bauer
 */
public class LedgerCoordinates implements Serializable {

    private Long accountId;
    private Long entryId;
    private Long splitId;

    public LedgerCoordinates() {
    }

    public LedgerCoordinates(Long accountId) {
        this.accountId = accountId;
    }

    public LedgerCoordinates(Long accountId, Long entryId, Long splitId) {
        this.accountId = accountId;
        this.entryId = entryId;
        this.splitId = splitId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getEntryId() {
        return entryId;
    }

    public void setEntryId(Long entryId) {
        this.entryId = entryId;
    }

    public Long getSplitId() {
        return splitId;
    }

    public void setSplitId(Long splitId) {
        this.splitId = splitId;
    }

    public static LedgerCoordinates valueOf(String s) {
        LedgerCoordinates coord = new LedgerCoordinates();
        if (s.contains("/")) {
            String[] strings = s.split("/");
            coord.setAccountId(Long.valueOf(strings[0]));
            if (strings.length > 1)
                coord.setEntryId(Long.valueOf(strings[1]));
            if (strings.length > 2)
                coord.setSplitId(Long.valueOf(strings[2]));
        } else {
            coord.setAccountId(Long.valueOf(s));
        }
        return coord;
    }

    @Override
    public String toString() {
        if (getEntryId() != null) {
            return getAccountId() + "/" +getEntryId()
                    + (getSplitId() != null ? "/" + getSplitId() : "");
        }
        return getAccountId().toString();
    }
}
