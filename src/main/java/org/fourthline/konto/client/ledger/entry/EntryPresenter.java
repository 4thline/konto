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

package org.fourthline.konto.client.ledger.entry;

import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import javax.inject.Inject;
import com.google.inject.Provider;
import org.seamless.gwt.component.client.AbstractEventListeningPresenter;
import org.fourthline.konto.client.service.LedgerServiceAsync;
import org.fourthline.konto.client.ledger.entry.event.EntryEditAmountUpdated;
import org.fourthline.konto.client.ledger.entry.event.EntryEditCanceled;
import org.fourthline.konto.client.ledger.entry.event.EntryEditStarted;
import org.fourthline.konto.client.ledger.entry.event.EntryEditSubmit;
import org.fourthline.konto.client.ledger.entry.event.EntryModified;
import org.fourthline.konto.client.ledger.entry.event.EntryRemoved;
import org.fourthline.konto.client.ledger.entry.view.EntrySummaryView;
import org.fourthline.konto.client.ledger.entry.view.EntryView;
import org.fourthline.konto.client.ledger.entry.view.SplitView;
import org.seamless.gwt.notify.client.Message;
import org.seamless.gwt.notify.client.ServerFailureNotifyEvent;
import org.seamless.gwt.notify.client.NotifyEvent;
import org.seamless.gwt.notify.client.ValidationErrorNotifyEvent;
import org.fourthline.konto.client.settings.GlobalSettings;
import org.fourthline.konto.client.settings.event.GlobalSettingsRefreshedEvent;
import org.fourthline.konto.shared.LedgerEntry;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.DebitCreditHolder;
import org.fourthline.konto.shared.entity.Entry;
import org.fourthline.konto.shared.MonetaryAmount;
import org.fourthline.konto.shared.entity.Split;
import org.seamless.gwt.validation.shared.Validatable;
import org.seamless.gwt.validation.shared.ValidationError;
import org.seamless.gwt.validation.shared.ValidationException;
import org.fourthline.konto.shared.entity.settings.GlobalOption;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Christian Bauer
 */
public class EntryPresenter
        extends AbstractEventListeningPresenter
        implements
        EntryView.Presenter,
        GlobalSettingsRefreshedEvent.Handler,
        EntryEditSubmit.Handler,
        EntryEditAmountUpdated.Handler {

    final EntryView view;
    final EventBus bus;
    final LedgerServiceAsync service;

    GlobalSettings globalSettings;
    Account currentAccount;
    Entry entry;

    // Sub-presenters
    final Provider<SplitView.Presenter> splitPresenterProvider;
    final Provider<EntrySummaryView.Presenter> entrySummaryPresenterProvider;

    final List<SplitView.Presenter> splitPresenters = new ArrayList();
    EntrySummaryView.Presenter entrySummaryPresenter;

    @Inject
    public EntryPresenter(EntryView view,
                          Provider<SplitView.Presenter> splitPresenterProvider,
                          Provider<EntrySummaryView.Presenter> entrySummaryPresenterProvider,
                          EventBus bus,
                          LedgerServiceAsync service,
                          GlobalSettings globalSettings) {

        this.view = view;
        this.splitPresenterProvider = splitPresenterProvider;
        this.entrySummaryPresenterProvider = entrySummaryPresenterProvider;
        this.bus = bus;
        this.service = service;

        addRegistration(bus.addHandler(GlobalSettingsRefreshedEvent.TYPE, this));
        addRegistration(bus.addHandler(EntryEditSubmit.TYPE, this));
        addRegistration(bus.addHandler(EntryEditAmountUpdated.TYPE, this));

        onSettingsRefreshed(globalSettings);
    }

    public EntrySummaryView.Presenter getEntrySummaryPresenter() {
        return entrySummaryPresenter;
    }

    public List<SplitView.Presenter> getSplitPresenters() {
        return splitPresenters;
    }

    public Entry getEntry() {
        return entry;
    }

    @Override
    public void onSettingsRefreshed(GlobalSettings gs) {
        this.globalSettings = gs;
        view.setDateFormat(gs.getValue(GlobalOption.OPT_DATE_FORMAT));
    }

    @Override
    public EntryView getView() {
        return view;
    }

    @Override
    public void startWith(Account currentAccount, LedgerEntry ledgerEntry, Date lastLedgerEntryDate) {

        this.currentAccount = currentAccount;

        Split split = null;
        if (ledgerEntry == null) {
            // New entry and one new split (each entry has at least one split)
            entry = new Entry();
            entry.setEffectiveOn(lastLedgerEntryDate);
            entry.setAccountId(currentAccount.getId());
            Split newSplit = new Split(currentAccount.getMonetaryUnit(), currentAccount.getMonetaryUnit());
            newSplit.setEntry(entry);
            entry.getSplits().add(newSplit);
        } else if (ledgerEntry instanceof Entry) {
            entry = (Entry) ledgerEntry;
        } else {
            split = (Split) ledgerEntry;
            entry = split.getEntry();
        }

        view.setPresenter(this);
        view.reset();
        view.getEffectiveOnProperty().set(entry.getEffectiveOn());

        // Are we looking at an account where this entry was made or the opposite side?
        if (split == null) {

            // The "onwing" side, we are looking at the same account on which this
            // entry was made or we are making a new entry on this account, show all the
            // splits of this entry
            for (Split s : entry.getSplits()) {
                addSplitPresenter(s);
            }

            // Allow addition of new splits
            view.showSplitAdd();

            // If there is more than one split we have to update the summary line
            updateEntrySummaryDebitCredit();

            bus.fireEvent(new EntryEditStarted(entry, splitPresenters.size() > 1));
        } else {

            // We are looking at the "other" side, this is just a split of potentially
            // many splits of this entry. The entry was made on a different account
            addSplitPresenter(split);

            // Deny adding new splits
            view.hideSplitAdd();

            bus.fireEvent(new EntryEditStarted(split, splitPresenters.size() > 1));
        }

        if (entry.getId() == null) {
            view.focus(globalSettings.getValue(GlobalOption.OPT_NEW_ENTRY_SELECT_DAY));
        } else {
            focusSplit(true);
        }

    }

    protected void addSplitPresenter(Split split) {
        SplitView.Presenter splitPresenter = splitPresenterProvider.get();
        splitPresenter.startWith(currentAccount, split);
        if (view.getEffectiveOnProperty().get() != null)
            splitPresenter.setEffectiveDate(view.getEffectiveOnProperty().get());
        splitPresenters.add(splitPresenter);
        view.addSplitView(splitPresenter.getView());
        if (splitPresenters.size() > 1) {
            // Second (or more) split added, need to show delete button
            view.showSplitDelete();
            // .. update the entry description with the first split's description
            if (entry.getDescription() == null)
                entry.setDescription(
                        splitPresenters.get(0).getView().getDescription().length() > 0
                        ? splitPresenters.get(0).getView().getDescription() : null
                );
            // ... show the entry summary
            createEntrySummaryPresenter();
            // .. and update the summary line's amounts
            updateEntrySummaryDebitCredit();
        }
    }

    protected void removeSplitPresenter(int index) {
        splitPresenters.remove(index);
        view.removeSplitView(index);
        if (splitPresenters.size() < 2) {
            // Only one left, can't remove that, hide delete button
            view.hideSplitDelete();
            // .. but keep the description of the summary
            if (entrySummaryPresenter.getView().getDescription().length() > 0) {
                splitPresenters.get(0).getView().setDescription(
                        entrySummaryPresenter.getView().getDescription()
                );
            }
            // ..  and remove summary
            removeEntrySummaryPresenter();
        }
    }

    protected void createEntrySummaryPresenter() {
        if (entrySummaryPresenter == null) {
            entrySummaryPresenter = entrySummaryPresenterProvider.get();
            entrySummaryPresenter.startWith(currentAccount, entry);
            view.showEntrySummaryView(entrySummaryPresenter.getView());
        }
    }

    protected void removeEntrySummaryPresenter() {
        entrySummaryPresenter = null;
        view.removeEntrySummaryView();

    }

    @Override
    public void dateEntered(Date date) {
        for (SplitView.Presenter splitPresenter : splitPresenters) {
            splitPresenter.setEffectiveDate(date);
        }
    }

    @Override
    public void addSplit() {

        Split split = new Split(currentAccount.getMonetaryUnit(), currentAccount.getMonetaryUnit());
        split.setEntry(entry);
        entry.getSplits().add(split);

        addSplitPresenter(split);

        bus.fireEvent(new EntryEditStarted(entry, splitPresenters.size() > 1, true));
        focusSplit(splitPresenters.size() > 2);
    }

    @Override
    public void removeSplit(int index) {
        if (splitPresenters.size() == 1) return; // Can't delete last one,

        SplitView.Presenter splitPresenter = splitPresenters.get(index);

        Split split = splitPresenter.getSplit();
        split.getEntry().getSplits().remove(split);
        if (split.getId() != null) {
            split.getEntry().getOrphanedSplits().add(split);
        }

        removeSplitPresenter(index);

        if (splitPresenters.size() == 1) {
            bus.fireEvent(new EntryEditStarted(entry, false));
        }
        focusSplit(true);
    }

    @Override
    public void onEntryEditSubmit(EntryEditSubmit event) {
        saveEntry();
    }

    @Override
    public void onEntryEditAmountUpdated(EntryEditAmountUpdated event) {
        updateEntrySummaryDebitCredit();
    }

    @Override
    public void saveEntry() {
        clearValidationErrors();

        // Client-side validation including view/model data binding
        List<ValidationError> errors = flushView();
        if (errors.size() > 0) {
            bus.fireEvent(new NotifyEvent(
                    new Message(Level.WARNING, "Can't save entry", "Please correct your input.")
            ));
            showValidationErrors(errors);
            return;
        }

        service.store(entry, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof ValidationException) {
                    ValidationException ex = (ValidationException) caught;

                    // This is probably a FK violation
                    if (!ex.hasErrors()) {
                        bus.fireEvent(new NotifyEvent(
                                new Message(
                                        Level.WARNING,
                                        "Can't save entry, errors on server",
                                        ex.getMessage()
                                )
                        ));
                    }

                    showValidationErrors(ex.getErrors());
                } else {
                    bus.fireEvent(new ServerFailureNotifyEvent(caught));
                }
            }

            @Override
            public void onSuccess(Void result) {
                bus.fireEvent(new EntryModified(entry));
            }
        });
    }

    @Override
    public void deleteEntry() {
        service.remove(entry, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                bus.fireEvent(new ServerFailureNotifyEvent(caught));
            }

            @Override
            public void onSuccess(Void result) {
                bus.fireEvent(new EntryRemoved(entry));
            }
        });
    }

    @Override
    public void cancel() {
        bus.fireEvent(new EntryEditCanceled());
    }

    public void clearValidationErrors() {
        view.getEffectiveOnProperty().clearValidationError();
        for (SplitView.Presenter splitPresenter : splitPresenters) {
            splitPresenter.clearValidationErrors();
        }
        if (entrySummaryPresenter != null) {
            entrySummaryPresenter.clearValidationErrors();
        }
    }

    public void showValidationErrors(List<ValidationError> errors) {

        // Seperate the Split errors from the Entry errors (many entries)
        Map<Integer, List<ValidationError>> splitErrors = new HashMap();

        List<ValidationError> entrySummaryErrors = new ArrayList();

        for (ValidationError error : errors) {

            // Find out which index (error id) is given, so we can pick the right sub-presenter
            // for displaying the error
            if (Split.class.getName().equals(error.getEntity()) && error.getId() != null) {
                int splitIndex = Integer.valueOf(error.getId());
                List<ValidationError> splitErrorList = splitErrors.get(splitIndex);
                if (splitErrorList == null) {
                    splitErrorList = new ArrayList();
                    splitErrors.put(splitIndex, splitErrorList);
                }
                splitErrorList.add(error);

            } else if (Entry.class.getName().equals(error.getEntity()) &&
                    Entry.Property.effectiveOn.equals(error.getProperty())) {

                view.getEffectiveOnProperty().showValidationError(error);
                bus.fireEvent(new NotifyEvent(
                        new Message(Level.WARNING, "Invalid date", error.getMessage())
                ));

            } else if (Entry.class.getName().equals(error.getEntity()) &&
                    Entry.Property.description.equals(error.getProperty())) {

                entrySummaryErrors.add(error);

            } else {
                bus.fireEvent(new ValidationErrorNotifyEvent(error));
            }
        }

        // Show errors for each sub-presenter

        if (entrySummaryPresenter != null) {
            entrySummaryPresenter.showValidationErrors(entrySummaryErrors);
        }

        for (Map.Entry<Integer, List<ValidationError>> me : splitErrors.entrySet()) {
            if (splitPresenters.size() >= me.getKey()) {
                SplitView.Presenter splitPresenter = splitPresenters.get(me.getKey());
                if (splitPresenter != null) splitPresenter.showValidationErrors(me.getValue());
            }
        }

    }

    protected void focusSplit(boolean lastSplit) {
        if (splitPresenters.size() == 0) return;
        splitPresenters.get(
                lastSplit ? splitPresenters.size() - 1 : 0
        ).getView().focus();
    }

    protected List<ValidationError> flushView() {
        List<ValidationError> errors = new ArrayList();

        Date effectiveOn = view.getEffectiveOnProperty().get();
        if (effectiveOn == null) effectiveOn = new Date();
        if (effectiveOn.getTime() < currentAccount.getEffectiveOn().getTime()) {
            errors.add(new ValidationError(
                    Entry.class.getName(),
                    Entry.Property.effectiveOn,
                    "Date of entry can't be before account's starting date."
            ));
        } else {
            entry.setEffectiveOn(effectiveOn);
        }
        entry.setEnteredOn(new Date());

        // Flush sub-presenters and give them their index so they can add validation errors
        for (int i = 0; i < splitPresenters.size(); i++) {
            SplitView.Presenter splitPresenter = splitPresenters.get(i);
            errors.addAll(splitPresenter.flushView(i));
        }

        if (entrySummaryPresenter != null) {
            errors.addAll(entrySummaryPresenter.flushView());
        }

        // If this entry has a single split, transfer the description
        if (entry.getSplits().size() == 1) {
            entry.setDescription(entry.getSplits().get(0).getDescription());
        }

        // Flushing was OK (that included data binding), now model integrity validation
        if (errors.size() == 0) {
            errors.addAll(entry.validate(Validatable.GROUP_CLIENT));
        }

        return errors;
    }

    protected void updateEntrySummaryDebitCredit() {
        if (entrySummaryPresenter == null) return;

        MonetaryAmount sum = new MonetaryAmount(currentAccount.getMonetaryUnit());

        for (SplitView.Presenter splitPresenter : splitPresenters) {
            sum = sum.add(DebitCreditHolder.Accessor.getDebitOrCredit(splitPresenter.getView()));
        }

        DebitCreditHolder.Accessor.setDebitOrCredit(entrySummaryPresenter, sum);

    }


}
