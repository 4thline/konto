package org.fourthline.konto.test.ledger.mock;

import com.google.gwt.user.client.ui.Widget;
import org.seamless.gwt.component.client.binding.ValidatableViewProperty;
import org.fourthline.konto.client.ledger.entry.view.EntrySummaryView;
import org.fourthline.konto.client.ledger.entry.view.EntryView;
import org.fourthline.konto.client.ledger.entry.view.SplitView;
import org.seamless.util.time.DateFormat;
import org.seamless.gwt.component.client.binding.MockValidatableViewProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class MockEntryView implements EntryView {

    public Presenter presenter;
    public DateFormat dateFormat;
    public boolean focus = false;
    public Boolean selectDay;
    public MockValidatableViewProperty<Date> effectiveOnProperty = new MockValidatableViewProperty<Date>();
    public EntrySummaryView entrySummaryView;
    public boolean splitDeleteVisible = false;
    public boolean splitAddVisible = true;
    public List<SplitView> splitViews = new ArrayList();

    @Override
    public void focus(Boolean selectDay) {
        this.focus = true;
        this.selectDay = selectDay;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public void reset() {
        focus = false;
        effectiveOnProperty.reset();
        entrySummaryView = null;
        splitDeleteVisible = false;
        splitAddVisible = true;
        splitViews.clear();
    }

    @Override
    public ValidatableViewProperty<Date> getEffectiveOnProperty() {
        return effectiveOnProperty;
    }

    @Override
    public void showEntrySummaryView(EntrySummaryView summaryView) {
        this.entrySummaryView = summaryView;
    }

    @Override
    public void removeEntrySummaryView() {
        entrySummaryView = null;
    }

    @Override
    public void hideSplitDelete() {
        splitDeleteVisible = false;
    }

    @Override
    public void showSplitDelete() {
        splitDeleteVisible = true;
    }

    @Override
    public void hideSplitAdd() {
        splitAddVisible = false;
    }

    @Override
    public void showSplitAdd() {
        splitAddVisible = true;
    }

    @Override
    public void addSplitView(SplitView splitView) {
        splitViews.add(splitView);
    }

    @Override
    public void removeSplitView(int index) {
        splitViews.remove(index);
    }

    @Override
    public Widget asWidget() {
        return null;
    }
}
