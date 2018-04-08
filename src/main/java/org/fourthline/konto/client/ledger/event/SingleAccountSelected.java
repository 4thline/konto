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

package org.fourthline.konto.client.ledger.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import org.fourthline.konto.shared.entity.Account;

/**
 * @author Christian Bauer
 */
public class SingleAccountSelected extends Event<SingleAccountSelected.Handler> {

    public static Type<Handler> TYPE = new Type<>();

    public interface Handler extends EventHandler {
        void onSingleAccountSelected(SingleAccountSelected event);
    }

    Account selection;

    public SingleAccountSelected(Account selection) {
        this.selection = selection;
    }

    public Account getSelection() {
        return selection;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onSingleAccountSelected(this);
    }
}
