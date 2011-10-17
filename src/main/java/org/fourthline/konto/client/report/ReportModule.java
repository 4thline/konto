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

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import org.fourthline.konto.client.report.view.ReportResultView;
import org.fourthline.konto.client.report.view.ReportResultViewImpl;
import org.fourthline.konto.client.report.view.ReportSelectView;
import org.fourthline.konto.client.report.view.ReportSelectViewImpl;
import org.fourthline.konto.client.report.view.ReportView;
import org.fourthline.konto.client.report.view.ReportViewImpl;

/**
 * @author Christian Bauer
 */
public class ReportModule extends AbstractGinModule {

    @Override
    protected void configure() {

        bind(ReportView.class).to(ReportViewImpl.class).in(Singleton.class);
        bind(ReportSelectView.class).to(ReportSelectViewImpl.class).in(Singleton.class);
        bind(ReportResultView.class).to(ReportResultViewImpl.class).in(Singleton.class);

    }

}
