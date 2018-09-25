/*
 GNU Lesser General Public License

 Copyright (c) 2017 Wimmer, Simon-Justus

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package org.sjwimmer.tacharting.implementation.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import org.sjwimmer.tacharting.chart.model.TaTimeSeries;
import org.sjwimmer.tacharting.chart.model.types.TimeFormatType;
import org.sjwimmer.tacharting.chart.model.types.YahooTimePeriod;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.sjwimmer.tacharting.implementation.model.api.CSVConnector;
import org.sjwimmer.tacharting.implementation.model.api.YahooSettingsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Connector class to request financial data from yahoo
 * following https://github.com/sstrickx/yahoofinance-api>
 */
public class YahooService extends Service<List<TaTimeSeries>> {

    private final Logger log = LoggerFactory.getLogger(YahooService.class);

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(TimeFormatType.YAHOO.pattern);
    private static final String REQ_BASE_URL = "https://query1.finance.yahoo.com/v7/finance/download/";
    private final Properties properties;
    private final String[] resources;


    public YahooService(String... resources){
        this.properties = YahooSettingsManager.getProperties();
        this.resources = resources;
    }

    @Override
    protected Task<List<TaTimeSeries>> createTask() {
        return new RequestTimeSeries();
    }



    class RequestTimeSeries extends Task<List<TaTimeSeries>>{

        @Override
        protected List<TaTimeSeries> call() throws Exception {
            String from = properties
                    .getProperty(Parameter.PROPERTY_YAHOO_FROM,
                            ZonedDateTime.now().minusYears(Parameter.DEFAULT_LOOK_BACK)
                                    .format(DateTimeFormatter.ofPattern(TimeFormatType.YAHOO.pattern)));
            LocalDate localDateFrom = LocalDate.parse(from, dateTimeFormatter);
            LocalDateTime dateTimeFrom = localDateFrom.atStartOfDay();

            String to = properties
                    .getProperty(Parameter.PROPERTY_YAHOO_TO, ZonedDateTime.now()
                            .format(DateTimeFormatter.ofPattern(TimeFormatType.YAHOO.pattern)));

            LocalDate localDateTo= LocalDate.parse(to, dateTimeFormatter);
            LocalDateTime dateTimeTo = localDateTo.atStartOfDay();


            List<TaTimeSeries> seriesList = new ArrayList<>();
            CSVConnector csvConnector = new CSVConnector();
            for(int i = 0; i < resources.length; i++) {
                String interval = YahooTimePeriod.of(properties.getProperty(Parameter.PROPERTY_YAHOO_INTERVAL, "1d")).toYahooString();
                Map<String, String> params = new LinkedHashMap<String, String>();
                params.put("period1", String.valueOf(dateTimeFrom.toEpochSecond(ZoneOffset.UTC)));
                params.put("period2", String.valueOf(dateTimeTo.toEpochSecond(ZoneOffset.UTC)));

                params.put("interval", interval);

                params.put("crumb", CrumbManager.getCrumb());
                Map<String, String> requestProperties = new HashMap<String, String>();
                requestProperties.put("Cookie", CrumbManager.getCookie());

                String symbol = resources[i];
                updateProgress(i, resources.length - 1);
                updateMessage("Request data for " + symbol);
                String url = REQ_BASE_URL + URLEncoder.encode(symbol, "UTF-8") + "?" + createURLParameters(params);
                URL request = new URL(url);
                HttpURLConnection connection = null;
                int redirects = 0;
                boolean hasResponse = false;
                URL currentRequest = request;
                while (!hasResponse && redirects < 5) {
                    connection = (HttpURLConnection) currentRequest.openConnection();
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    for (String property : requestProperties.keySet()) {
                        connection.addRequestProperty(property, requestProperties.get(property));
                    }
                    connection.setInstanceFollowRedirects(true);

                    switch (connection.getResponseCode()) {
                        case HttpURLConnection.HTTP_MOVED_PERM:
                        case HttpURLConnection.HTTP_MOVED_TEMP:
                            redirects++;
                            String location = connection.getHeaderField("Location");
                            currentRequest = new URL(request, location);
                            break;
                        default:
                            hasResponse = true;
                    }
                }

                if (redirects > 5) {
                    throw new IOException("Protocol redirect count exceeded for url: " + request.toExternalForm());
                } else if (connection == null) {
                    throw new IOException("Unexpected error while opening connection");
                } else {
                    InputStreamReader is = new InputStreamReader(connection.getInputStream());
                    BufferedReader br = new BufferedReader(is);
                    File file = new File(Parameter.PROGRAM_FOLDER + Parameter.S + symbol+"temp.csv");
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos));
                    String header = br.readLine();
                    bufferedWriter.write(header);
                    for (String line = br.readLine(); line != null; line = br.readLine()) {
                        bufferedWriter.newLine();
                        bufferedWriter.write(line);
                    }
                    bufferedWriter.close();

                    seriesList.add(csvConnector.getSeriesFromYahooFile(symbol, file));
                    log.debug("{}",file.delete());
                }
            }
            return seriesList;
        }

        private String createURLParameters(Map<String, String> params) {
            StringBuilder sb = new StringBuilder();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                String key = entry.getKey();
                String value = entry.getValue();
                try {
                    key = URLEncoder.encode(key, "UTF-8");
                    value = URLEncoder.encode(value, "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    log.debug(ex.getMessage());
                }
                sb.append(String.format("%s=%s", key, value));
            }
            return sb.toString();
        }
    }

    static class CrumbManager{
        private static Logger log = LoggerFactory.getLogger(CrumbManager.class);
        private static String crumb = "";
        private static String cookie = "";
        private static URL request;
        private static URL requestCookieURL;

        private static void setCrumb(){
            try{
                request = new URL("https://query1.finance.yahoo.com/v1/test/getcrumb");
                HttpURLConnection connection = null;
                int redirects = 0;
                boolean hasResponse = false;
                URL currentRequest = request;
                while(!hasResponse && redirects <= 5){
                    connection = (HttpURLConnection) currentRequest.openConnection();
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    connection.addRequestProperty("Cookie", cookie);
                    connection.setInstanceFollowRedirects(true);

                    switch (connection.getResponseCode()) {
                        case HttpURLConnection.HTTP_MOVED_PERM:
                        case HttpURLConnection.HTTP_MOVED_TEMP:
                            redirects++;
                            String location = connection.getHeaderField("Location");
                            currentRequest = new URL(request, location);
                            break;
                        default:
                            hasResponse = true;
                    }
                }

                if(redirects > 5) {
                    throw new IOException("Protocol redirect count exceeded for url: " + request.toExternalForm());
                } else if(connection == null) {
                    throw new IOException("Unexpected error while opening connection");
                }else {
                    InputStreamReader is = new InputStreamReader(connection.getInputStream());
                    BufferedReader br = new BufferedReader(is);
                    String crumbResult = br.readLine();

                    if(crumbResult != null && !crumbResult.isEmpty()) {
                        crumb = crumbResult.trim();
                        log.debug("Set crumb from http request: {}", crumb);
                    }else {
                        log.warn("Failed to set crumb from http request. Historical quote requests will most likely fail.");
                    }
                }
            } catch (IOException ioe){
                ioe.printStackTrace();
            }
        }

        private static void setCookie(){
            try{
                requestCookieURL = new URL("https://finance.yahoo.com/quote/%5EGSPC/options");
                HttpURLConnection connection = null;
                int redirects = 0;
                boolean hasResponse = false;
                URL currentRequest = requestCookieURL;
                while(!hasResponse && redirects <= 5){
                    connection = (HttpURLConnection) currentRequest.openConnection();
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    connection.setInstanceFollowRedirects(true);
                    switch (connection.getResponseCode()) {
                        case HttpURLConnection.HTTP_MOVED_PERM:
                        case HttpURLConnection.HTTP_MOVED_TEMP:
                            redirects++;
                            String location = connection.getHeaderField("Location");
                            currentRequest = new URL(request, location);
                            break;
                        default:
                            hasResponse = true;
                    }
                } // end while
                if(redirects > 5) {
                    throw new IOException("Protocol redirect count exceeded for url: " + request.toExternalForm());
                }else if(connection == null) {
                    throw new IOException("Unexpected error while opening connection");
                }else {
                    for(String headerKey : connection.getHeaderFields().keySet()) {
                        if("Set-Cookie".equalsIgnoreCase(headerKey)) {
                            for(String cookieField : connection.getHeaderFields().get(headerKey)) {
                                for(String cookieValue : cookieField.split(";")) {
                                    if(cookieValue.matches("B=.*")) {
                                        cookie = cookieValue;
                                        log.debug("Set cookie from http request: "+cookie);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    log.warn("Failed to get cookie!");
                    throw new IOException("Could not get Cookie from yahoo request");
                }

            }catch (MalformedURLException mue){
                mue.printStackTrace();
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
        }

        private static void refresh(){
            setCookie();
            setCrumb();
        }

        public static String getCookie(){
            if(cookie == null || cookie.isEmpty()){
                refresh();
            }
            return cookie;
        }

        public static synchronized String getCrumb() throws IOException {
            if(crumb == null || crumb.isEmpty()) {
                refresh();
            }
            return crumb;
        }
    }
}
