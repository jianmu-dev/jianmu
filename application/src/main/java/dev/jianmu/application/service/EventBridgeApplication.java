package dev.jianmu.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.eventbridge.aggregate.*;
import dev.jianmu.eventbridge.repository.ConnectionRepository;
import dev.jianmu.eventbridge.repository.SourceRepository;
import dev.jianmu.eventbridge.repository.TargetEventRepository;
import dev.jianmu.eventbridge.repository.TargetRepository;
import dev.jianmu.infrastructure.mybatis.eventbridge.BridgeRepositoryImpl;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.repository.ParameterRepository;
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
 * @class: EventBridgeApplication
 * @description: EventBridgeApplication
 * @author: Ethan Liu
 * @create: 2021-08-12 17:13
 **/
@Service
public class EventBridgeApplication {
    private final BridgeRepositoryImpl bridgeRepository;
    private final SourceRepository sourceRepository;
    private final TargetEventRepository targetEventRepository;
    private final ConnectionRepository connectionRepository;
    private final TargetRepository targetRepository;
    private final ParameterRepository parameterRepository;
    private final ApplicationEventPublisher publisher;
    private final ObjectMapper objectMapper;

    public EventBridgeApplication(
            BridgeRepositoryImpl bridgeRepository,
            SourceRepository sourceRepository,
            TargetEventRepository targetEventRepository,
            ConnectionRepository connectionRepository,
            TargetRepository targetRepository,
            ParameterRepository parameterRepository,
            ApplicationEventPublisher publisher,
            ObjectMapper objectMapper
    ) {
        this.bridgeRepository = bridgeRepository;
        this.sourceRepository = sourceRepository;
        this.targetEventRepository = targetEventRepository;
        this.connectionRepository = connectionRepository;
        this.targetRepository = targetRepository;
        this.parameterRepository = parameterRepository;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
    }

    public TargetEvent findTargetEvent(String targetEventId) {
        var event = this.targetEventRepository.findById(targetEventId)
                .orElseThrow(() -> new DataNotFoundException("未找到该触发事件"));
        return event;
    }

    public PageInfo<Bridge> findAll(int pageNum, int pageSize) {
        return this.bridgeRepository.findAllPage(pageNum, pageSize);
    }

    public Bridge findBridgeById(String bridgeId) {
        return this.bridgeRepository.findById(bridgeId)
                .orElseThrow(() -> new DataNotFoundException("未找到Bridge"));
    }

    public Source findSourceByBridgeId(String bridgeId) {
        return this.sourceRepository.findByBridgeId(bridgeId)
                .orElseThrow(() -> new DataNotFoundException("未找到Source"));
    }

    public List<Target> findTargetsByBridgeId(String bridgeId) {
        return this.targetRepository.findByBridgeId(bridgeId);
    }

    @Transactional
    public String generateWebhook(String sourceId) {
        var source = this.sourceRepository.findById(sourceId).orElseThrow(() -> new DataNotFoundException("未找到源"));
        source.generateToken();
        this.sourceRepository.updateTokenById(source);
        return source.getWebHookUrl();
    }

    public String getWebhookUrl(String sourceId) {
        var source = this.sourceRepository.findById(sourceId).orElseThrow(() -> new DataNotFoundException("未找到源"));
        return source.getWebHookUrl();
    }

    @Transactional
    public Bridge saveOrUpdate(Bridge bridge, Source source, List<Target> targets) {
        // ID不存在为新增
        if (StringUtils.isBlank(bridge.getId())) {
            bridge = Bridge.Builder.aBridge()
                    .name(bridge.getName())
                    .lastModifiedBy("admin")
                    .build();
            source = Source.Builder.aSource()
                    .bridgeId(bridge.getId())
                    .name(source.getName())
                    .type(Source.Type.WEBHOOK)
                    .build();
            source.generateToken();
        } else {
            var oldSource = this.sourceRepository.findByBridgeId(bridge.getId())
                    .orElseThrow(() -> new RuntimeException("未找到Source"));
            oldSource.setName(source.getName());
            source = oldSource;
        }
        bridge.setLastModifiedBy("admin");
        bridge.setLastModifiedTime();
        var bridgeId = bridge.getId();
        var sourceId = source.getId();
        // 校验Target Ref唯一性
        var countMap = targets.stream()
                .filter(target -> !StringUtils.isBlank(target.getRef()))
                .collect(Collectors.groupingBy(Target::getRef, Collectors.counting()));
        countMap.values().forEach(i -> {
            if (i > 1)
                throw new RuntimeException("Target Ref不能重复");
        });

        var oldTargets = this.targetRepository.findByBridgeId(bridgeId)
                .stream().filter(target -> !StringUtils.isBlank(target.getDestinationId()))
                .collect(Collectors.toList());
        oldTargets.forEach(target -> {
            long count = 0;
            count = targets.stream()
                    .filter(inTarget -> inTarget.getId().equals(target.getId()))
                    .count();
            if (count < oldTargets.size()) {
                throw new RuntimeException("已关联项目，禁止操作，若要继续，请先在项目中，移除关联关系。");
            }
            count = targets.stream()
                    .filter(inTarget -> inTarget.getId().equals(target.getId()))
                    .filter(inTarget -> !inTarget.getRef().equals(target.getRef()))
                    .count();
            if (count > 0) {
                throw new RuntimeException("已关联项目，禁止操作，若要继续，请先在项目中，移除关联关系。");
            }
        });

        var newTargets = targets.stream().map(target -> {
            var t = this.targetRepository.findById(target.getId())
                    .orElse(Target.Builder.aTarget()
                            .bridgeId(bridgeId)
                            .name(target.getName())
                            .ref(target.getRef())
                            .type(Target.Type.WORKFLOW)
                            .transformers(target.getTransformers())
                            .build());
            t.setBridgeId(bridgeId);
            t.setName(target.getName());
            t.setRef(target.getRef());
            t.setType(Target.Type.WORKFLOW);
            t.setTransformers(target.getTransformers());
            return t;
        }).collect(Collectors.toSet());

        var cons = newTargets.stream().map(target ->
                Connection.Builder.aConnection()
                        .bridgeId(bridgeId)
                        .sourceId(sourceId)
                        .targetId(target.getId())
                        .build()
        ).collect(Collectors.toSet());

        this.bridgeRepository.saveOrUpdate(bridge);
        this.sourceRepository.saveOrUpdate(source);
        this.targetRepository.saveOrUpdateList(newTargets);
        this.connectionRepository.saveOrUpdateList(cons);
        return bridge;
    }

    @Transactional
    public void delete(String bridgeId) {
        var targets = this.targetRepository.findByBridgeId(bridgeId);
        var count = targets.stream()
                .filter(target -> !StringUtils.isBlank(target.getDestinationId()))
                .count();
        if (count > 0)
            throw new RuntimeException("已关联项目，禁止操作，若要继续，请先在项目中，移除关联关系。");
        this.bridgeRepository.deleteById(bridgeId);
        this.sourceRepository.deleteByBridgeId(bridgeId);
        this.targetRepository.deleteByBridgeId(bridgeId);
        this.connectionRepository.deleteByBridgeId(bridgeId);
    }

    public void receiveHttpEvent(String token, String sourceId, HttpServletRequest request, String contentType) {
        var payload = this.createPayload(request, contentType);
        var source = this.sourceRepository.findById(sourceId)
                .orElseThrow(() -> new RuntimeException("未找到该Source: " + sourceId));
        if (!source.isValidToken(token)) {
            throw new RuntimeException("无效的Token");
        }
        if (source.getType().equals(Source.Type.WEBHOOK)) {
            var sourceEvent = SourceEvent.Builder.anOriginalEvent()
                    .sourceId(sourceId)
                    .payload(payload)
                    .build();
            this.publisher.publishEvent(sourceEvent);
        }
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
        return root.toPrettyString();
    }

    private static String decode(final String encoded) {
        return Optional.ofNullable(encoded)
                .map(e -> URLDecoder.decode(e, StandardCharsets.UTF_8))
                .orElse(null);
    }

    public List<Transformer> gitlabTemplates() {
        var refTf = Transformer.Builder.aTransformer()
                .variableName("gitlab_ref")
                .variableType("STRING")
                .expression("$.body.json.ref")
                .build();
        var objectKindTf = Transformer.Builder.aTransformer()
                .variableName("gitlab_object_kind")
                .variableType("STRING")
                .expression("$.body.json.object_kind")
                .build();
        var beforeTf = Transformer.Builder.aTransformer()
                .variableName("gitlab_before")
                .variableType("STRING")
                .expression("$.body.json.before")
                .build();
        var afterTf = Transformer.Builder.aTransformer()
                .variableName("gitlab_after")
                .variableType("STRING")
                .expression("$.body.json.after")
                .build();
        var eventTf = Transformer.Builder.aTransformer()
                .variableName("gitlab_event_name")
                .variableType("STRING")
                .expression("$.header.X-Gitlab-Event")
                .build();
        return List.of(refTf, objectKindTf, beforeTf, afterTf, eventTf);
    }

    public List<Transformer> giteeTemplates() {
        var giteeRefTf = Transformer.Builder.aTransformer()
                .variableName("gitee_ref")
                .variableType("STRING")
                .expression("$.body.json.ref")
                .build();
        var giteeBeforeTf = Transformer.Builder.aTransformer()
                .variableName("gitee_before")
                .variableType("STRING")
                .expression("$.body.json.before")
                .build();
        var giteeAfterTf = Transformer.Builder.aTransformer()
                .variableName("gitee_after")
                .variableType("STRING")
                .expression("$.body.json.after")
                .build();
        return List.of(giteeRefTf, giteeBeforeTf, giteeAfterTf);
    }
}
