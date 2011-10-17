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

package org.fourthline.konto.client.currency;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

/**
 * @author Christian Bauer
 */
public class CurrencyPlace extends Place {

    private Long monetaryUnitId;

    public CurrencyPlace() {
    }

    public CurrencyPlace(Long monetaryUnitId) {
        this.monetaryUnitId = monetaryUnitId;
    }

    public CurrencyPlace(String token) {
        this.monetaryUnitId = Long.valueOf(token);
    }

    public Long getMonetaryUnitId() {
        return monetaryUnitId;
    }

    @Prefix("currency")
    public static class Tokenizer implements PlaceTokenizer<CurrencyPlace> {

        @Override
        public CurrencyPlace getPlace(String token) {
            try {
                Long.parseLong(token);
                return new CurrencyPlace(token);
            } catch (Exception ex) {
                // Ignore
            }
            return new CurrencyPlace();
        }

        @Override
        public String getToken(CurrencyPlace place) {
            return place.getMonetaryUnitId() != null ? place.getMonetaryUnitId().toString() : "index";
        }
    }
}
