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

import org.fourthline.konto.shared.entity.MonetaryUnit;

/**
 * @author Christian Bauer
 */
public interface DebitCreditHolder {

    MonetaryUnit getMonetaryUnit();

    void setMonetaryUnit(MonetaryUnit unit);

    MonetaryAmount getDebit();

    void setDebit(MonetaryAmount amount);

    MonetaryAmount getCredit();

    void setCredit(MonetaryAmount amount);

    public static class Accessor {

        public static void updateDebitOrCredit(DebitCreditHolder holder, MonetaryAmount newAmount) {
            if (holder.getMonetaryUnit() == null)
                holder.setMonetaryUnit(newAmount.getUnit());

            MonetaryAmount currentAmount = getDebitOrCredit(holder);
            MonetaryAmount updatedAmount = currentAmount.add(newAmount);
            DebitCreditHolder.Accessor.setDebitOrCredit(holder, updatedAmount);
        }

        public static void setDebitOrCredit(DebitCreditHolder holder, MonetaryAmount newAmount) {
            if (holder.getMonetaryUnit() == null)
                holder.setMonetaryUnit(newAmount.getUnit());

            MonetaryAmount debit = new MonetaryAmount(newAmount.getUnit());
            MonetaryAmount credit = new MonetaryAmount(newAmount.getUnit());
            if (newAmount.signum() == -1) {
                debit = new MonetaryAmount(newAmount.getUnit(), newAmount.getValue().abs());
            } else {
                credit = new MonetaryAmount(newAmount.getUnit(), newAmount.getValue().abs());
            }
            holder.setDebit(debit);
            holder.setCredit(credit);
        }

        // TODO: Callers check for null
        public static MonetaryAmount getDebitOrCredit(DebitCreditHolder holder) {
            if (holder.getMonetaryUnit() == null) {
                return null;
            }
            MonetaryAmount amount = new MonetaryAmount(holder.getMonetaryUnit());
            if (holder.getDebit() != null) {
                amount = amount.subtract(holder.getDebit());
            }
            if (holder.getCredit() != null) {
                amount = amount.add(holder.getCredit());
            }
            return amount;
        }

    }

}
