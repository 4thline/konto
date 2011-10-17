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

package org.fourthline.konto.shared.result;

import org.fourthline.konto.shared.MonetaryAmount;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class ReportLine<RL extends ReportLine> implements Serializable {

    protected String label;
    protected MonetaryAmount amount;
    protected List<RL> subLines = new ArrayList();

    public ReportLine() {
    }

    public ReportLine(String label, MonetaryAmount amount) {
        this.label = label;
        this.amount = amount;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setAmount(MonetaryAmount amount) {
        this.amount = amount;
    }

    public MonetaryAmount getAmount() {
        return amount;
    }

    public List<RL> getSubLines() {
        return subLines;
    }

    public void setSubLines(List<RL> subLines) {
        this.subLines = subLines;
    }

    public void addSubLine(RL subLine) {
        getSubLines().add(subLine);
        if (getAmount() != null) {
            setAmount(getAmount().add(subLine.getAmount()));
        }
    }

    @Override
    public String toString() {
        return getLabel() + " => " + getAmount();
    }
}
