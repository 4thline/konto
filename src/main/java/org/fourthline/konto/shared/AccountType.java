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

package org.fourthline.konto.shared;

import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.AssetAccount;
import org.fourthline.konto.shared.entity.BankAccount;
import org.fourthline.konto.shared.entity.ExpenseAccount;
import org.fourthline.konto.shared.entity.IncomeAccount;
import org.fourthline.konto.shared.entity.LiabilityAccount;

import java.util.Arrays;
import java.util.List;

/**
* @author Christian Bauer
*/
public enum AccountType {

    Asset {
        @Override
        public Account instantiate() {
            return new AssetAccount();
        }
    },

    BankAccount(Asset, "Bank Account") {
        @Override
        public Account instantiate() {
            return new BankAccount();
        }
    },

    Liability {
        @Override
        public Account instantiate() {
            return new LiabilityAccount();
        }
    },

    Income("Increase", "Decrease") {
        @Override
        public Account instantiate() {
            return new IncomeAccount();
        }
    },

    Expense("Decrease", "Increase") {
        @Override
        public Account instantiate() {
            return new ExpenseAccount();
        }
    };

    public static AccountType[] getRootTypes() {
        return new AccountType[] {Asset, Liability, Income, Expense};
    }

    public static List<AccountType> getRootTypesAsList() {
        return Arrays.asList(getRootTypes());
    }

    AccountType rootType;
    String label;
    String debitLabel = "Debit";
    String creditLabel = "Credit";

    AccountType() {
        this.rootType = this;
        this.label = name();
    }

    AccountType(AccountType rootType) {
        this.rootType = rootType;
        this.label = name();
    }

    AccountType(String label) {
        this.label = label;
    }

    AccountType(AccountType rootType, String label) {
        this.rootType = rootType;
        this.label = label;
    }

    AccountType(String debitLabel, String creditLabel) {
        this.rootType = this;
        this.label = name();
        this.debitLabel = debitLabel;
        this.creditLabel = creditLabel;
    }
    AccountType(AccountType rootType, String debitLabel, String creditLabel) {
        this.rootType = rootType;
        this.label = name();
        this.debitLabel = debitLabel;
        this.creditLabel = creditLabel;
    }

    AccountType(AccountType rootType, String label, String debitLabel, String creditLabel) {
        this.rootType = rootType;
        this.label = label;
        this.debitLabel = debitLabel;
        this.creditLabel = creditLabel;
    }

    public AccountType getRootType() {
        return rootType;
    }

    public String getLabel() {
        return label;
    }

    public String getDebitLabel() {
        return debitLabel;
    }

    public String getCreditLabel() {
        return creditLabel;
    }

    public Account instantiate() {
        return null;
    }

}
