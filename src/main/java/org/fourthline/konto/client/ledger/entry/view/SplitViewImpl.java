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
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import javax.inject.Inject;
import org.fourthline.konto.client.bundle.Bundle;
import org.seamless.gwt.component.client.suggest.SuggestionSelectView;
import org.fourthline.konto.client.ledger.Constants;
import org.fourthline.konto.client.service.LedgerServiceAsync;
import org.fourthline.konto.client.ledger.entry.AccountSuggestion;
import org.fourthline.konto.client.ledger.entry.LedgerLineSuggestOracle;
import org.seamless.gwt.component.client.widget.BigDecimalGhostedTextBox;
import org.seamless.gwt.component.client.widget.EnterKeyHandler;
import org.seamless.gwt.component.client.widget.GhostedTextBox;
import org.seamless.gwt.component.client.widget.ImageTextButton;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.seamless.gwt.theme.shared.client.ThemeBundle;
import org.seamless.gwt.theme.shared.client.ThemeStyle;
import org.seamless.gwt.validation.shared.ValidationError;

import java.math.BigDecimal;

/**
 * @author Christian Bauer
 */
public class SplitViewImpl extends Composite implements SplitView {

    interface UI extends UiBinder<HTMLPanel, SplitViewImpl> {
    }

    private UI ui = GWT.create(UI.class);

    interface Style extends CssResource {
    }

    @UiField(provided = true)
    Bundle bundle;
    @UiField(provided = true)
    final ThemeBundle themeBundle;
    @UiField
    Style style;

    @UiField(provided = true)
    SuggestBox descriptionSuggestBox;
    @UiField(provided = true)
    BigDecimalGhostedTextBox debitTextBox;
    @UiField(provided = true)
    BigDecimalGhostedTextBox creditTextBox;

    @UiField
    SimplePanel accountSelectPanel;
    @UiField
    SimplePanel exchangePanel;
    @UiField
    ImageTextButton switchButton;
    @UiField
    TableCellElement switchButtonPanel;

    final KeyUpHandler immediateSubmitHandler =
            new EnterKeyHandler() {
                @Override
                protected void onEnterKey() {
                    presenter.immediateSubmit();
                }
            };

    Presenter presenter;
    Account currentAccount;
    MonetaryUnit unit;
    HandlerRegistration descriptionSelectionHandler;
    AccountSelectView accountSelectView;
    ExchangeView exchangeView;

    @Inject
    public SplitViewImpl(Bundle bundle,
                         LedgerServiceAsync service,
                         AccountSelectView accountSelectView,
                         ExchangeView exchangeView) {
        this.bundle = bundle;
        this.themeBundle = bundle.themeBundle().create();

        descriptionSuggestBox =
                new SuggestBox(
                        new LedgerLineSuggestOracle(service) {
                            @Override
                            public Long getAccountId() {
                                return currentAccount.getId();
                            }

                            @Override
                            public boolean isQueryEnabled(String query) {
                                return !query.equals(getDescriptionLabel())
                                        && presenter.isNewDescription(query);
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

        debitTextBox =
                new BigDecimalGhostedTextBox(ThemeStyle.GhostedTextBox()) {
                    @Override
                    public String getGhostLabel() {
                        return getDebitLabel();
                    }

                    @Override
                    protected void valueChanged(String text) {
                        clearValidationErrorAmount();
                        BigDecimal value = getBigDecimalValue();
                        if (value == null || value.signum() == 0) {
                            debitTextBox.clear();
                        } else if (creditTextBox.getBigDecimalValue() != null) {
                            creditTextBox.clear();
                        }
                        presenter.debitUpdated();
                    }
                };

        debitTextBox.addKeyUpHandler(immediateSubmitHandler);

        creditTextBox =
                new BigDecimalGhostedTextBox(ThemeStyle.GhostedTextBox()) {
                    @Override
                    public String getGhostLabel() {
                        return getCreditLabel();
                    }

                    @Override
                    protected void valueChanged(String text) {
                        clearValidationErrorAmount();
                        BigDecimal value = getBigDecimalValue();
                        if (value == null || value.signum() == 0) {
                            creditTextBox.clear();
                        } else if (debitTextBox.getBigDecimalValue() != null) {
                            debitTextBox.clear();
                        }
                        presenter.creditUpdated();
                    }
                };

        creditTextBox.addKeyUpHandler(immediateSubmitHandler);

        initWidget(ui.createAndBindUi(this));

        this.accountSelectView = accountSelectView;
        accountSelectPanel.setWidget(accountSelectView);

        this.exchangeView = exchangeView;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void enableSwitch(boolean enabled) {
        if (enabled) {
            switchButton.setVisible(true);
            switchButtonPanel.addClassName(ThemeStyle.FormCell());
        } else {
            switchButton.setVisible(false);
            switchButtonPanel.removeClassName(ThemeStyle.FormCell());
        }
    }

    @Override
    public void setSplitSuggestionHandler(SelectionHandler<SuggestOracle.Suggestion> handler) {
        if (descriptionSelectionHandler != null) {
            descriptionSelectionHandler.removeHandler();
        }
        descriptionSelectionHandler = descriptionSuggestBox.addSelectionHandler(handler);
    }

    @Override
    public void focus() {
        descriptionSuggestBox.getTextBox().selectAll();
        descriptionSuggestBox.setFocus(true);
    }

    @Override
    public void setCurrentAccount(Account currentAccount) {
        this.currentAccount = currentAccount;
    }

    @Override
    public SuggestionSelectView<AccountSuggestion> getAccountSelectView() {
        return accountSelectView;
    }

    @Override
    public ExchangeView getExchangeView() {
        return exchangeView;
    }

    @Override
    public void showExchangeView(boolean show) {
        if (show) {
            exchangePanel.setWidget(exchangeView);
        } else {
            exchangePanel.clear();
        }
    }

    @Override
    public void setDescription(String description) {
        descriptionSuggestBox.setValue(description, true);
    }

    @Override
    public String getDescription() {
        return descriptionSuggestBox.getValue();
    }

    @Override
    public MonetaryUnit getMonetaryUnit() {
        return unit;
    }

    @Override
    public void setMonetaryUnit(MonetaryUnit unit) {
        this.unit = unit;
    }

    @Override
    public MonetaryAmount getDebit() {
        MonetaryAmount amount =
                debitTextBox.getBigDecimalValue() != null
                ? new MonetaryAmount(getMonetaryUnit(), debitTextBox.getBigDecimalValue())
                : null;
        if (amount != null) {
            // Update displayed value (e.g. correcting user-entered fraction digits)
            debitTextBox.setBigDecimalValue(amount.getValue());
        }
        return amount;
    }

    @Override
    public void setDebit(MonetaryAmount amount) {
        debitTextBox.setBigDecimalValue(amount.getValue());
    }

    @Override
    public MonetaryAmount getCredit() {
        MonetaryAmount amount =
                creditTextBox.getBigDecimalValue() != null
                ? new MonetaryAmount(getMonetaryUnit(), creditTextBox.getBigDecimalValue())
                : null; 
        if (amount != null) {
            // Update displayed value (e.g. correcting user-entered fraction digits)
            creditTextBox.setBigDecimalValue(amount.getValue());
        }
        return amount;
    }

    @Override
    public void setCredit(MonetaryAmount amount) {
        creditTextBox.setBigDecimalValue(amount.getValue());
    }

    @Override
    public void showValidationErrorDescription(ValidationError error) {
        descriptionSuggestBox.addStyleName(ThemeStyle.FormErrorField());
    }

    @Override
    public void clearValidationErrorDescription() {
        descriptionSuggestBox.removeStyleName(ThemeStyle.FormErrorField());
    }

    @Override
    public void showValidationErrorAmount(ValidationError error) {
        debitTextBox.addStyleName(ThemeStyle.FormErrorField());
        creditTextBox.addStyleName(ThemeStyle.FormErrorField());
    }

    @Override
    public void clearValidationErrorAmount() {
        debitTextBox.removeStyleName(ThemeStyle.FormErrorField());
        creditTextBox.removeStyleName(ThemeStyle.FormErrorField());
    }

    @Override
    public void showValidationErrorAccount(ValidationError error) {
        accountSelectView.showValidationError(error);
    }

    @Override
    public void clearValidationErrorAccount() {
        accountSelectView.clearValidationError();
    }

    @UiHandler("switchButton")
    void onClickSwitch(ClickEvent e) {
        if (presenter != null) {
            presenter.switchToOpposite();
        }
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
