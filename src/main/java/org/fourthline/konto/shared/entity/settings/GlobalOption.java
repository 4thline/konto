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

import org.seamless.util.time.DateFormat;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "GLOBAL_OPTION")
public class GlobalOption<V> extends Option<V> {

    public static final Option.Domain<Integer> OPT_SIDEBAR_WIDTH =
            new Option.Domain("SIDEBAR_WIDTH", new IntegerOptionDatatype());

    public static final Option.Domain<DateFormat> OPT_DATE_FORMAT =
            new Option.Domain("DATE_FORMAT", new DateFormatOptionDatatype());

    public static final Option.Domain<Boolean> OPT_NEW_ENTRY_SELECT_DAY =
            new Option.Domain("NEW_ENTRY_SELECT_DAY", new BooleanOptionDatatype());

    public static final Option.Domain<Boolean> OPT_ROUND_FRACTIONS_IN_REPORTS =
            new Option.Domain("ROUND_FRACTIONS_IN_REPORTS", new BooleanOptionDatatype());

    protected static final Map<String, OptionDatatype> DATATYPES = new HashMap<String, OptionDatatype>() {{
        put(OPT_SIDEBAR_WIDTH.getName(), OPT_SIDEBAR_WIDTH.getDatatype());
        put(OPT_DATE_FORMAT.getName(), OPT_DATE_FORMAT.getDatatype());
        put(OPT_NEW_ENTRY_SELECT_DAY.getName(), OPT_NEW_ENTRY_SELECT_DAY.getDatatype());
        put(OPT_ROUND_FRACTIONS_IN_REPORTS.getName(), OPT_ROUND_FRACTIONS_IN_REPORTS.getDatatype());
    }};

    public GlobalOption() {
        super(DATATYPES);
    }

    public GlobalOption(Domain<V> domain) {
        this(domain.getName());
    }

    public GlobalOption(String name) {
        super(name, DATATYPES);
    }

    public GlobalOption(Domain<V> domain, Long userId) {
        this(domain.getName(), userId);
    }

    public GlobalOption(String name, Long userId) {
        super(name, userId, DATATYPES);
    }

    public GlobalOption(Domain<V> domain, V value) {
        super(domain.getName(), DATATYPES, value);
    }

    public GlobalOption(String name, V value) {
        super(name, DATATYPES, value);
    }

    public GlobalOption(Domain<V> domain, Long userId, V value) {
        super(domain.name, userId, DATATYPES, value);
    }

    public GlobalOption(String name, Long userId, V value) {
        super(name, userId, DATATYPES, value);
    }
}
