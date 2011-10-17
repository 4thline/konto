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

package org.fourthline.konto.shared.entity;

import org.fourthline.konto.shared.Constants;
import org.seamless.gwt.validation.shared.EntityProperty;
import org.seamless.gwt.validation.shared.Validatable;
import org.seamless.gwt.validation.shared.ValidationError;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "USERS")
public class User implements Serializable, Validatable {

    public enum Property implements EntityProperty {
        id,
    }

    @Id
    @GeneratedValue(generator = Constants.SEQUENCE_NAME)
    @Column(name = "USER_ID")
    private Long id;

    public User() {
    }

    public User(Long id) {
        this.id = id;
    }

    @Override
    public List<ValidationError> validate(String group) {
        return new ArrayList();
    }
}