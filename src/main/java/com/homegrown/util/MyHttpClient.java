package com.homegrown.util;

import java.net.URL;
import java.util.Map;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import org.apache.log4j.Logger;

public class MyHttpClient {

    private static Logger logger = Logger.getLogger (MyHttpClient.class);

    private static final String USER_AGENT = "Mozilla/5.0";


    public static String sendGet (String url, int timeoutMills) throws Exception {
        return sendGet(url,null, timeoutMills);
    }

    public static String sendPost (String url, String body, int timeoutMillis) throws Exception {
        return sendPost (url,body,null,timeoutMillis);
    }
    /*
        public static String sendGet (String url, Map<String,String> headers) throws Exception {
            logger.info("sendGet | Sending 'GET' request to URL: " + url);
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(new URI(url));

            //request.addHeader("User-Agent", USER_AGENT);
            if (headers != null) {
                for (Map.Entry<String,String> entry : headers.entrySet()) {
                    request.addHeader(entry.getKey(),entry.getValue());
                }
            }

            HttpResponse response = client.execute(request);
            logger.info("sendGet | Response Code : " + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        }
    */
    public static String sendGet (String url, Map<String,String> headers, int timeoutMillis) throws Exception {
        logger.info("sendGet | Sending 'GET' request to URL: " + url);

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setConnectTimeout(timeoutMillis);
            con.setReadTimeout(timeoutMillis);
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Jakarta Commons-HttpClient/3.1");
            //con.setRequestProperty("Content-Length", String.valueOf(body.getBytes().length));
            //con.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    con.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            con.setDoInput(true);
        /*con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(body);
        wr.flush();
        wr.close();*/

            int responseCode = con.getResponseCode();
            logger.error("sendGet | Response Code : " + responseCode);

            String inputLine = "";
            StringBuilder response = new StringBuilder();

            long start = System.currentTimeMillis();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((inputLine = in.readLine()) != null) {
                if (System.currentTimeMillis() - start >= timeoutMillis) {
                    response.append(inputLine);
                    break;
                } else response.append(inputLine);
            }
            in.close();
            con.disconnect();
            return response.toString();
        }catch(SocketTimeoutException e){
            logger.error("sendGet | Timeout for 'GET' request to URL: " + url);
            return "";
        }
    }

    public static String sendPost (String url, String body, Map<String,String> headers, int timeoutMillis) throws Exception {
        logger.info("sendPost | Sending 'POST' request to URL: " + url);

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setConnectTimeout(timeoutMillis);
            con.setReadTimeout(timeoutMillis);
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Jakarta Commons-HttpClient/3.1");
            con.setRequestProperty("Content-Length", String.valueOf(body.getBytes().length));
            //con.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    con.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            con.setDoInput(true);
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(body);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            logger.error("sendPost | Response Code : " + responseCode);

            String inputLine = "";
            StringBuilder response = new StringBuilder();

            long start = System.currentTimeMillis();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((inputLine = in.readLine()) != null) {
                if (System.currentTimeMillis() - start >= timeoutMillis) {
                    response.append(inputLine);
                    break;
                } else response.append(inputLine);
            }
            in.close();
            con.disconnect();
            return response.toString();
        }catch(SocketTimeoutException e){
            logger.error("sendPost | Timeout for 'POST' request to URL: " + url);
            return "";
        }
    }
}
