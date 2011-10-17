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

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import org.fourthline.konto.client.currency.view.CurrencyPairView;
import org.fourthline.konto.client.currency.view.CurrencyPairViewImpl;
import org.fourthline.konto.client.currency.view.CurrencyView;
import org.fourthline.konto.client.currency.view.CurrencyViewImpl;
import org.fourthline.konto.client.currency.view.MonetaryUnitView;
import org.fourthline.konto.client.currency.view.MonetaryUnitViewImpl;

/**
 * @author Christian Bauer
 */
public class CurrencyModule extends AbstractGinModule {

    @Override
    protected void configure() {

        bind(CurrencyView.class)
                .to(CurrencyViewImpl.class)
                .in(Singleton.class);
        bind(CurrencyActivity.class);

        bind(MonetaryUnitView.class)
                .to(MonetaryUnitViewImpl.class)
                .in(Singleton.class);
        bind(MonetaryUnitView.Presenter.class)
                .to(MonetaryUnitPresenter.class);

        bind(CurrencyPairView.class)
                .to(CurrencyPairViewImpl.class)
                .in(Singleton.class);
        bind(CurrencyPairView.Presenter.class)
                .to(CurrencyPairPresenter.class);

    }
}
