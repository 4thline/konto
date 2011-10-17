package org.fourthline.konto.test.mock;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.fourthline.konto.client.service.LedgerService;
import org.fourthline.konto.client.service.LedgerServiceAsync;
import org.fourthline.konto.shared.entity.Account;
import org.fourthline.konto.shared.entity.AccountGroup;
import org.fourthline.konto.shared.AccountType;
import org.fourthline.konto.shared.query.AccountsQueryCriteria;
import org.fourthline.konto.shared.entity.Entry;
import org.fourthline.konto.shared.result.LedgerLines;
import org.fourthline.konto.shared.query.LedgerLinesQueryCriteria;
import org.seamless.gwt.validation.shared.ValidationException;

import java.util.List;

/**
 * @author Christian Bauer
 */
public class MockLedgerServiceAsync implements LedgerServiceAsync {

    public LedgerService svc;

    public MockLedgerServiceAsync(LedgerService svc) {
        this.svc = svc;
    }

    @Override
    public void getAccount(Long id, AsyncCallback<Account> async) {
        async.onSuccess(svc.getAccount(id));
    }

    @Override
    public void getAccountGroups(AccountType type, String name, AsyncCallback<List<AccountGroup>> async) {
        async.onSuccess(svc.getAccountGroups(type, name));

    }

    @Override
    public void getAccounts(AccountsQueryCriteria criteria, AsyncCallback<List<Account>> async) {
        async.onSuccess(svc.getAccounts(criteria));
    }

    @Override
    public void getLedgerLines(LedgerLinesQueryCriteria criteria, AsyncCallback<LedgerLines> async) {
        async.onSuccess(svc.getLedgerLines(criteria));
    }

    @Override
    public void populateSplits(Entry entry, AsyncCallback<Entry> async) {
        async.onSuccess(svc.populateSplits(entry));

    }

    @Override
    public void store(Entry entry, AsyncCallback<Void> async) {
        try {
            svc.store(entry);
            async.onSuccess(null);
        } catch (ValidationException ex) {
            async.onFailure(ex);
        }
    }

    @Override
    public void remove(Entry entry, AsyncCallback<Void> async) {
        svc.remove(entry);
        async.onSuccess(null);
    }

    @Override
    public void remove(Account account, AsyncCallback<Void> async) {
        svc.remove(account);
        async.onSuccess(null);
    }

    @Override
    public void store(Account account, AsyncCallback<Long> async) {
        try {
            svc.store(account);
            async.onSuccess(null);
        } catch (ValidationException ex) {
            async.onFailure(ex);
        }
    }

}
