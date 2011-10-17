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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Christian Bauer
 */
public class Settings<O extends Option> extends ArrayList<O> {

    public Settings(int i) {
        super(i);
    }

    public Settings() {
    }

    public Settings(Collection<? extends O> ses) {
        super(ses);
    }

    public Settings(O... options) {
        addAll(Arrays.asList(options));
    }

    public <V> O getOption(Option.Domain<V> domain) {
        if (domain == null || size() == 0) return null;
        for (O option : this) {
            if (option.getName().equals(domain.getName()))
                return option;
        }
        return null;
    }

    public <V> V getValue(Option.Domain<V> domain) {
        Option<V> op = getOption(domain);
        return op != null ? op.getValue() : null;
    }
}
