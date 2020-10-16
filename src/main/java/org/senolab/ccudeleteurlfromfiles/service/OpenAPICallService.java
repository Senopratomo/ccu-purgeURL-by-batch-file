package org.senolab.ccudeleteurlfromfiles.service;

import com.akamai.edgegrid.signer.ClientCredential;
import com.akamai.edgegrid.signer.exceptions.RequestSigningException;
import com.akamai.edgegrid.signer.googlehttpclient.GoogleHttpClientEdgeGridInterceptor;
import com.akamai.edgegrid.signer.googlehttpclient.GoogleHttpClientEdgeGridRequestSigner;
import com.google.api.client.http.*;
import com.google.api.client.http.apache.ApacheHttpTransport;
import org.senolab.ccudeleteurlfromfiles.util.Edgerc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.ParseException;

public class OpenAPICallService {
    private final int MAX_BODY = 131072;
    private String[] apiClientInfo;

    private StringBuilder uriPath;
    private ClientCredential credential;
    private HttpRequest request;
    private HttpResponse response;

    public OpenAPICallService(String edgerc, String httpPath, String jsonBody) throws IOException, ParseException {
        apiClientInfo = Edgerc.extractTokens(edgerc);

        //Build the URL based on the path specified and the host from edgerc file
        uriPath = buildURIPath(apiClientInfo[0], httpPath);

        //Build the credential based on tokens provided in edgerc file
        credential = buildCredential(apiClientInfo[2], apiClientInfo[3], apiClientInfo[4], apiClientInfo[0]);

        //Build the HTTP request
        request = buildHttpRequest(uriPath.toString(), jsonBody);
        response = null;

        //Add application-json header
        HttpHeaders headers = request.getHeaders();
        headers.setContentType("application/json");
        //Setting Host header
        headers.set("Host", apiClientInfo[1]);
    }

    private ClientCredential buildCredential(String clientToken, String accessToken, String clientSecret, String host) {
        return new ClientCredential.ClientCredentialBuilder()
                .clientToken(clientToken)
                .accessToken(accessToken)
                .clientSecret(clientSecret)
                .host(host)
                .build();
    }

    private StringBuilder buildURIPath(String host, String path) {
        return new StringBuilder()
                .append("https://")
                .append(host)
                .append(path);
    }

    private HttpRequestFactory createSigningRequestFactory() {
        HttpTransport httpTransport = new ApacheHttpTransport();
        return httpTransport.createRequestFactory(new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
                request.setInterceptor(new GoogleHttpClientEdgeGridInterceptor(credential));
            }
        });
    }

    private HttpRequest buildHttpRequest(String urlPath, String jsonBody) throws IOException {
        HttpRequestFactory requestFactory = createSigningRequestFactory();
        URI uri = URI.create(urlPath);
        HttpRequest request = requestFactory.buildPostRequest(new GenericUrl(uri), ByteArrayContent.fromString(null, jsonBody));
        request.setFollowRedirects(true);
        request.setReadTimeout(400000);
        System.out.println("POST "+urlPath);
        System.out.println("Body: "+jsonBody);
        return request;
    }

    public void execute() throws IOException, RequestSigningException {
        //Sign request and execute
        GoogleHttpClientEdgeGridRequestSigner requestSigner = new GoogleHttpClientEdgeGridRequestSigner(credential);
        requestSigner.sign(request);
        response = request.execute();

        //Print HTTP response code + response headers
        System.out.println("HTTP Response code: "+response.getStatusCode());
        System.out.println("HTTP Response headers: \n"+response.getHeaders());
        //Extract the HTTP Response code and response
        if(response.getContent() != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getContent(), "UTF-8"));
            String json = reader.readLine();
            System.out.println("HTTP Response Body: ");
            while (json != null) {
                System.out.println(json+"\n");
                json = reader.readLine();
            }
            reader.close();
        }
    }
}
