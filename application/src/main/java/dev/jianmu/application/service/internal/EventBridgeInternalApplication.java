package dev.jianmu.application.service.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import dev.jianmu.eventbridge.aggregate.EventParameter;
import dev.jianmu.eventbridge.aggregate.Source;
import dev.jianmu.eventbridge.aggregate.event.ConnectionEvent;
import dev.jianmu.eventbridge.aggregate.event.SourceEvent;
import dev.jianmu.eventbridge.aggregate.event.TargetEvent;
import dev.jianmu.eventbridge.repository.*;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.repository.ParameterRepository;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * @class: EventBridgeInternalApplication
 * @description: EventBridgeInternalApplication
 * @author: Ethan Liu
 * @create: 2021-10-21 14:52
 **/
@Service
@Slf4j
public class EventBridgeInternalApplication {
    private final SourceRepository sourceRepository;
    private final SourceEventRepository sourceEventRepository;
    private final TargetEventRepository targetEventRepository;
    private final ConnectionRepository connectionRepository;
    private final TargetRepository targetRepository;
    private final ParameterRepository parameterRepository;
    private final ApplicationEventPublisher publisher;
    private final ObjectMapper objectMapper;

    public EventBridgeInternalApplication(
            SourceRepository sourceRepository,
            SourceEventRepository sourceEventRepository,
            TargetEventRepository targetEventRepository,
            ConnectionRepository connectionRepository,
            TargetRepository targetRepository,
            ParameterRepository parameterRepository,
            ApplicationEventPublisher publisher,
            ObjectMapper objectMapper
    ) {
        this.sourceRepository = sourceRepository;
        this.sourceEventRepository = sourceEventRepository;
        this.targetEventRepository = targetEventRepository;
        this.connectionRepository = connectionRepository;
        this.targetRepository = targetRepository;
        this.parameterRepository = parameterRepository;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
    }

    public void receiveHttpEvent(String token, String sourceId, HttpServletRequest request, String contentType) {
        var payload = this.createPayload(request, contentType);
        var source = this.sourceRepository.findById(sourceId)
                .orElseThrow(() -> new RuntimeException("未找到该Source: " + sourceId));
        var sourceEvent = SourceEvent.Builder.anOriginalEvent()
                .sourceId(sourceId)
                .bridgeId(source.getBridgeId())
                .sourceType(source.getType())
                .payload(payload)
                .build();
        this.sourceEventRepository.add(sourceEvent);
        // Check Token
        if (!source.isValidToken(token)) {
            throw new RuntimeException("无效的Token");
        }
        // Check Type
        if (!source.getType().equals(Source.Type.WEBHOOK)) {
            throw new RuntimeException("Source类型不匹配");
        }
        // isMatched
        if (!StringUtils.isBlank(source.getMatcher())) {
            if (this.isMatched(source.getMatcher(), payload)) {
                log.info("符合匹配规则: {}", source.getMatcher());
            } else {
                log.info("不符合匹配规则: {}", source.getMatcher());
                return;
            }
        }
        this.publisher.publishEvent(sourceEvent);
    }

    public void dispatchEvent(SourceEvent sourceEvent) {
        var connections = this.connectionRepository.findBySourceId(sourceEvent.getSourceId());
        connections.forEach(connection -> {
            var target = this.targetRepository.findById(connection.getTargetId())
                    .orElseThrow(() -> new RuntimeException("未找到该Target: " + connection.getTargetId()));
            var connectionEvent = ConnectionEvent.Builder.aConnectionEvent()
                    .sourceId(connection.getSourceId())
                    .sourceEventId(sourceEvent.getId())
                    .targetId(target.getId())
                    .payload(sourceEvent.getPayload())
                    .build();
            this.publisher.publishEvent(connectionEvent);
        });
    }

    @Transactional
    public void eventHandling(ConnectionEvent connectionEvent) {
        var target = this.targetRepository.findById(connectionEvent.getTargetId())
                .orElseThrow(() -> new RuntimeException("未找到该Target: " + connectionEvent.getTargetId()));
        Set<EventParameter> eventParameters = new HashSet<>();
        List<Parameter> parameters = new ArrayList<>();
        target.getTransformers().forEach(transformer -> {
            Parameter<?> parameter = Parameter.Type
                    .getTypeByName(transformer.getVariableType())
                    .newParameter(transformer.extractParameter(connectionEvent.getPayload()));
            var eventParameter = EventParameter.Builder.anEventParameter()
                    .name(transformer.getVariableName())
                    .type(transformer.getVariableType())
                    .value(parameter.getStringValue())
                    .parameterId(parameter.getId())
                    .build();
            parameters.add(parameter);
            eventParameters.add(eventParameter);
        });
        var targetEvent = TargetEvent.Builder.aTargetEvent()
                .sourceId(connectionEvent.getSourceId())
                .sourceEventId(connectionEvent.getSourceEventId())
                .connectionEventId(connectionEvent.getId())
                .targetId(target.getId())
                .targetRef(target.getRef())
                .destinationId(target.getDestinationId())
                .payload(connectionEvent.getPayload())
                .eventParameters(eventParameters)
                .build();
        this.parameterRepository.addAll(parameters);
        this.targetEventRepository.save(targetEvent);
        this.publisher.publishEvent(targetEvent);
    }

    private String createPayload(HttpServletRequest request, String contentType) {
        // Get body
        String body = null;
        try {
            body = request.getReader()
                    .lines()
                    .collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        // Create root node
        var root = this.objectMapper.createObjectNode();
        // Headers node
        Map<String, List<String>> headers = Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        h -> Collections.list(request.getHeaders(h))
                ));
        var headerNode = root.putObject("header");
        headers.forEach((key, value) -> {
            var item = headerNode.putArray(key);
            value.forEach(item::add);
        });
        // Query String node
        var url = request.getRequestURL().toString();
        var queryString = request.getQueryString();
        MultiValueMap<String, String> parameters =
                UriComponentsBuilder.fromUriString(url + "?" + queryString).build().getQueryParams();
        var queryNode = root.putObject("query");
        parameters.forEach((key, value) -> {
            var item = queryNode.putArray(key);
            value.forEach(item::add);
        });
        // Body node
        var bodyNode = root.putObject("body");
        // Body Json node
        if (contentType.startsWith("application/json")) {
            try {
                var bodyJson = this.objectMapper.readTree(body);
                bodyNode.set("json", bodyJson);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Body格式错误");
            }
        }
        // Body Form node
        if (contentType.startsWith("application/x-www-form-urlencoded")) {
            var formNode = bodyNode.putObject("form");
            var formMap = Pattern.compile("&")
                    .splitAsStream(body)
                    .map(s -> Arrays.copyOf(s.split("=", 2), 2))
                    .collect(groupingBy(s -> decode(s[0]), mapping(s -> decode(s[1]), toList())));
            formMap.forEach((key, value) -> {
                var item = formNode.putArray(key);
                value.forEach(item::add);
            });
        }
        // Body Text node
        if (contentType.startsWith("text/plain")) {
            bodyNode.put("text", body);
        }
        return root.toString();
    }

    private boolean isMatched(String expression, String json) {
        var variable = JsonPath.read(json, expression);
        return !((JSONArray) variable).isEmpty();
    }

    private static String decode(final String encoded) {
        return Optional.ofNullable(encoded)
                .map(e -> URLDecoder.decode(e, StandardCharsets.UTF_8))
                .orElse(null);
    }
}
