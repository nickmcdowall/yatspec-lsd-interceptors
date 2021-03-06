package io.lsdconsulting.interceptors.http.common;

import com.lsd.LsdContext;
import io.lsdconsulting.interceptors.http.naming.DestinationNameMappings;
import io.lsdconsulting.interceptors.http.naming.SourceNameMappings;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

class DefaultHttpInteractionHandlerTest {

    private final Map<String, String> serviceNameHeaders = Map.of(
            Headers.HeaderKeys.TARGET_NAME.key(), "target",
            Headers.HeaderKeys.SOURCE_NAME.key(), "source"
    );
    private final SourceNameMappings sourceNameMapping = path -> "sourceName";
    private final DestinationNameMappings destinationNameMapping = path -> "destinationName";
    private final LsdContext lsdContext = Mockito.mock(LsdContext.class);

    private final DefaultHttpInteractionHandler handler = new DefaultHttpInteractionHandler(lsdContext, sourceNameMapping, destinationNameMapping);

    @Test
    void usesTestStateToLogRequest() {
        handler.handleRequest("GET", emptyMap(), "/path", "{\"type\":\"request\"}");

        verify(lsdContext).capture("GET /path from SourceName to DestinationName",
                "<p>" +
                        "<p><h4>Full Path</h4><span>/path</span></p>" +
                        "<p><h4>Request Headers</h4><code></code></p>" +
                        "<p><h4>Body</h4><code>{\n  &quot;type&quot;: &quot;request&quot;\n}</code></p>" +
                        "</p>");
    }

    @Test
    void usesTestStateToLogResponse() {
        handler.handleResponse("200 OK", emptyMap(), "/path", "response body");

        verify(lsdContext).capture("sync 200 OK response from DestinationName to SourceName",
                "<p>" +
                        "<p><h4>Full Path</h4><span>/path</span></p>" +
                        "<p><h4>Request Headers</h4><code></code></p>" +
                        "<p><h4>Body</h4><code>response body</code></p>" +
                        "</p>");
    }

    @Test
    void headerValuesForSourceAndDestinationArePreferredWhenLoggingRequest() {
        handler.handleRequest("GET", serviceNameHeaders, "/path", "");

        verify(lsdContext).capture(ArgumentMatchers.eq("GET /path from Source to Target"), anyString());
    }

    @Test
    void headerValuesForSourceAndDestinationArePreferredWhenLoggingResponse() {
        handler.handleResponse("200 OK", serviceNameHeaders, "/path", "response body");

        verify(lsdContext).capture(
                ArgumentMatchers.eq("sync 200 OK response from Target to Source"),
                ArgumentMatchers.contains("<code>response body</code>"));
    }
}