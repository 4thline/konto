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

import com.google.gwt.i18n.client.DateTimeFormat;
import org.fourthline.konto.client.ledger.component.AccountTreeSelectView;
import org.fourthline.konto.client.report.view.ReportSelectView;
import org.fourthline.konto.shared.AccountType;
import org.seamless.util.time.DateFormat;
import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.query.AccountsQueryCriteria;
import org.fourthline.konto.shared.query.LineReportCriteria;
import org.fourthline.konto.shared.query.LineReportOption;
import org.fourthline.konto.shared.result.ReportLines;

import java.util.Date;
import java.util.Map;

/**
 * @author Christian Bauer
 */
public enum LineReportType {

    BS("Balance Sheet",
       new AccountsQueryCriteria[]{
               new AccountsQueryCriteria(AccountType.Asset),
               new AccountsQueryCriteria(AccountType.Liability),
       },
       new LineReportOption(false, false, false, false),
       new AccountTreeSelectView.Option[]{
               AccountTreeSelectView.Option.NEW_BUTTON,
               AccountTreeSelectView.Option.LABEL_FILTER,
               AccountTreeSelectView.Option.MULTISELECT,
               AccountTreeSelectView.Option.SELECT_ALL,
               AccountTreeSelectView.Option.HIDE_INCOME,
               AccountTreeSelectView.Option.HIDE_EXPENSE
       },
       new ReportSelectView.Option[0]
    ) {
        @Override
        public String getLabelSub(DateFormat dateFormat,
                                  LineReportCriteria criteria) {
            DateTimeFormat fmt = dateFormat != null
                    ? DateTimeFormat.getFormat(dateFormat.getPattern())
                    : DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM);
            return fmt.format(criteria.getRange().getEnd());
        }

        @Override
        public String getTotalSum(boolean roundFractions, Map<AccountType, ReportLines> linesByType) {
            if (!linesByType.containsKey(AccountType.Asset)
                    || !linesByType.containsKey(AccountType.Liability))
                return null;

            MonetaryAmount total = linesByType.get(AccountType.Asset).getTotal().add(
                    linesByType.get(AccountType.Liability).getTotal()
            );
            return "Net Worth: " + total.getReportString(true, true, false, roundFractions);
        }
    },

    CF("Cashflow",
       new AccountsQueryCriteria[0], // Load no accounts initially, too expensive for this report
       new LineReportOption(true, true, false, true),
       new AccountTreeSelectView.Option[]{
               AccountTreeSelectView.Option.NEW_BUTTON,
               AccountTreeSelectView.Option.LABEL_FILTER,
               AccountTreeSelectView.Option.MULTISELECT,
               AccountTreeSelectView.Option.SELECT_NONE,
               AccountTreeSelectView.Option.HIDE_INCOME,
               AccountTreeSelectView.Option.HIDE_EXPENSE
       },
       new ReportSelectView.Option[]{
               ReportSelectView.Option.USE_DATE_RANGE,
               ReportSelectView.Option.ENABLE_ENTRY_DETAILS
       }
    ) {
        @Override
        public boolean useInitialBalance(LineReportCriteria criteria) {
            return false;
        }

        @Override
        public String getTotalSum(boolean roundFractions, Map<AccountType, ReportLines> linesByType) {
            if (!linesByType.containsKey(AccountType.Asset)) return null;
            return "Total Cashflow: " +
                    linesByType.get(AccountType.Asset).getTotal().getReportString(true, true, false, roundFractions);
        }
    },

    ES("Earnings Statement",
       new AccountsQueryCriteria[]{
               new AccountsQueryCriteria(AccountType.Income),
               new AccountsQueryCriteria(AccountType.Expense),
       },
       new LineReportOption(false, false, false, false),
       new AccountTreeSelectView.Option[]{
               AccountTreeSelectView.Option.NEW_BUTTON,
               AccountTreeSelectView.Option.LABEL_FILTER,
               AccountTreeSelectView.Option.MULTISELECT,
               AccountTreeSelectView.Option.SELECT_ALL,
               AccountTreeSelectView.Option.HIDE_ASSET,
               AccountTreeSelectView.Option.HIDE_LIABILITY
       },
       new ReportSelectView.Option[]{
               ReportSelectView.Option.USE_DATE_RANGE,
               ReportSelectView.Option.ENABLE_ENTRY_DETAILS
       }
    ) {
        @Override
        public String getTotalSum(boolean roundFractions, Map<AccountType, ReportLines> linesByType) {
            if (!linesByType.containsKey(AccountType.Income)
                    || !linesByType.containsKey(AccountType.Expense))
                return null;

            MonetaryAmount total = linesByType.get(AccountType.Income).getTotal().add(
                    linesByType.get(AccountType.Expense).getTotal()
            );
            return "Profit/Loss: " + total.negate().getReportString(true, true, false, roundFractions);
        }

        @Override
        public boolean areAmountsNegated() {
            return true;
        }
    };

    final String label;
    final AccountsQueryCriteria[] defaultAccountSelection;
    final LineReportOption defaultOptions;

    final AccountTreeSelectView.Option[] accountSelectOptions;
    final ReportSelectView.Option[] reportSelectOptions;

    LineReportType(String label,
                   AccountsQueryCriteria[] defaultAccountSelection, LineReportOption defaultOptions,
                   AccountTreeSelectView.Option[] accountSelectOptions, ReportSelectView.Option[] reportSelectOptions) {
        this.label = label;
        this.defaultAccountSelection = defaultAccountSelection;
        this.defaultOptions = defaultOptions;
        this.accountSelectOptions = accountSelectOptions;
        this.reportSelectOptions = reportSelectOptions;
    }

    public String getLabel() {
        return label;
    }

    public String getLabelSub(DateFormat dateFormat, LineReportCriteria criteria) {

        DateTimeFormat fmt = dateFormat != null
                ? DateTimeFormat.getFormat(dateFormat.getPattern())
                : DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM);
        StringBuilder sb = new StringBuilder();

        if (criteria.getRange().getStart() != null)
            sb.append(fmt.format(criteria.getRange().getStart()));

        if (ReportSelectView.Option.USE_DATE_RANGE.in(getReportSelectOptions()))
            sb.append(" - ");

        if (criteria.getRange().getEnd() != null) {
            sb.append(fmt.format(criteria.getRange().getEnd()));
        } else {
            sb.append(fmt.format(new Date()));
        }
        return sb.toString();
    }

    public String getTypeSum(boolean roundFractions, AccountType type, Map<AccountType, ReportLines> linesByType) {
        return areAmountsNegated()
                ? linesByType.get(type).getTotal().negate().getReportString(false, true, false, roundFractions)
                : linesByType.get(type).getTotal().getReportString(false, true, false, roundFractions);
    }

    public String getTotalSum(boolean roundFractions, Map<AccountType, ReportLines> linesByType) {
        return null;
    }

    public boolean areAmountsNegated() {
        return false;
    }

    public boolean useInitialBalance(LineReportCriteria criteria) {
        return criteria.getRange().getStart() == null;
    }

    public AccountsQueryCriteria[] getDefaultAccountSelection() {
        return defaultAccountSelection;
    }

    public LineReportOption getDefaultOptions() {
        return defaultOptions;
    }

    public AccountTreeSelectView.Option[] getAccountSelectOptions() {
        return accountSelectOptions;
    }

    public ReportSelectView.Option[] getReportSelectOptions() {
        return reportSelectOptions;
    }
}
