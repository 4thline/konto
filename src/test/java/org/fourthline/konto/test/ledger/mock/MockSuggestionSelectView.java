package org.fourthline.konto.test.ledger.mock;

import com.google.gwt.user.client.ui.Widget;
import org.seamless.gwt.component.client.suggest.Suggestion;
import org.seamless.gwt.component.client.suggest.SuggestionSelectView;
import org.seamless.gwt.validation.shared.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class MockSuggestionSelectView<S extends Suggestion> implements SuggestionSelectView<S> {

    public Presenter presenter;
    public String name;
    public List<S> suggestions = new ArrayList();
    public int selectedIndex;
    public boolean validationErrorVisible = false;

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void reset() {
        name = null;
        suggestions.clear();
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setSuggestions(List<S> suggestions, int selectedIndex) {
        this.suggestions = suggestions;
        this.selectedIndex = selectedIndex;
    }

    @Override
    public void showValidationError(ValidationError error) {
        this.validationErrorVisible = true;
    }

    @Override
    public void clearValidationError() {
        this.validationErrorVisible = false;
    }

    @Override
    public Widget asWidget() {
        return null;
    }
}
