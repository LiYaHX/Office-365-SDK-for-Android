package com.microsoft.office365.odata;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.JsonObject;
import com.microsoft.office365.odata.interfaces.DependencyResolver;
import com.microsoft.office365.odata.interfaces.HttpTransport;
import com.microsoft.office365.odata.interfaces.HttpVerb;
import com.microsoft.office365.odata.interfaces.LogLevel;
import com.microsoft.office365.odata.interfaces.Logger;
import com.microsoft.office365.odata.interfaces.Request;
import com.microsoft.office365.odata.interfaces.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import static com.microsoft.office365.odata.Helpers.urlEncode;

public class BaseODataContainerHelper {

    public static String getODataParameterValue(DependencyResolver resolver, Object value) {

        String serialized = resolver.getJsonSerializer().serialize(value);
        return  urlEncode(serialized);
    }

    public static ListenableFuture<byte[]> oDataExecute(String path, byte[] content, HttpVerb verb, String url, DependencyResolver resolver) {

        final Logger logger = resolver.getLogger();

        String fullUrl = url + "/" + path;
        String executionInfo = String.format("URL: %s - HTTP VERB: %s", fullUrl, verb);
        logger.log("Start preparing OData execution for " + executionInfo, LogLevel.INFO);

        if (content != null) {
            logger.log("With " + content.length + " bytes of payload", LogLevel.INFO);
        }

        HttpTransport httpTransport = resolver.getHttpTransport();
        Request request = httpTransport.createRequest();
        request.setVerb(verb);
        request.setUrl(fullUrl);
        request.setContent(content);
        request.addHeader("Content-Type", "application/json");
        resolver.getCredentialsFactory().getCredentials().prepareRequest(request);

        logger.log("Request Headers: ", LogLevel.VERBOSE);
        for (String key : request.getHeaders().keySet()) {
            logger.log(key + " : " + request.getHeaders().get(key).toString(), LogLevel.VERBOSE);
        }

        final ListenableFuture<Response> future = httpTransport.execute(request);
        logger.log("OData request executed", LogLevel.INFO);

        final SettableFuture<byte[]> result = SettableFuture.create();

        Futures.addCallback(future, new FutureCallback<Response>() {

            @Override
            public void onSuccess(Response response) {
                try {
                    logger.log("OData response received", LogLevel.INFO);

                    logger.log("Reading response data...", LogLevel.VERBOSE);
                    byte[] data = readAllBytes(response.getStream());
                    logger.log(data.length + " bytes read from response", LogLevel.VERBOSE);

                    int status = response.getStatus();
                    logger.log("Response Status Code: " + status, LogLevel.INFO);

                    try {
                        logger.log("Closing response", LogLevel.VERBOSE);
                        response.close();
                    } catch (Throwable t) {
                        logger.log("Error closing response: " + t.toString(), LogLevel.ERROR);
                        result.setException(t);
                        return;
                    }

                    if (status < 200 || status > 299) {
                        logger.log("Invalid status code. Processing response content as String", LogLevel.VERBOSE);
                        String responseData = new String(data, Constants.UTF8_NAME);
                        String message = "Response status: " + response.getStatus() + "\n" + "Response content: " + responseData;
                        logger.log(message, LogLevel.ERROR);
                        result.setException(new IllegalStateException(message));
                        return;
                    }
                    result.set(data);
                } catch (Throwable t) {
                    logger.log("Unexpected error: " + t.toString(), LogLevel.ERROR);
                    result.setException(t);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                result.setException(throwable);
            }
        });
        return result;
    }

    public static byte[] readAllBytes(InputStream stream) throws IOException {
        if (stream == null) {
            return new byte[0];
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];

        while ((nRead = stream.read(data, 0, data.length)) != -1) {
            os.write(data, 0, nRead);
        }
        return os.toByteArray();
    }

    public static String generateParametersPayload(Map<String, Object> parameters, DependencyResolver resolver) {
        return resolver.getJsonSerializer().serialize(parameters);
    }

}