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

package org.fourthline.konto.client.currency.view;

import com.google.gwt.user.client.ui.IsWidget;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.seamless.gwt.component.client.binding.ValidatableViewProperty;

/**
 * @author Christian Bauer
 */
public interface MonetaryUnitView extends IsWidget {

    public interface Presenter {

        void startWith(MonetaryUnit unit);

        void save();

        void delete();

        void cancel();
    }

    void setPresenter(Presenter presenter);

    void reset(boolean createMode);

    void focus();

    ValidatableViewProperty<String> getCodeProperty();

    ValidatableViewProperty<Integer> getFractionDigitsProperty();

    ValidatableViewProperty<String> getPrefixProperty();

}
