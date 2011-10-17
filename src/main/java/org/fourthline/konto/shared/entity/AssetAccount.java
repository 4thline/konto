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

import org.fourthline.konto.shared.AccountType;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "ASSET_ACCOUNT")
public class AssetAccount extends Account {

    public AssetAccount() {
    }

    public AssetAccount(String name, Long monetaryUnitId) {
        super(name, monetaryUnitId);
    }

    public AssetAccount(String name, Long monetaryUnitId, Date effectiveOn, BigDecimal initialBalance) {
        super(name, monetaryUnitId, effectiveOn, initialBalance);
    }

    @Override
    public AccountType getType() {
        return AccountType.Asset;
    }
}
