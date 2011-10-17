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

/**
 * @author Christian Bauer
 */
public class EnumOptionDatatype<V extends Enum<V>> implements OptionDatatype<V> {

    Class<V> enumType;

    protected EnumOptionDatatype(Class<V> enumType) {
        this.enumType = enumType;
    }

    public Class<V> getEnumType() {
        return enumType;
    }

    @Override
    public V valueOf(String s) {
        try {
            return Enum.valueOf(getEnumType(), s);
        } catch (Exception ex) {
            // Ignore
        }
        return null;
    }

    @Override
    public String toString(V value) {
        return value.toString();
    }
}
