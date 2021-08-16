package dev.jianmu.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.application.service.EventBridgeApplication;
import dev.jianmu.eventbridge.aggregate.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @class: EndpointController
 * @description: EndpointController
 * @author: Ethan Liu
 * @create: 2021-08-13 09:24
 **/
@Controller
@RequestMapping("/endpoints")
public class EndpointController {
    private EventBridgeApplication eventBridgeApplication;
    private ObjectMapper objectMapper;

    public EndpointController(EventBridgeApplication eventBridgeApplication, ObjectMapper objectMapper) {
        this.eventBridgeApplication = eventBridgeApplication;
        this.objectMapper = objectMapper;
    }

    @RequestMapping(value = "event/{sourceId}", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public void receivePostJsonEvent(HttpServletRequest request, @PathVariable String sourceId) throws IOException {
        var body = request.getReader()
                .lines()
                .collect(Collectors.joining(System.lineSeparator()));
        var valid = isValidJSON(body);
        if (!valid) {
            return;
        }
        Map<String, List<String>> headers = Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        h -> Collections.list(request.getHeaders(h))
                ));
        var path = request.getRequestURI();
        var query = request.getParameterMap();
        var payload = Payload.Builder.aPayload()
                .body(body)
                .header(headers)
                .query(query)
                .path(path)
                .build();
        this.eventBridgeApplication.receiveHttpEvent(sourceId, payload);
    }

    private boolean isValidJSON(final String json) {
        if (json.isBlank())
            return false;
        try {
            objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            return false;
        }
        return true;
    }
}
