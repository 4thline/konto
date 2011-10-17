package org.fourthline.konto.test.mock;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.fourthline.konto.client.service.ReportService;
import org.fourthline.konto.client.service.ReportServiceAsync;
import org.fourthline.konto.shared.query.LineReportCriteria;
import org.fourthline.konto.shared.result.ReportLines;

/**
 * @author Christian Bauer
 */
public class MockReportServiceAsync implements ReportServiceAsync {

    public ReportService svc;

    public MockReportServiceAsync(ReportService svc) {
        this.svc = svc;
    }

    @Override
    public void getReportLines(LineReportCriteria criteria, AsyncCallback<ReportLines[]> async) {
        async.onSuccess(svc.getReportLines(criteria));
    }

}
