package org.fourthline.konto.test;

import com.google.web.bindery.event.shared.Event;
import org.fourthline.konto.client.service.CurrencyService;
import org.fourthline.konto.client.service.CurrencyServiceAsync;
import org.fourthline.konto.client.service.LedgerService;
import org.fourthline.konto.client.service.LedgerServiceAsync;
import org.fourthline.konto.client.service.ReportService;
import org.fourthline.konto.client.service.ReportServiceAsync;
import org.fourthline.konto.client.service.SettingsService;
import org.fourthline.konto.client.service.SettingsServiceAsync;
import org.fourthline.konto.server.dao.AccountDAO;
import org.fourthline.konto.server.dao.CurrencyDAO;
import org.fourthline.konto.server.dao.EntryDAO;
import org.fourthline.konto.server.service.CurrencyServiceImpl;
import org.fourthline.konto.server.service.LedgerServiceImpl;
import org.fourthline.konto.server.service.ReportServiceImpl;
import org.fourthline.konto.server.service.SettingsServiceImpl;
import org.fourthline.konto.test.mock.MockCurrencyServiceAsync;
import org.fourthline.konto.test.mock.MockLedgerServiceAsync;
import org.fourthline.konto.test.mock.MockReportServiceAsync;
import org.fourthline.konto.test.mock.MockSettingsServiceAsync;
import org.seamless.gwt.notify.client.NotifyEvent;
import org.seamless.mock.gwt.MockEventBus;
import org.seamless.mock.gwt.MockPlaceController;

/**
 * @author Christian Bauer
 */
public class IntegrationTest extends HibernateTest {

    public AccountDAO accountDAO = new AccountDAO();
    public EntryDAO entryDAO = new EntryDAO();
    public CurrencyDAO currencyDAO = new CurrencyDAO();

    public MockEventBus eventBus = new MockEventBus();
    public MockPlaceController.MockDelegate placeControllerWindowDelegate = new MockPlaceController.MockDelegate();
    public MockPlaceController placeController = new MockPlaceController(eventBus, placeControllerWindowDelegate);

    public SettingsService settingsService = new SettingsServiceImpl();
    public SettingsServiceAsync settingsServiceAsync = new MockSettingsServiceAsync(settingsService);

    public LedgerService ledgerService = new LedgerServiceImpl();
    public LedgerServiceAsync ledgerServiceAsync = new MockLedgerServiceAsync(ledgerService);

    public CurrencyService currencyService = new CurrencyServiceImpl();
    public CurrencyServiceAsync currencyServiceAsync = new MockCurrencyServiceAsync(currencyService);

    public ReportService reportService = new ReportServiceImpl();
    public ReportServiceAsync reportServiceAsync = new MockReportServiceAsync(reportService);

    public void printEventBus() {
        System.err.println("Event Bus collected: " + eventBus.events.size());
        for (Event<?> event : eventBus.events) {
            if (event instanceof NotifyEvent) {
                NotifyEvent NotifyEvent = (NotifyEvent) event;
                System.out.println("MSG EVENT: " + NotifyEvent.getMessage());

            } else {
                System.out.println(event.getClass());
            }
        }
    }

}
