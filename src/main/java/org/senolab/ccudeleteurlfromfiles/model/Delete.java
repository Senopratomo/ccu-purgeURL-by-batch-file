package org.senolab.ccudeleteurlfromfiles.model;

import com.akamai.edgegrid.signer.exceptions.RequestSigningException;
import com.google.api.client.http.HttpResponseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.senolab.ccudeleteurlfromfiles.service.OpenAPICallService;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.stream.Stream;

public class Delete {

    private OpenAPICallService openAPICallService;
    private int retryCount = 3;

    public Delete(String edgerc, String network, String body) throws IOException, ParseException {
        //Parse JSON body
        boolean isListFile;

        isListFile = new File(body).isFile();

        JSONObject jsonObject = new JSONObject();
        JSONArray listOfObjects = new JSONArray();
        JSONParser parser = new JSONParser();
        if (isListFile) {
            try {
                Object obj = parser.parse(new FileReader(body));
                jsonObject = (JSONObject) obj;
            } catch (org.json.simple.parser.ParseException e) {
                try (Stream<String> stream = Files.lines(Paths.get(body))) {
                    stream.forEach(listOfObjects::add);
                }
                jsonObject.put("objects", listOfObjects);
            }
        } else {
            listOfObjects.add(body);
            jsonObject.put("objects", listOfObjects);
        }
        String requestBody = jsonObject.toJSONString();
        if(network.equalsIgnoreCase("staging")) {
            String DELETE_URL_STAG = "/ccu/v3/delete/url/staging";
            openAPICallService = new OpenAPICallService(edgerc, DELETE_URL_STAG, requestBody);
        } else if(network.equalsIgnoreCase("production")) {
            String DELETE_URL_PROD = "/ccu/v3/delete/url/production";
            openAPICallService = new OpenAPICallService(edgerc, DELETE_URL_PROD, requestBody);
        }
    }

    public void execute(int sleepInterval) throws IOException, RequestSigningException, InterruptedException {
        try {
            openAPICallService.execute();
        } catch(HttpResponseException e) {
            System.out.println("Response code: "+e.getStatusCode());
            System.out.println("Response headers: \n"+e.getHeaders());
            System.out.println("Response body: \n"+e.getContent());
            if(e.getStatusCode() <= 599 && e.getStatusCode() >= 500 && retryCount > 0) {
                Thread.sleep(sleepInterval);
                System.out.println("Re-trying the call....");
                retryCount--;
                this.execute(sleepInterval);
            } else {
                System.exit(0);
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Socket timeout has occurred.......");
            e.printStackTrace();
            if(retryCount > 0) {
                Thread.sleep(sleepInterval);
                System.out.println("Re-trying the call....");
                retryCount--;
                this.execute(sleepInterval);
            } else {
                System.exit(0);
            }
        } catch (SSLException e) {
            System.out.println("Error occurred during SSL handshake process ...");
            e.printStackTrace();
            if(retryCount > 0) {
                Thread.sleep(sleepInterval);
                System.out.println("Re-trying the call....");
                retryCount--;
                this.execute(sleepInterval);
            } else {
                System.exit(0);
            }
        }
    }
}
