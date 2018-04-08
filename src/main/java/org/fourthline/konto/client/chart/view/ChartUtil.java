package org.fourthline.konto.client.chart.view;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import org.fourthline.konto.shared.result.ChartDataPoint;
import org.fourthline.konto.shared.result.ChartDataPoints;

public class ChartUtil {

    static public JavaScriptObject convertLabels(ChartDataPoints chartDataPoints) {
        JsArrayString array = (JsArrayString) JsArrayString.createArray();
        for (int i = 0; i < chartDataPoints.size(); i++) {
            ChartDataPoint chartDataPoint = chartDataPoints.get(i);
            array.set(i, chartDataPoint.getYear() + "-" + chartDataPoint.getMonth());
        }
        return array;
    }

    static public JavaScriptObject convertData(ChartDataPoints chartDataPoints) {
        JSONArray array = new JSONArray();
        for (int x = 0; x < chartDataPoints.size(); x++) {
            ChartDataPoint chartDataPoint = chartDataPoints.get(x);
            Number number = chartDataPoint.getMonetaryAmount().getValue();
            if (number != null) {
                array.set(x, new JSONNumber(number.doubleValue()));
            } else {
                array.set(x, null);
            }
        }
        return array.getJavaScriptObject();
    }

    public native static void update(Chart chart, JavaScriptObject labels, JavaScriptObject data) /*-{
        chart.data.labels = labels;
        chart.data.datasets[0].data = data;
        chart.update();
    }-*/;

    public native static Chart createLineChart(Context2d canvasContext) /*-{

        return new $wnd.Chart(canvasContext, {
            type: 'line',
            data: {
                datasets: [{
                    backgroundColor: "rgba(193, 215, 47, 0.1)",
                    borderColor: "#aaaaaa",
                    pointBorderColor: "#cccccc",
                    pointBackgroundColor: "#374c4f",
                    pointHoverBackgroundColor: "#374c4f",
                    pointHoverBorderColor: "#aaaaaa",
                    pointBorderWidth: 2,
                    pointHoverRadius: 4,
                    pointHoverBorderWidth: 4,
                    pointRadius: 4,
                    pointHitRadius: 20,
                    spanGaps: true
                }]
            },
            options: {
                legend: {
                    display: false
                },
                tooltips: {
                    backgroundColor: "rgba(0,0,0,0.8)",
                    titleFontFamily: "'Open Sans', Helvetica, Arial, Lucida, sans-serif",
                    titleFontColor: "#ffffff",
                    titleFontSize: 10,
                    titleMarginBottom: 4,
                    bodyFontFamily: "'Open Sans', Helvetica, Arial, Lucida, sans-serif",
                    bodyFontColor: "#cccccc",
                    bodyFontSize: 16,
                    displayColors: false,
                    xPadding: 10,
                    yPadding: 10,
                    footerFontSize: 0,
                    callbacks: {
                        label: function (tooltipItem, data) {
                            return tooltipItem.yLabel.toLocaleString('en') ; // Removes the colon before the label, formats number
                        },
                        footer: function () {
                            return " "; // Hack the broken vertical alignment of body with footerFontSize: 0
                        }
                    }
                },
                scales: {
                    yAxes: [{
                        ticks: {
                            fontColor: "#000",
                            fontFamily: "'Open Sans', Helvetica, Arial, Lucida, sans-serif",
                            fontSize: 12.5,
                            fontStyle: "normal",
                            beginAtZero: true
                        },
                        gridLines: {
                            color: "#cccccc"
                        }
                    }],
                    xAxes: [{
                        ticks: {
                            autoSkip: true,
                            maxTicksLimit: 30,
                            fontColor: "#000",
                            fontFamily: "'Open Sans', Helvetica, Arial, Lucida, sans-serif",
                            fontSize: 10,
                            fontStyle: "normal"
                        },
                        gridLines: {
                            color: "#cccccc"
                        }
                    }]
                }
            }
        });
    }-*/;
}