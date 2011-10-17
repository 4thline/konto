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

package org.fourthline.konto.client.ledger.callback;

import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.seamless.gwt.notify.client.ServerFailureNotifyEvent;
import org.fourthline.konto.shared.entity.Account;

import java.util.Iterator;
import java.util.List;

/**
 * @author Christian Bauer
 */
public abstract class ListAccountsCallback implements AsyncCallback<List<Account>> {

    EventBus bus;
    Long ignoreId;

    public ListAccountsCallback(EventBus bus, Long ignoreId) {
        this.bus = bus;
        this.ignoreId = ignoreId;
    }

    public Long getIgnoreId() {
        return ignoreId;
    }

    @Override
    public void onFailure(Throwable caught) {
        bus.fireEvent(new ServerFailureNotifyEvent(caught));
    }

    @Override
    public void onSuccess(List<Account> result) {
        filterResult(result);
        handle(result);
    }

    public void filterResult(List<Account> result) {
        Iterator<Account> it = result.iterator();
        while (it.hasNext()) {
            Account next = it.next();
            if (next.getId().equals(ignoreId)) {
                it.remove();
            }
        }
    }

    abstract protected void handle(List<Account> accounts);
}
