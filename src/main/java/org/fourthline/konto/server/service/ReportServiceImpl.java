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

package org.fourthline.konto.server.service;

import org.fourthline.konto.client.service.ReportService;
import org.seamless.gwt.server.HibernateRemoteServiceServlet;
import org.fourthline.konto.server.dao.AccountDAO;
import org.fourthline.konto.server.dao.CurrencyDAO;
import org.fourthline.konto.server.dao.EntryDAO;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.fourthline.konto.shared.query.AccountsQueryCriteria;
import org.fourthline.konto.shared.query.LineReportCriteria;
import org.fourthline.konto.shared.result.AccountReportLine;
import org.fourthline.konto.shared.result.EntryReportLine;
import org.fourthline.konto.shared.result.ReportLines;

import java.util.List;
import java.util.Map;

/**
 * @author Christian Bauer
 */
public class ReportServiceImpl extends HibernateRemoteServiceServlet implements ReportService {

    @Override
    public ReportLines[] getReportLines(LineReportCriteria reportCriteria) {

        AccountDAO accountDAO = new AccountDAO();
        EntryDAO entryDAO = new EntryDAO();
        CurrencyDAO currencyDAO = new CurrencyDAO();

        AccountsQueryCriteria[] accountSelection = reportCriteria.getAccountSelection();

        ReportLines[] linesArray = new ReportLines[accountSelection.length];

        MonetaryUnit monetaryUnit = currencyDAO.getMonetaryUnit(reportCriteria.getCurrencyCode());
        if (monetaryUnit == null) return linesArray;

        for (int i = 0; i < accountSelection.length; i++) {
            AccountsQueryCriteria ac = accountSelection[i];

            List<Account> accounts = accountDAO.getAccounts(ac);

            if (reportCriteria.getOptions().isEntryDetails()) {
                Map<Account, List<EntryReportLine>> lines =
                        entryDAO.getEntryReportLines(accounts, reportCriteria.getRange());
                linesArray[i] = new ReportLines(
                        ac,
                        monetaryUnit,
                        lines,
                        reportCriteria.getType().useInitialBalance(reportCriteria),
                        new DefaultCurrencyProvider(currencyDAO),
                        reportCriteria.getDayOfExchangeRate()
                );
            } else {
                List<AccountReportLine> lines =
                        entryDAO.getAccountReportLines(
                                accounts,
                                reportCriteria.getRange(),
                                reportCriteria.getType().useInitialBalance(reportCriteria)
                        );
                linesArray[i] = new ReportLines(
                        ac,
                        monetaryUnit,
                        lines,
                        new DefaultCurrencyProvider(currencyDAO),
                        reportCriteria.getDayOfExchangeRate()
                );
            }

        }

        return linesArray;
    }
}
