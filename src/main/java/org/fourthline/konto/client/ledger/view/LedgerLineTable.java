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

package org.fourthline.konto.client.ledger.view;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.Entry;
import org.fourthline.konto.shared.entity.Split;
import org.fourthline.konto.shared.result.LedgerLine;
import org.seamless.gwt.component.client.widget.BlacklistEventTranslator;
import org.seamless.gwt.component.client.widget.ClickableTextColumn;
import org.seamless.gwt.component.client.widget.DateColumn;
import org.seamless.util.time.DateFormat;

import java.util.Date;

/**
 * @author Christian Bauer
 */
public class LedgerLineTable extends CellTable<LedgerLine> {

    public static interface Style {
        String accountCell();

        String dateColumn();

        String descriptionColumn();

        String accountColumn();

        String amountColumn();

        String filteredBalance();

        String rowInFuture();
    }

    final DateColumn<LedgerLine> dateColumn = new DateColumn<LedgerLine>() {
        @Override
        protected Date getDate(LedgerLine object) {
            return object.getDate();
        }
    };

    final SingleSelectionModel<LedgerLine> selectionModel;

    public LedgerLineTable(CellTable.Resources cellTableResources, final Style style) {
        super(Integer.MAX_VALUE, cellTableResources);

        // This steals the focus from the entry form if enabled
        setKeyboardSelectionPolicy(
                HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED
        );

        selectionModel = new SingleSelectionModel<LedgerLine>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                LedgerLine selected = selectionModel.getSelectedObject();
                if (selected != null) {
                    onSelection(selected);
                }
            }
        });

        // TODO Hack until this is available http://code.google.com/p/google-web-toolkit/source/detail?r=9788#
        setSelectionModel(selectionModel, new DefaultSelectionEventManager(new BlacklistEventTranslator(2)) {
        });

        dateColumn.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

        TextColumn<LedgerLine> descriptionColumn = new TextColumn<LedgerLine>() {
            @Override
            public String getValue(LedgerLine object) {
                return object.getDescription();
            }
        };
        descriptionColumn.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

        Column<LedgerLine, String> accountColumn =
                new ClickableTextColumn<LedgerLine>(style.accountCell()) {

                    @Override
                    protected boolean isStyled(LedgerLine object) {
                        return object.getFromToAccount() != null;
                    }

                    @Override
                    public String getValue(LedgerLine object) {
                        if (!object.getDescription().toLowerCase().contains(getCurrentDescriptionFilter().toLowerCase())) {
                            return "Split(s) match '" + getCurrentDescriptionFilter() + "'";
                        } else {
                            if (object.getFromToAccount() == null) {
                                return ((Entry) object.getLedgerEntry()).getSplits().size() + " Splits";
                            } else {
                                return object.getFromToAccount().getLabel(
                                        true, true, true, false
                                );
                            }
                        }
                    }

                    @Override
                    protected void onClick(int index, LedgerLine object, String value) {
                        if (object.getFromToAccount() != null) {
                            onSelection(object.getFromToAccount());
                        }
                    }
                };
        accountColumn.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

        TextColumn<LedgerLine> debitColumn = new TextColumn<LedgerLine>() {
            @Override
            public String getValue(LedgerLine object) {
                return object.getDebit().getReportString(false, false, true);
            }
        };
        debitColumn.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        debitColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

        TextColumn<LedgerLine> creditColumn = new TextColumn<LedgerLine>() {
            @Override
            public String getValue(LedgerLine object) {
                return object.getCredit().getReportString(false, false, true);
            }
        };
        creditColumn.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        creditColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

        TextColumn<LedgerLine> balanceColumn = new TextColumn<LedgerLine>() {

            @Override
            public void render(Cell.Context context, LedgerLine object, SafeHtmlBuilder sb) {
                if (getCurrentDescriptionFilter().length() > 0) {
                    sb.appendHtmlConstant("<span class=\"" + style.filteredBalance() + "\">");
                    super.render(context, object, sb);
                    sb.appendHtmlConstant("</span>");
                } else {
                    super.render(context, object, sb);
                }
            }

            @Override
            public String getValue(LedgerLine object) {
                return object.getBalanceString();
            }
        };
        balanceColumn.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        balanceColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

        addColumn(dateColumn, new TextHeader("Date"));
        addColumnStyleName(0, style.dateColumn());
        addColumn(descriptionColumn, new TextHeader("Description"));
        addColumnStyleName(1, style.descriptionColumn());
        addColumn(accountColumn, new TextHeader("From/To"));
        addColumnStyleName(2, style.accountColumn());

        // TODO: This celltable API and behavior is evil
        addColumn(debitColumn, new Header<String>(new TextCell() {
            @Override
            public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div align=\"right\">");
                super.render(context, value, sb);
                sb.appendHtmlConstant("</div>");
            }
        }) {

            @Override
            public String getValue() {
                return getDebitLabel();
            }

            @Override
            public void render(Cell.Context context, SafeHtmlBuilder sb) {
                super.render(context, sb);
            }
        });
        addColumnStyleName(3, style.amountColumn());
        addColumnStyleName(3, style.amountColumn());
        addColumn(creditColumn, new Header<String>(new TextCell() {
            @Override
            public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div align=\"right\">");
                super.render(context, value, sb);
                sb.appendHtmlConstant("</div>");
            }
        }) {

            @Override
            public String getValue() {
                return getCreditLabel();
            }
        });
        addColumnStyleName(4, style.amountColumn());
        addColumn(balanceColumn, new Header<String>(new TextCell() {
            @Override
            public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div align=\"right\">");
                super.render(context, value, sb);
                sb.appendHtmlConstant("</div>");
            }
        }) {
            @Override
            public String getValue() {
                return "Balance";
            }
        });
        addColumnStyleName(5, style.amountColumn());

        setRowStyles(new RowStyles<LedgerLine>() {
            @Override
            public String getStyleNames(LedgerLine row, int rowIndex) {
                return !getSelectionModel().isSelected(row) && row.isDateInFuture()
                        ? style.rowInFuture() : null;
            }
        });
    }

    public void select(Long entryId, Long splitId) {
        if (entryId == null && splitId == null) return;
        for (LedgerLine line : getVisibleItems()) {
            if (line.getLedgerEntry() instanceof Split && line.getLedgerEntry().getId().equals(splitId)) {
                getSelectionModel().setSelected(line, true);
                break;
            }
            if (line.getLedgerEntry() instanceof Entry && line.getLedgerEntry().getId().equals(entryId)) {
                getSelectionModel().setSelected(line, true);
                break;
            }
        }
    }

    public void setDateFormat(DateFormat df) {
        if (df != null)
            dateColumn.setDateFormat(df.getPattern());
    }

    protected String getDebitLabel() {
        return "";
    }

    protected String getCreditLabel() {
        return "";
    }

    protected String getCurrentDescriptionFilter() {
        return "";
    }

    protected void onSelection(LedgerLine line) {
    }

    protected void onSelection(Account account) {

    }
}
