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

package org.fourthline.konto.client.ledger.account.view;

import com.google.gwt.user.client.ui.IsWidget;
import org.seamless.gwt.component.client.binding.ValidatableViewProperty;
import org.seamless.gwt.component.client.suggest.SuggestionSelectView;
import org.fourthline.konto.client.ledger.account.AccountGroupSuggestion;
import org.fourthline.konto.shared.AccountType;
import org.seamless.util.time.DateFormat;
import org.seamless.gwt.validation.shared.ValidationError;
import org.fourthline.konto.shared.entity.MonetaryUnit;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Christian Bauer
 */
public interface AccountView extends IsWidget {

    public interface Presenter {

        void typeSelected(AccountType type);

        void currencySelected(int index);

        void save();

        void delete();

        void cancel();

    }

    void setPresenter(Presenter presenter);

    void setDateFormat(DateFormat dateFormat);

    void reset();

    void focus();

    SuggestionSelectView<AccountGroupSuggestion> getAccountGroupSelectView();

    void showValidationErrorAccountGroup(ValidationError error);

    void clearValidationErrorAccountGroup();

    void setCreateMode(boolean createMode);
    
    ValidatableViewProperty<String> getNameProperty();

    ValidatableViewProperty<Date> getEffectiveOnProperty();

    ValidatableViewProperty<BigDecimal> getInitialBalanceProperty();

    void setCurrency(String string);

    void setCurrencies(List<MonetaryUnit> currencies);

    void addFormPanelRow(IsWidget widget);

    void removeFormPanelRow(IsWidget widget);

}
