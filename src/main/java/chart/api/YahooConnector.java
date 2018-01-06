package chart.api;

import chart.parameters.Parameter;
import chart.settings.YahooSettingsManager;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;


public class YahooConnector{

    private static final Logger log = LoggerFactory.getLogger(YahooConnector.class);
    private static final String REQ_BASE_URL = "https://query1.finance.yahoo.com/v7/finance/download/";


    public File request(String symbol)throws IOException{

        Properties properties = YahooSettingsManager.getProperties();
        Map<String, String> params = new LinkedHashMap<String, String>();
        String from = properties.getProperty(Parameter.PROPERTY_YAHOO_FROM, ZonedDateTime.now().format(Parameter.FORMATTER_yyy_MM_dd));
        LocalDate localDateFrom = LocalDate.parse(from, Parameter.FORMATTER_yyy_MM_dd);
        LocalDateTime dateTimeFrom = localDateFrom.atStartOfDay();

        String to = properties.getProperty(Parameter.PROPERTY_YAHOO_TO, ZonedDateTime.now().format(Parameter.FORMATTER_yyy_MM_dd));
        LocalDate localDateTo= LocalDate.parse(to, Parameter.FORMATTER_yyy_MM_dd);
        LocalDateTime dateTimeTo = localDateTo.atStartOfDay();

        String interval = Parameter.YahooInterval.valueOf(properties.getProperty(Parameter.PROPERTY_YAHOO_INTERVAL, "daily")).toYahooString();
        params.put("period1", String.valueOf(dateTimeFrom.toEpochSecond(ZoneOffset.UTC)));
        params.put("period2", String.valueOf(dateTimeTo.toEpochSecond(ZoneOffset.UTC)));

        params.put("interval", interval);

        params.put("crumb", CrumbManager.getCrumb());
        String url = REQ_BASE_URL + URLEncoder.encode(symbol , "UTF-8") + "?" + createURLParameters(params);

        Map<String, String> requestProperties = new HashMap<String, String>();
        requestProperties.put("Cookie", CrumbManager.getCookie());

        URL request = new URL(url);
        HttpURLConnection connection = null;
        int redirects = 0;
        boolean hasResponse = false;
        URL currentRequest = request;
        while(!hasResponse && redirects < 5){
            connection = (HttpURLConnection) currentRequest.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            for(String property: requestProperties.keySet()){
                connection.addRequestProperty(property, requestProperties.get(property));
            }
            connection.setInstanceFollowRedirects(true);

            switch (connection.getResponseCode()){
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
        } else {
            InputStreamReader is = new InputStreamReader(connection.getInputStream());
            BufferedReader br = new BufferedReader(is);
            File file = new File("temp.csv");
            FileOutputStream fos = new FileOutputStream(file);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos));
            String header = br.readLine();
            bufferedWriter.write(header);
            bufferedWriter.newLine();
            bufferedWriter.write(String.format("%s %s, %s",symbol, Parameter.YahooInterval.of(interval),3));
            for(String line=br.readLine(); line != null;line = br.readLine()){
                bufferedWriter.newLine();
                bufferedWriter.write(line);
            }
            bufferedWriter.close();

            return file;
        }
    }

    public static String createURLParameters(Map<String, String> params) {
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
                // Still try to continue with unencoded values
            }
            sb.append(String.format("%s=%s", key, value));
        }
        return sb.toString();
    }

    static class CrumbManager{
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
                }else if(connection == null) {
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
            }catch (MalformedURLException mue){
                mue.printStackTrace();
            }catch (IOException ioe){
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
                    log.warn("Failed to set cookie from http request. Historical quote requests will most likely fail.");
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
