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

package org.fourthline.konto.client.ledger.component;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import org.fourthline.konto.client.bundle.Bundle;
import org.fourthline.konto.shared.AccountType;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.query.AccountsQueryCriteria;
import org.seamless.gwt.component.client.widget.GhostedTextBox;
import org.seamless.gwt.component.client.widget.ImageTextButton;
import org.seamless.gwt.theme.shared.client.ThemeBundle;
import org.seamless.gwt.theme.shared.client.ThemeStyle;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class AccountTreeSelectViewImpl extends Composite implements AccountTreeSelectView {

    interface UI extends UiBinder<Widget, AccountTreeSelectViewImpl> {
    }

    interface Style extends CssResource {
    }

    private static UI ui = GWT.create(UI.class);

    @UiField(provided = true)
    final Bundle bundle;
    @UiField(provided = true)
    final ThemeBundle themeBundle;
    @UiField
    Style style;

    @UiField
    ImageTextButton newButton;
    @UiField
    SimplePanel newPanel;

    @UiField(provided = true)
    GhostedTextBox filterTextBox;

    @UiField
    Button filterClearButton;

    @UiField(provided = true)
    AccountTree assetTree;
    @UiField(provided = true)
    AccountTree liabilityTree;
    @UiField(provided = true)
    AccountTree incomeTree;
    @UiField(provided = true)
    AccountTree expenseTree;
    @UiField
    Label noAssetsLabel;
    @UiField
    Label noLiabilitiesLabel;
    @UiField
    Label noIncomeLabel;
    @UiField
    Label noExpenseLabel;
    @UiField
    SimplePanel filterPanel;

    protected Presenter presenter;
    protected Option[] options;
    protected List<Account> accounts = new ArrayList();
    protected Timer filterTimer;

    @Inject
    public AccountTreeSelectViewImpl(Bundle bundle) {
        this.bundle = bundle;
        this.themeBundle = bundle.themeBundle().create();

        this.filterTextBox =
                new GhostedTextBox(getFilterLabel(), ThemeStyle.GhostedTextBox()) {
                    @Override
                    public void onKeyUp(KeyUpEvent event) {
                        super.onKeyUp(event);

                        final String enteredValue = getValue();
                        if (enteredValue.length() > 0) {
                            filterClearButton.setEnabled(true);
                            scheduleFilterReqest(new Timer() {
                                public void run() {
                                    if (presenter != null) {
                                        presenter.onFilter(enteredValue);
                                    }
                                }
                            });
                        } else {
                            filterClearButton.setEnabled(false);
                            if (filterTimer != null) filterTimer.cancel();
                            if (presenter != null) {
                                presenter.onFilter(null);
                            }
                        }
                    }
                };

        assetTree = new AccountTree(AccountType.Asset);
        liabilityTree = new AccountTree(AccountType.Liability);
        incomeTree = new AccountTree(AccountType.Income);
        expenseTree = new AccountTree(AccountType.Expense);

        addSelectionHandler(new AccountTree.AccountSelectionHandler() {

            @Override
            public void onSelection(SelectionEvent<TreeItem> event) {
                super.onSelection(event);
                if (Option.MULTISELECT.in(options))
                    presenter.onMultiSelectionChange(getSelectedAccounts());
            }

            @Override
            public boolean onAccountGroupSelection(Long id, String name) {
                return !Option.MULTISELECT.in(options);
            }

            @Override
            public boolean onAccountSelection(Account account) {
                if (!Option.MULTISELECT.in(options))
                    presenter.onSingleSelectionChange(account);
                return !Option.MULTISELECT.in(options);
            }

            @Override
            public boolean onSelection(AccountType type) {
                return !Option.MULTISELECT.in(options);
            }
        });

        initWidget(ui.createAndBindUi(this));
    }


    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setAccounts(List<Account> value, Option... options) {
        this.accounts = value;
        this.options = options;

        getWidget().setVisible(true);
        clearTrees();
        updateTrees(true);
        applyCurrentOptions();
    }

    public void addSelectionHandler(SelectionHandler handler) {
        assetTree.addSelectionHandler(handler);
        liabilityTree.addSelectionHandler(handler);
        incomeTree.addSelectionHandler(handler);
        expenseTree.addSelectionHandler(handler);
    }

    @Override
    public void setSelectedAccounts(AccountsQueryCriteria[] selectedAccounts) {
        AccountsQueryCriteria criteria;

        if ((criteria = AccountsQueryCriteria.get(selectedAccounts, AccountType.Asset)) != null)
            assetTree.setMultiSelected(criteria.getListOfIdentifiers());
        else
            assetTree.setMultiSelected(false);

        if ((criteria = AccountsQueryCriteria.get(selectedAccounts, AccountType.Liability)) != null)
            liabilityTree.setMultiSelected(criteria.getListOfIdentifiers());
        else
            liabilityTree.setMultiSelected(false);

        if ((criteria = AccountsQueryCriteria.get(selectedAccounts, AccountType.Income)) != null)
            incomeTree.setMultiSelected(criteria.getListOfIdentifiers());
        else
            incomeTree.setMultiSelected(false);

        if ((criteria = AccountsQueryCriteria.get(selectedAccounts, AccountType.Expense)) != null)
            expenseTree.setMultiSelected(criteria.getListOfIdentifiers());
        else
            expenseTree.setMultiSelected(false);
    }

    @Override
    public AccountsQueryCriteria[] getSelectedAccounts() {
        List<AccountsQueryCriteria> list = new ArrayList();
        List<Long> ids;

        if (!Option.HIDE_ASSET.in(options)) {
            ids = assetTree.getSelectedAccounts();
            if (ids.size() > 0)
                list.add(new AccountsQueryCriteria(ids, AccountType.Asset));
        }

        if (!Option.HIDE_LIABILITY.in(options)) {
            ids = liabilityTree.getSelectedAccounts();
            if (ids.size() > 0)
                list.add(new AccountsQueryCriteria(ids, AccountType.Liability));
        }

        if (!Option.HIDE_INCOME.in(options)) {
            ids = incomeTree.getSelectedAccounts();
            if (ids.size() > 0)
                list.add(new AccountsQueryCriteria(ids, AccountType.Income));
        }

        if (!Option.HIDE_EXPENSE.in(options)) {
            ids = expenseTree.getSelectedAccounts();
            if (ids.size() > 0)
                list.add(new AccountsQueryCriteria(ids, AccountType.Expense));
        }

        return list.toArray(new AccountsQueryCriteria[list.size()]);
    }

    protected void applyCurrentOptions() {
        newPanel.setVisible(false);
        filterPanel.setVisible(false);
        setTreesMultiSelectEnabled(false);
        setTreesMultiSelected(false);

        assetTree.setVisible(true);
        liabilityTree.setVisible(true);
        incomeTree.setVisible(true);
        expenseTree.setVisible(true);

        if (options == null) return;

        for (Option option : options) {
            switch (option) {
                case NEW_BUTTON:
                    newPanel.setVisible(true);
                    break;
                case LABEL_FILTER:
                    filterPanel.setVisible(true);
                    break;
                case MULTISELECT:
                    setTreesMultiSelectEnabled(true);
                    break;
                case SELECT_ALL:
                    setTreesMultiSelected(true);
                    break;
                case SELECT_NONE:
                    setTreesMultiSelected(false);
                    break;
                case HIDE_ASSET:
                    assetTree.setVisible(false);
                    break;
                case HIDE_LIABILITY:
                    liabilityTree.setVisible(false);
                    break;
                case HIDE_INCOME:
                    incomeTree.setVisible(false);
                    break;
                case HIDE_EXPENSE:
                    expenseTree.setVisible(false);
                    break;
            }
        }
    }

    protected void clearTrees() {
        assetTree.clear();
        liabilityTree.clear();
        incomeTree.clear();
        expenseTree.clear();
    }

    protected void setTreesMultiSelectEnabled(boolean enabled) {
        assetTree.setMultiSelectEnabled(enabled);
        liabilityTree.setMultiSelectEnabled(enabled);
        incomeTree.setMultiSelectEnabled(enabled);
        expenseTree.setMultiSelectEnabled(enabled);
    }

    protected void setTreesMultiSelected(boolean selected) {
        assetTree.setMultiSelected(selected);
        liabilityTree.setMultiSelected(selected);
        incomeTree.setMultiSelected(selected);
        expenseTree.setMultiSelected(selected);
    }

    protected void updateTrees(boolean expandAll) {
        for (Account account : accounts) {

            switch (account.getType()) {
                case Asset:
                case BankAccount:
                    assetTree.addAccount(account, expandAll);
                    break;
                case Liability:
                    liabilityTree.addAccount(account, expandAll);
                    break;
                case Income:
                    incomeTree.addAccount(account, expandAll);
                    break;
                case Expense:
                    expenseTree.addAccount(account, expandAll);
                    break;
            }
        }
        if (!Option.HIDE_ASSET.in(options))
            noAssetsLabel.setVisible(!assetTree.hasRootChildren());
        if (!Option.HIDE_LIABILITY.in(options))
            noLiabilitiesLabel.setVisible(!liabilityTree.hasRootChildren());
        if (!Option.HIDE_INCOME.in(options))
            noIncomeLabel.setVisible(!incomeTree.hasRootChildren());
        if (!Option.HIDE_EXPENSE.in(options))
            noExpenseLabel.setVisible(!expenseTree.hasRootChildren());

    }


    @UiHandler("newButton")
    void onClickNewAccountButton(ClickEvent e) {
        presenter.onNewAccount();
    }

    @UiHandler("filterClearButton")
    void onClickDescriptionFilterClear(ClickEvent e) {
        filterTextBox.clear();
        filterClearButton.setEnabled(false);
        if (filterTimer != null) filterTimer.cancel();
        if (presenter != null) {
            presenter.onFilter(null);
        }
    }

    protected void scheduleFilterReqest(Timer timer) {
        if (filterTimer != null) {
            filterTimer.cancel();
        }
        filterTimer = timer;
        filterTimer.schedule(1000);
    }

    protected String getFilterLabel() {
        return "Filter Accounts...";
    }

}
