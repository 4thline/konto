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

package org.fourthline.konto.client.ledger.entry;

import com.google.gwt.user.client.ui.SuggestOracle;
import org.fourthline.konto.shared.entity.Split;

/**
 * @author Christian Bauer
 */
public class SplitSuggestion implements SuggestOracle.Suggestion {

    String description;
    Split split;

    public SplitSuggestion(String description, Split split) {
        this.description = description;
        this.split = split;
    }

    public String getDescription() {
        return description;
    }

    public Split getSplit() {
        return split;
    }

    @Override
    public String getDisplayString() {
        return getDescription();
    }

    @Override
    public String getReplacementString() {
        return getDescription();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SplitSuggestion that = (SplitSuggestion) o;

        if (!description.equals(that.description)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return description.hashCode();
    }
}
