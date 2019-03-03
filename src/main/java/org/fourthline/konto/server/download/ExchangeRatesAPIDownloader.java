/*
 * Copyright (C) 2018 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.fourthline.konto.server.download;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.fourthline.konto.server.dao.CurrencyDAO;
import org.fourthline.konto.shared.entity.CurrencyPair;
import org.seamless.util.io.IO;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * @author Christian Bauer
 */
public class ExchangeRatesAPIDownloader extends CurrencyDownloader {

    public static final int BATCH_SIZE = 100;

    public static final String BASE_URL =
        "https://api.exchangeratesapi.io/latest?base=%s&symbols=%s";

    public ExchangeRatesAPIDownloader(CurrencyDAO currencyDAO) {
        super(currencyDAO);
    }

    @Override
    protected void updateExchangeRates(List<CurrencyPair> pairs) throws Exception {

        Map<String, List<String>> pairGroups = new HashMap<>();
        for (CurrencyPair pair : pairs) {
            if (!pairGroups.containsKey(pair.getFromCode())) {
                pairGroups.put(pair.getFromCode(), new ArrayList<>());
            }
            List<String> group = pairGroups.get(pair.getFromCode());
            group.add(pair.getToCode());
        }

        for (Map.Entry<String, List<String>> entry : pairGroups.entrySet()) {

            StringBuilder groupString = new StringBuilder();
            for (String s : entry.getValue()) {
                groupString.append(s).append(",");
            }
            if (groupString.length() > 0)
                groupString.deleteCharAt(groupString.length()-1);

            URL url = new URL(String.format(BASE_URL, entry.getKey(), groupString));
            String result = retrieveExchangeRates(url);
            if (result == null)
                return;
            JsonValue json = Json.parse(result);

            /* Result is:
            {
                "rates": {
                    "EUR": 0.8800492828,
                    "USD": 0.9894033444
                },
                "base": "CHF",
                "date": "2019-03-01"
            }
            */
            for (CurrencyPair pair : pairs) {
                if (json.asObject().get("base").asString().equals(pair.getFromCode())) {
                    JsonObject rates = json.asObject().get("rates").asObject();
                    for (String rateName : rates.names()) {
                        if (rateName.equals((pair.getToCode()))) {
                            pair.setExchangeRate(
                                new BigDecimal(rates.get(rateName).asDouble()).setScale(CurrencyPair.SCALE, RoundingMode.HALF_UP)
                            );
                        }
                    }
                }
            }
        }
    }

    protected String retrieveExchangeRates(URL url) throws Exception {
        HttpURLConnection urlConnection = null;
        InputStream inputStream;
        try {

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10 * 1000);
            urlConnection.setConnectTimeout(10 * 1000);

            inputStream = urlConnection.getInputStream();
            return readResponse(urlConnection, inputStream);

        } catch (IOException ex) {

            if (urlConnection == null) {
                throw new Exception("Could not open URL connection: " + ex, ex);
            }

            try {
                inputStream = urlConnection.getErrorStream();
            } catch (Exception errorEx) {
                throw new Exception("Could not read error stream: " + errorEx);
            }
            if (inputStream != null) {
                // Will throw exception with status >= 300
                readResponse(urlConnection, inputStream);
                return null;
            } else {
                throw new Exception("I/O exception occurred, no error response: " + ex, ex);
            }

        } catch (Exception ex) {
            throw new Exception("Unrecoverable exception occurred, no error response possible: " + ex, ex);

        } finally {
            if (urlConnection != null) {
                // Release any idle persistent connection, or "indicate that we don't
                // want to use this server for a while"
                urlConnection.disconnect();
            }
        }
    }

    protected String readResponse(HttpURLConnection urlConnection, InputStream inputStream) throws Exception {
        if (urlConnection.getResponseCode() == -1) {
            throw new Exception("Did not receive valid HTTP response");
        }

        // Status
        int statusCode = urlConnection.getResponseCode();
        String statusMessage = urlConnection.getResponseMessage();
        if (statusCode >= 300) {
            throw new Exception("Response status was: " + statusCode + " " + statusMessage);
        }

        // Body
        byte[] bodyBytes = null;
        InputStream is = null;
        try {
            is = inputStream;
            if (inputStream != null) bodyBytes = IO.readBytes(is);
        } finally {
            if (is != null)
                is.close();
        }
        if (bodyBytes != null && bodyBytes.length > 0) {
            return new String(bodyBytes, "UTF-8");
        } else {
            throw new Exception("Response did not contain body");
        }
    }
}
