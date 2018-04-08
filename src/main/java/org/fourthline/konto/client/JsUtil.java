package org.fourthline.konto.client;

public class JsUtil {

    public native static void log(Object o) /*-{
        console.dir(o);
    }-*/;

    public native static void printType(Object o) /*-{
        console.log(({}).toString.call(o).match(/\s([a-zA-Z]+)/)[1].toLowerCase());
    }-*/;

    public native static String typeOf(Object o) /*-{
        var type = typeof o;
        if (type == 'object') {
            return typeof (o.valueOf());
        } else {
            return type;
        }
    }-*/;

}