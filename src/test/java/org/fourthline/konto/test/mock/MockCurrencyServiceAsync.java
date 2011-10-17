package org.fourthline.konto.test.mock;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.fourthline.konto.client.service.CurrencyService;
import org.fourthline.konto.client.service.CurrencyServiceAsync;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.fourthline.konto.shared.entity.MonetaryUnit;
import org.seamless.gwt.validation.shared.ValidationException;

import java.util.Date;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class MockCurrencyServiceAsync implements CurrencyServiceAsync {

    public CurrencyService svc;

    public MockCurrencyServiceAsync(CurrencyService svc) {
        this.svc = svc;
    }

    @Override
    public void getMonetaryUnits(AsyncCallback<List<MonetaryUnit>> async) {
        async.onSuccess(svc.getMonetaryUnits());
    }

    @Override
    public void getCurrencyPair(MonetaryUnit fromUnit, MonetaryUnit toUnit, Date forDay, AsyncCallback<CurrencyPair> async) {
        async.onSuccess(svc.getCurrencyPair(fromUnit, toUnit, forDay));
    }

    @Override
    public void store(MonetaryUnit unit, AsyncCallback<Void> async) {
        try {
            svc.store(unit);
            async.onSuccess(null);
        } catch (ValidationException ex) {
            async.onFailure(ex);
        }
    }

    @Override
    public void remove(MonetaryUnit unit, AsyncCallback<Boolean> async) {
        async.onSuccess(svc.remove(unit));
    }

    @Override
    public void getCurrencyPairs(MonetaryUnit fromUnit, MonetaryUnit toUnit, AsyncCallback<List<CurrencyPair>> async) {
        async.onSuccess(svc.getCurrencyPairs(fromUnit, toUnit));
    }

    @Override
    public void store(CurrencyPair pair, AsyncCallback<Void> async) {
        try {
            svc.store(pair);
            async.onSuccess(null);
        } catch (ValidationException ex) {
            async.onFailure(ex);
        }
    }

    @Override
    public void remove(CurrencyPair pair, AsyncCallback<Void> async) {
        svc.remove(pair);
        async.onSuccess(null);
    }

    @Override
    public void removeAll(CurrencyPair pair, AsyncCallback<Void> async) {
        svc.removeAll(pair);
        async.onSuccess(null);
    }

    @Override
    public void download(CurrencyPair pair, AsyncCallback<String> async) {
        async.onSuccess(svc.download(pair));
    }
}
