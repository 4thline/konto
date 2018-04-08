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
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.UsableTree;
import org.fourthline.konto.shared.AccountType;
import org.fourthline.konto.shared.entity.Account;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class AccountTree extends UsableTree {

    public interface Resources extends UsableTree.Resources {

        @Source(Style.DEFAULT_CSS)
        Style style();

    }

    public interface Style extends CssResource {

        String DEFAULT_CSS = "org/fourthline/konto/client/ledger/component/AccountTree.css";

        String accountTree();

        String accountTypeLabel();

        String accountTypeLabelHover();

        String accountGroupLabel();

        String accountGroupHover();

        String accountLabel();

        String accountLabelHover();

        String accountMultiSelect();
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
    final AccountType type;

    MultiSelectableTreeItem root;

    public AccountTree(AccountType type) {
        this(type, getDefaultResources(), false);
    }

    public AccountTree(AccountType type, Resources resources) {
        this(type, resources, false);
    }

    public AccountTree(AccountType type, Resources resources, boolean useLeafImages) {
        super(resources, useLeafImages);
        this.type = type;
        this.resources = resources;
        this.style = resources.style();

        style.ensureInjected();
        addStyleName(style.accountTree());

        clear(); // Create the tree root
    }

    @Override
    public void setFocus(boolean focus) {
        // TODO https://github.com/gwtproject/gwt/issues/9475
    }

    @Override
    public void clear() {
        super.clear();
        // This is where the tree "model" is created
        root = new RootItem(type.getLabel());
        addItem(root);
    }

    public void addAccount(Account account, boolean expandAll) {

        if (account.getGroupId() == null) {
            root.addItem(new AccountItem(account));
            root.setState(expandAll);
            return;
        }

        GroupItem groupItem = null;
        // Try to find an existing group item
        for (int i = 0; i < root.getChildCount(); i++) {
            MultiSelectableTreeItem item = root.getChild(i);
            if (item instanceof GroupItem) {
                GroupItem gi = (GroupItem) item;
                if (gi.id.equals(account.getGroupId())) {
                    groupItem = gi;
                    break;
                }
            }
        }
        if (groupItem == null) {
            groupItem = new GroupItem(account.getGroupId(), account.getGroupName());
            root.addItem(groupItem);
        }
        groupItem.addItem(new AccountItem(account));

        root.setState(expandAll);
        groupItem.setState(expandAll);
    }

    public boolean hasRootChildren() {
        return root.getChildCount() > 0;
    }

    public List<Long> getSelectedAccounts() {
        List<Long> selectedIdentifiers = new ArrayList();
        for (MultiSelectableTreeItem item : root.getMultiSelected()) {
            if (item instanceof AccountItem) {
                AccountItem accountItem = (AccountItem) item;
                selectedIdentifiers.add(accountItem.getAccount().getId());
            }
        }
        return selectedIdentifiers;
    }

    public void setMultiSelectEnabled(final boolean enabled) {
        accept(new Visitor() {
            @Override
            public void visit(MultiSelectableTreeItem treeItem) {
                super.visit(treeItem);
                treeItem.setMultiSelectStyle(
                        enabled
                                ? style.accountMultiSelect()
                                : null
                );
            }
        });
    }

    public void setMultiSelected(final boolean selected) {
        accept(new Visitor() {
            @Override
            public void visit(MultiSelectableTreeItem treeItem) {
                super.visit(treeItem);
                treeItem.setMultiSelected(selected);
            }
        });
    }

    public void setMultiSelected(final List<Long> identifiers) {
        if (identifiers == null) { // Null means "all of the type", just like in the DAO
            setMultiSelected(true);
            return;
        }
        accept(new Visitor() {
            @Override
            public void visit(MultiSelectableTreeItem treeItem) {
                super.visit(treeItem);
                treeItem.setMultiSelected(false);
                if (treeItem instanceof AccountItem) {
                    AccountItem item = (AccountItem) treeItem;
                    if (identifiers.contains(item.getAccount().getId()))
                        treeItem.setMultiSelected(true);
                }
            }
        });
    }

    abstract public static class AccountSelectionHandler implements SelectionHandler<TreeItem> {

        public static class DefaultAccountSelectionHandler extends AccountSelectionHandler {
            @Override
            public boolean onAccountGroupSelection(Long id, String name) {
                return true;
            }

            @Override
            public boolean onAccountSelection(Account account) {
                return true;
            }

            @Override
            public boolean onSelection(AccountType type) {
                return true;
            }
        }

        @Override
        public void onSelection(SelectionEvent<TreeItem> event) {

            boolean cont = true;
            MultiSelectableTreeItem item = (MultiSelectableTreeItem) event.getSelectedItem();
            AccountTree tree = (AccountTree) item.getTree();

            if (item instanceof GroupItem) {
                GroupItem i = (GroupItem) item;
                cont = onAccountGroupSelection(i.id, i.name);
            } else if (item instanceof AccountItem) {
                AccountItem i = (AccountItem) item;
                cont = onAccountSelection(i.account);
            } else if (item.equals(tree.root)) {
                cont = onSelection(tree.type);
            }
            // If the subclass wants it, continue and expand/collapse item
            if (cont) {
                item.setState(!item.getState());
            }
        }

        public abstract boolean onAccountGroupSelection(Long id, String name);

        public abstract boolean onAccountSelection(Account account);

        public abstract boolean onSelection(AccountType type);
    }

    protected class AccountLabel extends Label {

        AccountLabel(String text, String style, final String hoverStyle) {
            super(text);
            addStyleName(style);
            addMouseOverHandler(new MouseOverHandler() {
                @Override
                public void onMouseOver(MouseOverEvent event) {
                    addStyleName(hoverStyle);
                }
            });
            addMouseOutHandler(new MouseOutHandler() {
                @Override
                public void onMouseOut(MouseOutEvent event) {
                    removeStyleName(hoverStyle);
                }
            });
        }
    }

    protected class RootItem extends MultiSelectableTreeItem {
        public RootItem(String label) {
            super(
                    new AccountLabel(label, style.accountTypeLabel(), style.accountTypeLabelHover()),
                    true
            );
        }

        @Override
        protected boolean isAddedToMultiSelection() {
            return false;
        }
    }

    protected class GroupItem extends MultiSelectableTreeItem {

        final Long id;
        final String name;

        public GroupItem(Long id, String name) {
            super(
                    new AccountLabel(name, style.accountGroupLabel(), style.accountGroupHover()),
                    true
            );
            this.id = id;
            this.name = name;
        }

        @Override
        protected boolean isAddedToMultiSelection() {
            return false;
        }
    }

    protected class AccountItem extends MultiSelectableTreeItem {

        final Account account;

        public AccountItem(Account account) {
            super(new AccountLabel(
                    account.getLabel(false, false, false, true),
                    style.accountLabel(),
                    style.accountLabelHover()
            ));
            this.account = account;
        }

        public Account getAccount() {
            return account;
        }
    }

    public static class Visitor implements TreeItemVisitor<MultiSelectableTreeItem> {
        @Override
        public void visit(MultiSelectableTreeItem treeItem) {
            if (treeItem instanceof RootItem) {
                visit((RootItem) treeItem);
            } else if (treeItem instanceof GroupItem) {
                visit((GroupItem) treeItem);
            } else if (treeItem instanceof AccountItem) {
                visit((AccountItem) treeItem);
            }
        }

        public void visit(RootItem item) {
        }

        public void visit(GroupItem item) {

        }

        public void visit(AccountItem item) {
        }
    }

}
