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

package org.fourthline.konto.shared.entity.settings;

import org.seamless.util.time.DateRange;

/**
 * @author Christian Bauer
 */
public class DateRangeOptionDatatype implements OptionDatatype<DateRange> {

    @Override
    public DateRange valueOf(String s) {
        try {
            String[] splits = s.split("-");
            if (splits.length == 2) {
                return new DateRange(splits[0], splits[1]);
            } else if (splits.length == 1 && s.startsWith("-")) {
                return new DateRange(null, splits[0]);
            } else if (splits.length == 1 && s.endsWith("-")) {
                return new DateRange(splits[0], null);
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public String toString(DateRange value) {
        StringBuilder sb = new StringBuilder();
        if (value.getStart() != null) {
            sb.append(value.getStart().getTime());
        }
        sb.append("-");
        if (value.getEnd() != null) {
            sb.append(value.getEnd().getTime());
        }
        return sb.toString();
    }
}
