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

package org.fourthline.konto.client.report;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import org.fourthline.konto.shared.query.LineReportCriteria;

import java.io.Serializable;

/**
 * Encapsulats all reporting schema and state for the UI.
 *
 * @author Christian Bauer
 */
public class ReportPlace extends Place implements Serializable {

    LineReportCriteria criteria;

    public ReportPlace() {
    }

    public ReportPlace(LineReportCriteria criteria) {
        this.criteria = criteria;
    }

    public LineReportCriteria getCriteria() {
        return criteria;
    }

    @Override
    public String toString() {
        return "ReportPlace: " + getCriteria();
    }

    @Prefix("report")
    public static class Tokenizer implements PlaceTokenizer<ReportPlace> {

        @Override
        public ReportPlace getPlace(String token) {
            if ("index".equals(token)) return new ReportPlace();
            return new ReportPlace(LineReportCriteria.valueOf(token));
        }

        @Override
        public String getToken(ReportPlace place) {
            return place.getCriteria() != null ? place.getCriteria().toString() : "index";
        }
    }

}

