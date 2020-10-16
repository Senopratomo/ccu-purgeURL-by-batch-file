package org.senolab.ccudeleteurlfromfiles.model;

import com.akamai.edgegrid.signer.exceptions.RequestSigningException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.senolab.ccudeleteurlfromfiles.service.OpenAPICallService;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.stream.Stream;

public class Delete {

    private final String DELETE_URL_STAG = "/ccu/v3/delete/url/staging";
    private final String DELETE_URL_PROD = "/ccu/v3/delete/url/production";

    private OpenAPICallService openAPICallService;

    public Delete(String edgerc, String network, String body) throws IOException, ParseException {
        //Parse JSON body
        boolean isListFile;

        if(new File(body).isFile()) {
            isListFile = true;
        } else {
            isListFile = false;
        }

        JSONObject jsonObject = new JSONObject();
        JSONArray listOfObjects = new JSONArray();
        JSONParser parser = new JSONParser();
        if (isListFile) {
            try {
                Object obj = parser.parse(new FileReader(body));
                jsonObject = (JSONObject) obj;
            } catch (org.json.simple.parser.ParseException e) {
                try (Stream<String> stream = Files.lines(Paths.get(body))) {
                    stream.forEach(x -> listOfObjects.add(x));
                }
                jsonObject.put("objects", listOfObjects);
            }
        } else {
            listOfObjects.add(body);
            jsonObject.put("objects", listOfObjects);
        }
        String requestBody = jsonObject.toJSONString();
        if(network.equalsIgnoreCase("staging")) {
            openAPICallService = new OpenAPICallService(edgerc, DELETE_URL_STAG, requestBody);
        } else if(network.equalsIgnoreCase("production")) {
            openAPICallService = new OpenAPICallService(edgerc, DELETE_URL_PROD, requestBody);
        }
    }

    public void execute() throws IOException, RequestSigningException {
        openAPICallService.execute();
    }

}
