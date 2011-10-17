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

package org.fourthline.konto.client.ledger.account;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

/**
 * @author Christian Bauer
 */
public class AccountPlace extends Place {

    private Long accountId;

    public AccountPlace() {
    }

    public AccountPlace(Long accountId) {
        this.accountId = accountId;
    }

    public AccountPlace(String token) {
        this.accountId = Long.valueOf(token);
    }

    public Long getAccountId() {
        return accountId;
    }

    @Prefix("account")
    public static class Tokenizer implements PlaceTokenizer<AccountPlace> {

        @Override
        public String getToken(AccountPlace place) {
            return place.getAccountId() != null ? place.getAccountId().toString() : "new";
        }

        @Override
        public AccountPlace getPlace(String token) {
            try {
                Long.parseLong(token);
                return new AccountPlace(token);
            } catch (Exception ex) {
                // Ignore
            }
            return new AccountPlace();
        }
    }

}
