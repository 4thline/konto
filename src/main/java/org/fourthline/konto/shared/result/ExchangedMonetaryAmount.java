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

package org.fourthline.konto.shared.result;

import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.entity.MonetaryUnit;

import java.math.BigDecimal;

/**
 * @author Christian Bauer
 */
public class ExchangedMonetaryAmount extends MonetaryAmount {

    protected MonetaryAmount originalAmount;
    protected CurrencyPair currencyPair;

    public ExchangedMonetaryAmount() {
    }

    public ExchangedMonetaryAmount(MonetaryAmount originalAmount, CurrencyPair currencyPair) {
        super(currencyPair.getExchangedAmount(originalAmount));
        if (!currencyPair.getFromCode().equals(originalAmount.getUnit().getCurrencyCode())) {
            throw new IllegalArgumentException(
                    "Original amount does not match currency pair from-code '"
                            + currencyPair.getFromCode()
                            + "': "
                            + originalAmount
            );
        }
        this.originalAmount = originalAmount;
        this.currencyPair = currencyPair;
    }

    protected ExchangedMonetaryAmount(MonetaryUnit unit, BigDecimal value, MonetaryAmount originalAmount, CurrencyPair currencyPair) {
        super(unit, value);
        this.originalAmount = originalAmount;
        this.currencyPair = currencyPair;
    }

    public MonetaryAmount getOriginalAmount() {
        return originalAmount;
    }

    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }

    @Override
    protected MonetaryAmount newInstance(MonetaryUnit unit, BigDecimal value) {
        return new ExchangedMonetaryAmount(unit, value, getOriginalAmount(), getCurrencyPair());
    }

    @Override
    public String toString() {
        return super.toString() + ", " + getOriginalAmount() + ", " + getCurrencyPair();
    }

}
