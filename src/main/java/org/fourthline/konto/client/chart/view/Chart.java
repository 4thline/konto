package org.fourthline.konto.client.chart.view;

import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true, name = "Chart", namespace = GLOBAL)
public interface Chart {
    void destroy();
}