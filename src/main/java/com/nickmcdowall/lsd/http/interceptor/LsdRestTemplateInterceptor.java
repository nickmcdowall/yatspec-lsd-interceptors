package com.nickmcdowall.lsd.http.interceptor;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.http.naming.SourceNameMappings;
import com.nickmcdowall.lsd.http.common.HttpInteractionMessageTemplates;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created to intercept rest template calls for Yatspec interactions.
 * Attempts to reset the input stream so that no data is lost on reading the reponse body
 */
@Value
@RequiredArgsConstructor
public class LsdRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    TestState interactions;
    SourceNameMappings sourceNameMappings;
    DestinationNameMappings destinationNameMappings;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String path = request.getURI().getPath();

        String sourceName = sourceNameMappings.mapForPath(path);
        String destinationName = destinationNameMappings.mapForPath(path);

        captureRequest(request, body, path, sourceName, destinationName);
        ClientHttpResponse response = execution.execute(request, body);
        captureResponse(sourceName, destinationName, response);

        return response;
    }

    private void captureRequest(HttpRequest request, byte[] body, String path, String sourceName, String destinationName) {
        String interactionMessage = HttpInteractionMessageTemplates.requestOf(request.getMethodValue(), path, sourceName, destinationName);
        interactions.log(interactionMessage, new String(body));
    }

    private void captureResponse(String sourceName, String destinationName, ClientHttpResponse response) throws IOException {
        String interactionMessage = HttpInteractionMessageTemplates.responseOf(response.getStatusCode().toString(), destinationName, sourceName);
        interactions.log(interactionMessage, copyBodyToString(response));
    }

    private String copyBodyToString(ClientHttpResponse response) throws IOException {
        if (response.getHeaders().getContentLength() == 0)
            return "";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = response.getBody();
        inputStream.transferTo(outputStream);
        return outputStream.toString();
    }

}