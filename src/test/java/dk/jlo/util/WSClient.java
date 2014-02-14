package dk.jlo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class WSClient {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private String username;
    private String password;
    private HttpURLConnection urlConnection;

    public static WSClient forUrl(String urlString) throws IOException {
        WSClient wsClient = new WSClient();
        return wsClient.setup(urlString);
    }

    private WSClient setup(String urlString) throws IOException {
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        return this;
    }

    public void post(String requestAsString) throws IOException {
        logger.debug("POSTing request to {}.", urlConnection.getURL());
        urlConnection.setRequestMethod("POST");
        PrintWriter writer = new PrintWriter(urlConnection.getOutputStream());
        writer.print(requestAsString);
        if (writer.checkError()) {
            logger.debug("Some error occurred.");
        }
        writer.close();
        logger.debug("Request sent.");

        logger.debug("HTTP Response: {} {}.", getHttpResponseCode(), getHttpResponseMessage());
    }

    public WSClient usingIdentity(String username) {
        this.username = username;
        return this;
    }

    public WSClient andPassword(String password) {
        this.password = password;
        return this;
    }

    public WSClient forBasicAuth() {
        urlConnection.setRequestProperty("Authorization", "Basic " + DatatypeConverter.printBase64Binary((username + ":" + password).getBytes()));
        return this;
    }

    public WSClient usingRequestProperty(String propertyName, String propertyValue) {
        urlConnection.setRequestProperty(propertyName, propertyValue);
        return this;
    }

    public int getHttpResponseCode() throws IOException {
        return urlConnection.getResponseCode();
    }

    String getHttpResponseMessage() throws IOException {
        return urlConnection.getResponseMessage();
    }

    InputStream getErrorStream() {
        return urlConnection.getErrorStream();
    }

    InputStream getInputStream() throws IOException {
        return urlConnection.getInputStream();
    }

    public String getResponse() throws IOException {
        String response = "";
        logger.debug("Response stream:");
        String s;
        BufferedReader reader;
        reader = new BufferedReader(new InputStreamReader(getInputStream()));
        while ((s = reader.readLine()) != null) {
            logger.debug("Response Output: {}", s);
            response += s;
        }
        logger.debug("Response output.");

        return response;
    }

    public String getError() throws IOException {
        String s;
        BufferedReader reader;
        if (getErrorStream() == null) {
            logger.debug("No error stream.");
            return null;
        } else {
            String response = "";
            logger.debug("Error stream:");
            reader = new BufferedReader(new InputStreamReader(getErrorStream()));
            while ((s = reader.readLine()) != null) {
                logger.debug("WS Error: {}", s);
                response += s;
            }
            return response;
        }
    }

    public void teardown() {
        urlConnection.disconnect();
    }
}
