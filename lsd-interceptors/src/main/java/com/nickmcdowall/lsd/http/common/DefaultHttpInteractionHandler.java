package com.nickmcdowall.lsd.http.common;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.http.naming.SourceNameMappings;
import lombok.EqualsAndHashCode;

import java.util.Map;

import static com.nickmcdowall.lsd.http.common.Headers.HeaderKeys.SOURCE_NAME;
import static com.nickmcdowall.lsd.http.common.Headers.HeaderKeys.TARGET_NAME;
import static com.nickmcdowall.lsd.http.common.HttpInteractionMessageTemplates.requestOf;
import static com.nickmcdowall.lsd.http.common.HttpInteractionMessageTemplates.responseOf;
import static com.nickmcdowall.lsd.http.common.PrettyPrinter.prettyPrint;
import static j2html.TagCreator.*;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

@EqualsAndHashCode
public class DefaultHttpInteractionHandler implements HttpInteractionHandler {

    private final TestState testState;
    private final SourceNameMappings sourceNameMappings;
    private final DestinationNameMappings destinationNameMappings;

    public DefaultHttpInteractionHandler(TestState testState, SourceNameMappings sourceNameMappings, DestinationNameMappings destinationNameMappings) {
        this.testState = testState;
        this.sourceNameMappings = sourceNameMappings;
        this.destinationNameMappings = destinationNameMappings;
    }

    @Override
    public void handleRequest(String method, Map<String, String> requestHeaders, String path, String body) {
        String sourceName = deriveSourceName(requestHeaders, path);
        String destinationName = deriveTargetName(requestHeaders, path);
        testState.log(requestOf(method, path, sourceName, destinationName), renderHtmlFor(path, requestHeaders, prettyPrint(body)));
    }

    @Override
    public void handleResponse(String statusMessage, Map<String, String> requestHeaders, String path, String body) {
        String destinationName = deriveTargetName(requestHeaders, path);
        String sourceName = deriveSourceName(requestHeaders, path);
        testState.log(responseOf(statusMessage, destinationName, sourceName), renderHtmlFor(path, requestHeaders, prettyPrint(body)));
    }

    private String renderHtmlFor(String path, Map<String, String> requestHeaders, String prettyBody) {
        var popupValue =
                p(
                        p(
                                h4("request path:"),
                                sub(path)
                        ),
                        p(
                                h4("request headers:"),
                                sub(prettyPrintHeaders(requestHeaders))
                        ),
                        p(
                                h4("body:"),
                                pre(prettyBody)
                        )
                ).render();
        return popupValue;
    }

    private String prettyPrintHeaders(Map<String, String> requestHeaders) {
        return requestHeaders.entrySet().stream().map(entry ->
                entry.getKey() + ": " + entry.getValue()).collect(joining(lineSeparator()));
    }

    private String deriveTargetName(Map<String, String> headers, String path) {
        return headers.containsKey(TARGET_NAME.key())
                ? headers.get(TARGET_NAME.key())
                : destinationNameMappings.mapForPath(path);
    }

    private String deriveSourceName(Map<String, String> headers, String path) {
        return headers.containsKey(SOURCE_NAME.key())
                ? headers.get(SOURCE_NAME.key())
                : sourceNameMappings.mapForPath(path);
    }
}