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

package org.fourthline.konto.client.ledger;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import org.fourthline.konto.shared.LedgerCoordinates;

/**
 * @author Christian Bauer
 */
public class LedgerPlace extends Place {

    protected LedgerCoordinates ledgerCoordinates;

    public LedgerPlace(LedgerCoordinates ledgerCoordinates) {
        this.ledgerCoordinates = ledgerCoordinates;
    }

    public LedgerPlace(String token) {
        this.ledgerCoordinates = LedgerCoordinates.valueOf(token);
    }

    public LedgerCoordinates getLedgerCoordinates() {
        return ledgerCoordinates;
    }

    @Prefix("ledger")
    public static class Tokenizer implements PlaceTokenizer<LedgerPlace> {

        @Override
        public String getToken(LedgerPlace place) {
            return place.getLedgerCoordinates().toString();
        }

        @Override
        public LedgerPlace getPlace(String token) {
            return new LedgerPlace(token);
        }
    }

}
