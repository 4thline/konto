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
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.DebitCreditHolder;
import org.fourthline.konto.shared.entity.Entry;
import org.fourthline.konto.shared.MonetaryAmount;
import org.seamless.gwt.validation.shared.ValidationError;

import java.util.List;

/**
 * @author Christian Bauer
 */
public interface EntrySummaryView extends IsWidget, DescriptionView {

    public interface Presenter extends DescriptionView.Presenter, DebitCreditHolder {

        EntrySummaryView getView();

        void startWith(Account currentAccount, Entry entry);

        List<ValidationError> flushView();

        void clearValidationErrors();

        void showValidationErrors(List<ValidationError> errors);

    }

    void setPresenter(Presenter presenter);

    void focus();

    void setCurrentAccount(Account currentAccount);

    void setDebit(MonetaryAmount debitAmount);

    void setCredit(MonetaryAmount creditAmount);
}
