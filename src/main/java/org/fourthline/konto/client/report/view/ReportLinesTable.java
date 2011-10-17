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

package org.fourthline.konto.client.report.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import org.seamless.util.time.DateFormat;
import org.fourthline.konto.shared.LedgerCoordinates;
import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.query.LineReportCriteria;
import org.fourthline.konto.shared.result.EntryReportLine;
import org.fourthline.konto.shared.result.ExchangedMonetaryAmount;
import org.fourthline.konto.shared.result.GroupReportLine;
import org.fourthline.konto.shared.result.ReportLines;
import org.fourthline.konto.shared.result.AccountReportLine;


/**
 * TODO: Migrate to CellTable
 *
 * @author Christian Bauer
 */
public class ReportLinesTable extends FlexTable {

    public interface Resources extends ClientBundle {

        @Source(Style.DEFAULT_CSS)
        Style style();

    }

    public interface Style extends CssResource {

        String DEFAULT_CSS = "org/fourthline/konto/client/report/view/ReportLinesTable.css";

        String table();

        String groupRow();

        String groupLabelCell();

        String groupAmountCell();

        String accountRow();

        String accountLabelCell();

        String accountAmountCell();

        String accountAmountGroupedCell();

        String accountExchangeCell();

        String entryRow();

        String entryEffectiveOnCell();

        String entryLabelCell();

        String entryFromToAccountCell();

        String entryAmountCell();

        String rowOdd();

        String rowEven();
    }

    protected static Resources DEFAULT_RESOURCES;

    protected static Resources getDefaultResources() {
        if (DEFAULT_RESOURCES == null) {
            DEFAULT_RESOURCES = GWT.create(Resources.class);
        }
        return DEFAULT_RESOURCES;
    }

    final protected Resources resources;
    final protected Style style;

    protected DateTimeFormat dateFormat =
            DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM);

    protected boolean roundFractions;

    public ReportLinesTable() {
        this(getDefaultResources());
    }

    public ReportLinesTable(Resources resources) {
        this.resources = resources;
        this.style = resources.style();
        style.ensureInjected();

        setCellPadding(2);
        setCellSpacing(0);
        setBorderWidth(0);

        addStyleName(style.table());
    }

    public void setValue(ReportLines reportLines, LineReportCriteria criteria) {
        setValue(
                reportLines,
                criteria.getType().areAmountsNegated(),
                criteria.getOptions().isEntryAccounts(),
                criteria.getOptions().isExchangeRates(),
                criteria.getOptions().isZeroBalances()
        );
    }

    public void setValue(ReportLines reportLines, boolean negate,
                         boolean entryAccounts, boolean exchangeRates, boolean zeroBalances) {
        int row = 0;

        removeAllRows();

        for (GroupReportLine groupLine : reportLines) {
            if (!zeroBalances && groupLine.getAmount().signum() == 0) continue;

            boolean grouped = false;
            if (!ReportLines.isDefaultGroup(groupLine.getGroup())) {
                grouped = true;
                row = addGroupLine(row, groupLine, negate);
            }

            for (AccountReportLine accountLine : groupLine.getSubLines()) {
                if (!zeroBalances && accountLine.getAmount().signum() == 0) continue;
                row = addAccountLine(row, accountLine, negate, grouped, exchangeRates);

                for (EntryReportLine entryLine : accountLine.getSubLines()) {
                    row = addEntryLine(row, entryLine, negate, entryAccounts);
                }
            }
        }
    }

    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat != null
                ? DateTimeFormat.getFormat(dateFormat.getPattern())
                : DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM);
    }

    public void setRoundFractions(boolean roundFractions) {
        this.roundFractions = roundFractions;
    }

    protected void onLineClick(LedgerCoordinates ledgerCoordinates) {

    }

    protected int addGroupLine(int row, GroupReportLine line, boolean negate) {

        setText(row, 0, line.getLabel());

        MonetaryAmount amount = line.getAmount();
        if (negate) amount = amount.negate();
        setText(row, 1, amount.getReportString(true, false, false, roundFractions));

        ((FlexTable.FlexCellFormatter) getCellFormatter()).setColSpan(row, 0, 3);
        getCellFormatter().addStyleName(row, 0, style.groupLabelCell());
        getCellFormatter().addStyleName(row, 1, style.groupAmountCell());
        getRowFormatter().addStyleName(row, style.groupRow());
        return ++row;
    }

    protected int addAccountLine(int row, final AccountReportLine line,
                                 boolean negate, boolean isGrouped, boolean exchangeRates) {

        Anchor accountAnchor = new Anchor(line.getAccount().getLabel(false, false, false, true));
        accountAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onLineClick(new LedgerCoordinates(line.getAccount().getId()));
            }
        });
        setWidget(row, 0, accountAnchor);

        MonetaryAmount amount = line.getAmount();
        if (negate) amount = amount.negate();
        setText(row, 1, amount.getReportString(true, false, false, roundFractions));

        ((FlexTable.FlexCellFormatter) getCellFormatter()).setColSpan(row, 0, 3);
        getCellFormatter().addStyleName(row, 0, style.accountLabelCell());
        getCellFormatter().addStyleName(row, 1, isGrouped ? style.accountAmountGroupedCell() : style.accountAmountCell());
        getRowFormatter().addStyleName(row, style.accountRow());
        row++;

        if (exchangeRates && amount instanceof ExchangedMonetaryAmount) {
            ExchangedMonetaryAmount ex = (ExchangedMonetaryAmount) amount;

            StringBuilder sb = new StringBuilder();
            sb.append(
                    negate
                            ? ex.getOriginalAmount().negate().getReportString(true, true, false, roundFractions)
                            : ex.getOriginalAmount().getReportString(true, true, false, roundFractions)
            );
            sb.append(" x ");
            sb.append(ex.getCurrencyPair().getExchangeRateString());
            sb.append(" (").append(dateFormat.format(ex.getCurrencyPair().getCreatedOn())).append(")");
            setText(row, 0, sb.toString());
            getCellFormatter().addStyleName(row, 0, style.accountExchangeCell());
            ((FlexTable.FlexCellFormatter) getCellFormatter()).setRowSpan(row - 1, 1, 2);
            ((FlexTable.FlexCellFormatter) getCellFormatter()).setColSpan(row, 0, 3);

            row++;
        }

        return row;
    }

    protected int addEntryLine(int row, final EntryReportLine line, boolean negate, boolean entryAccounts) {
        setText(row, 0, line.getEffectiveOn().toString());

        Anchor entryAnchor = new Anchor(line.getLabel());
        entryAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onLineClick(line.getLedgerCoordinates());
            }
        });
        setWidget(row, 1, entryAnchor);

        String fromToAccount =
                line.getFromToAccountGroup() != null
                        ? line.getFromToAccountGroup() + ": " : "";
        fromToAccount = fromToAccount + line.getFromToAccount();

        if (entryAccounts) {
            Anchor fromToAccountAnchor = new Anchor(fromToAccount);
            fromToAccountAnchor.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    onLineClick(new LedgerCoordinates(line.getFromToAccountId()));
                }
            });
            setWidget(row, 2, fromToAccountAnchor);
        } else {
            setWidget(row, 2, new Label(""));
        }

        MonetaryAmount amount = line.getAmount();
        if (negate) amount = amount.negate();
        setText(row, 3, amount.getReportString(true, false, false, roundFractions));

        getCellFormatter().addStyleName(row, 0, style.entryEffectiveOnCell());
        getCellFormatter().addStyleName(row, 1, style.entryLabelCell());
        getCellFormatter().addStyleName(row, 2, style.entryFromToAccountCell());
        getCellFormatter().addStyleName(row, 3, style.entryAmountCell());
        getRowFormatter().addStyleName(row, style.entryRow());
        getRowFormatter().addStyleName(row, row % 2 == 0 ? style.rowOdd() : style.rowEven());
        return ++row;
    }

}
