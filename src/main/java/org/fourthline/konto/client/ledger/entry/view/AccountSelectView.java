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

package org.fourthline.konto.client.ledger.entry.view;

import org.fourthline.konto.client.ledger.Constants;
import org.fourthline.konto.client.ledger.entry.AccountSuggestion;
import org.seamless.gwt.component.client.suggest.PopupSelectViewImpl;

import javax.inject.Inject;

/**
 * @author Christian Bauer
 */
public class AccountSelectView extends PopupSelectViewImpl<AccountSuggestion> {

    @Inject
    public AccountSelectView() {
        super(500, 350, 200);
    }

    @Override
    protected String getSelectLabel() {
        return Constants.LABEL_SELECT_ACCOUNT;
    }
}
