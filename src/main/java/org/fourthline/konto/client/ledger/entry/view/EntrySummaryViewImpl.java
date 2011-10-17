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

package org.fourthline.konto.client.ledger.entry.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import javax.inject.Inject;
import org.fourthline.konto.client.bundle.Bundle;
import org.fourthline.konto.client.ledger.Constants;
import org.fourthline.konto.client.service.LedgerServiceAsync;
import org.fourthline.konto.client.ledger.entry.LedgerLineSuggestOracle;
import org.seamless.gwt.component.client.widget.BigDecimalGhostedTextBox;
import org.seamless.gwt.component.client.widget.GhostedTextBox;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.MonetaryAmount;
import org.seamless.gwt.theme.shared.client.ThemeStyle;
import org.seamless.gwt.validation.shared.ValidationError;

/**
 * @author Christian Bauer
 */
public class EntrySummaryViewImpl extends Composite implements EntrySummaryView {

    interface UI extends UiBinder<HTMLPanel, EntrySummaryViewImpl> {
    }

    private UI ui = GWT.create(UI.class);

    interface Style extends CssResource {
    }

    @UiField(provided = true)
    Bundle bundle;
    @UiField
    Style style;

    @UiField(provided = true)
    SuggestBox entryDescriptionSuggestBox;
    @UiField(provided = true)
    BigDecimalGhostedTextBox entryDebitTextBox;
    @UiField(provided = true)
    BigDecimalGhostedTextBox entryCreditTextBox;

    Presenter presenter;
    Account currentAccount;
    HandlerRegistration descriptionSelectionHandler;

    @Inject
    public EntrySummaryViewImpl(Bundle bundle, LedgerServiceAsync service) {
        this.bundle = bundle;

        entryDescriptionSuggestBox =
                new SuggestBox(
                        new LedgerLineSuggestOracle(service) {
                            @Override
                            public Long getAccountId() {
                                return currentAccount.getId();
                            }

                            @Override
                            public boolean isQueryEnabled(String query) {
                                return presenter.isNewDescription(query);
                            }
                        },
                        new GhostedTextBox(
                                getDescriptionLabel(),
                                ThemeStyle.GhostedTextBox()
                        ) {

                            @Override
                            protected void valueEntered(String text) {
                                clearValidationErrorDescription();
                            }
                        }
                );

        entryDebitTextBox =
                new BigDecimalGhostedTextBox(ThemeStyle.GhostedTextBox()) {
                    @Override
                    public String getGhostLabel() {
                        return getDebitLabel();
                    }
                };


        entryCreditTextBox =
                new BigDecimalGhostedTextBox(ThemeStyle.GhostedTextBox()) {
                    @Override
                    public String getGhostLabel() {
                        return getCreditLabel();
                    }
                };

        initWidget(ui.createAndBindUi(this));
    }

    @Override
    public void focus() {
        entryDescriptionSuggestBox.getTextBox().setFocus(true);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setSplitSuggestionHandler(SelectionHandler<SuggestOracle.Suggestion> handler) {
        clearValidationErrorDescription();
        if (descriptionSelectionHandler != null) {
            descriptionSelectionHandler.removeHandler();
        }
        descriptionSelectionHandler = entryDescriptionSuggestBox.addSelectionHandler(handler);
    }

    @Override
    public void setCurrentAccount(Account currentAccount) {
        this.currentAccount = currentAccount;
    }

    @Override
    public void setDebit(MonetaryAmount debitAmount) {
        entryDebitTextBox.setBigDecimalValue(debitAmount.getValue());
    }

    @Override
    public void setCredit(MonetaryAmount creditAmount) {
        entryCreditTextBox.setBigDecimalValue(creditAmount.getValue());
    }

    @Override
    public void setDescription(String description) {
        entryDescriptionSuggestBox.setValue(description, true);
    }

    @Override
    public String getDescription() {
        return entryDescriptionSuggestBox.getValue();
    }

    @Override
    public void showValidationErrorDescription(ValidationError error) {
        entryDescriptionSuggestBox.addStyleName(ThemeStyle.FormErrorField());
    }

    @Override
    public void clearValidationErrorDescription() {
        entryDescriptionSuggestBox.removeStyleName(ThemeStyle.FormErrorField());
    }

    protected String getDescriptionLabel() {
        return Constants.LABEL_DESCRIPTION;
    }

    protected String getDebitLabel() {
        return currentAccount.getType().getDebitLabel();
    }

    protected String getCreditLabel() {
        return currentAccount.getType().getCreditLabel();
    }
}