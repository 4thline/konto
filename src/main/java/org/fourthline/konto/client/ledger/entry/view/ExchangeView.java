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

import com.google.gwt.user.client.ui.IsWidget;
import org.seamless.gwt.component.client.binding.ValidatableViewProperty;
import org.fourthline.konto.shared.MonetaryAmount;
import org.seamless.gwt.validation.shared.ValidationError;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.entity.MonetaryUnit;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Christian Bauer
 */
public interface ExchangeView extends IsWidget {

    public interface Presenter {

        void startWith(Date forDay,
                       MonetaryUnit fromUnit,
                       MonetaryUnit toUnit,
                       MonetaryAmount originalAmount,
                       MonetaryAmount exchangedAmount);

        void updateForDay(Date date);

        void updateOriginalAmount(MonetaryAmount originalAmount);

        void rateUpdated();

        void exchangedAmountUpdated();

        MonetaryAmount getExchangedAmount();

        void clearValidationErrors();

        void showValidationErrors(List<ValidationError> errors);
    }

    void setPresenter(Presenter presenter);

    void reset(CurrencyPair pair, MonetaryAmount amount);

    ValidatableViewProperty<BigDecimal> getRateProperty();

    ValidatableViewProperty<MonetaryAmount> getAmountProperty();

}
