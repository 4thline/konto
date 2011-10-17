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

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@MappedSuperclass
public abstract class Option<V> implements Serializable {

    static public class Domain<V> implements Serializable {

        protected String name;
        protected OptionDatatype<V> datatype;

        public Domain(String name, OptionDatatype<V> datatype) {
            this.name = name;
            this.datatype = datatype;
        }

        public String getName() {
            return name;
        }

        public OptionDatatype<V> getDatatype() {
            return datatype;
        }
    }

    @Id
    @Column(name = "NAME")
    protected String name;

    @Id
    @Column(name = "USER_ID")
    protected Long userId;

    @Column(name = "VALUE", nullable = false, length = 255)
    protected String value;

    transient protected Map<String, OptionDatatype> datatypes = new HashMap();

    protected Option() {
    }

    protected Option(Map<String, OptionDatatype> datatypes) {
        this.datatypes = datatypes;
    }

    protected Option(String name, Map<String, OptionDatatype> datatypes) {
        this.name = name;
        this.datatypes = datatypes;
    }

    protected Option(String name, Long userId, Map<String, OptionDatatype> datatypes) {
        this.name = name;
        this.userId = userId;
        this.datatypes = datatypes;
    }

    protected Option(String name, Map<String, OptionDatatype> datatypes, V value) {
        this.name = name;
        this.datatypes = datatypes;
        setValue(value);
    }

    protected Option(String name, Long userId, Map<String, OptionDatatype> datatypes, V value) {
        this.name = name;
        this.userId = userId;
        this.datatypes = datatypes;
        setValue(value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRawValue() {
        return value;
    }

    public void setRawValue(String value) {
        this.value = value;
    }

    public V getValue() {
        if (getName() == null || datatypes == null) return null;
        OptionDatatype dt = datatypes.get(getName());
        return dt != null ? (V)dt.valueOf(getRawValue()) : null;
    }

    public void setValue(V value) {
        if (getName() == null || datatypes == null) return;
        OptionDatatype dt = datatypes.get(getName());
        if (dt != null) setRawValue(dt.toString(value));
    }

    public boolean isEqualValue(Option<V> other) {
        return (getValue() == null && other.getValue() == null)
                || getValue() != null && getValue().equals(other.getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Option option = (Option) o;

        if (name != null ? !name.equals(option.name) : option.name != null) return false;
        if (userId != null ? !userId.equals(option.userId) : option.userId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getName() + ", User ID: " + getUserId() + ", Raw Value: " + getRawValue();
    }
}
