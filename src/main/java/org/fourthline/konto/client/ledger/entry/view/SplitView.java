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
import org.seamless.gwt.component.client.suggest.SuggestionSelectView;
import org.fourthline.konto.client.ledger.entry.AccountSuggestion;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.DebitCreditHolder;
import org.fourthline.konto.shared.entity.Split;
import org.seamless.gwt.validation.shared.ValidationError;

import java.util.Date;
import java.util.List;

/**
 * @author Christian Bauer
 */
public interface SplitView extends IsWidget, DescriptionView, DebitCreditHolder {

    public interface Presenter extends DescriptionView.Presenter {

        SplitView getView();

        void startWith(Account currentAccount, Split split);

        Split getSplit();

        void setEffectiveDate(Date date);

        void debitUpdated();

        void creditUpdated();

        void immediateSubmit();

        void switchToOpposite();

        List<ValidationError> flushView(int index);

        void clearValidationErrors();

        void showValidationErrors(List<ValidationError> errors);
    }

    void setPresenter(Presenter presenter);

    void focus();

    void enableSwitch(boolean enabled);

    void setCurrentAccount(Account currentAccount);

    SuggestionSelectView<AccountSuggestion> getAccountSelectView();

    ExchangeView getExchangeView();

    public void showExchangeView(boolean show);

    void showValidationErrorAmount(ValidationError error);

    void clearValidationErrorAmount();

    void showValidationErrorAccount(ValidationError error);

    void clearValidationErrorAccount();

}
