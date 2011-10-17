package org.fourthline.konto.test.mock;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.fourthline.konto.client.service.SettingsService;
import org.fourthline.konto.client.service.SettingsServiceAsync;
import org.seamless.gwt.validation.shared.ValidationException;
import org.fourthline.konto.shared.entity.settings.AccountOption;
import org.fourthline.konto.shared.entity.settings.GlobalOption;
import org.fourthline.konto.shared.entity.settings.Option;
import org.fourthline.konto.shared.entity.settings.Settings;

/**
 * @author Christian Bauer
 */
public class MockSettingsServiceAsync implements SettingsServiceAsync {

    public SettingsService svc;

    public MockSettingsServiceAsync(SettingsService svc) {
        this.svc = svc;
    }

    @Override
    public void getGlobalSettings(AsyncCallback<Settings<GlobalOption>> async) {
        async.onSuccess(svc.getGlobalSettings());
    }

    @Override
    public void getAccountSettings(Long accountId, AsyncCallback<Settings<AccountOption>> async) {
        async.onSuccess(svc.getAccountSettings(accountId));
    }

    @Override
    public void store(Settings<Option> settings, AsyncCallback<Void> async) {
        try {
            svc.store(settings);
            async.onSuccess(null);
        } catch (ValidationException ex) {
            async.onFailure(ex);
        }
    }

    @Override
    public void remove(Settings<Option> settings, AsyncCallback<Void> async) {
        svc.remove(settings);
        async.onSuccess(null);
    }

}
