package dev.jianmu.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.eventbridge.aggregate.*;
import dev.jianmu.eventbridge.repository.*;
import dev.jianmu.infrastructure.eventbridge.BodyTransformer;
import dev.jianmu.infrastructure.eventbridge.HeaderTransformer;
import dev.jianmu.project.aggregate.Project;
import dev.jianmu.project.repository.ProjectRepository;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.repository.ParameterRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @class: EventBridgeApplication
 * @description: EventBridgeApplication
 * @author: Ethan Liu
 * @create: 2021-08-12 17:13
 **/
@Service
public class EventBridgeApplication {
    private final ProjectRepository projectRepository;
    private final BridgeRepository bridgeRepository;
    private final SourceRepository sourceRepository;
    private final TargetEventRepository targetEventRepository;
    private final ConnectionRepository connectionRepository;
    private final TargetRepository targetRepository;
    private final ParameterRepository parameterRepository;
    private final ApplicationEventPublisher publisher;
    private final ObjectMapper objectMapper;

    public EventBridgeApplication(
            ProjectRepository projectRepository,
            BridgeRepository bridgeRepository,
            SourceRepository sourceRepository,
            TargetEventRepository targetEventRepository,
            ConnectionRepository connectionRepository,
            TargetRepository targetRepository,
            ParameterRepository parameterRepository,
            ApplicationEventPublisher publisher,
            ObjectMapper objectMapper
    ) {
        this.projectRepository = projectRepository;
        this.bridgeRepository = bridgeRepository;
        this.sourceRepository = sourceRepository;
        this.targetEventRepository = targetEventRepository;
        this.connectionRepository = connectionRepository;
        this.targetRepository = targetRepository;
        this.parameterRepository = parameterRepository;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
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
    public void create(String projectId) {
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到项目"));
        if (!project.getTriggerType().equals(Project.TriggerType.WEBHOOK)) {
            return;
        }
        var source = Source.Builder.aSource()
                .name(project.getWorkflowName() + "_webhook")
                .type(Source.Type.WEBHOOK)
                .build();
        var target = Target.Builder.aTarget()
                .name(project.getWorkflowName())
                .type(Target.Type.WORKFLOW)
                .destinationId(project.getId())
                .build();
        target.setTransformers(this.transformerTemplate());
        var connection = Connection.Builder.aConnection()
                .sourceId(source.getId())
                .targetId(target.getId())
                .build();
        project.setEventBridgeSourceId(source.getId());

        this.sourceRepository.saveOrUpdate(source);
        this.targetRepository.save(target);
        this.connectionRepository.save(connection);
        this.projectRepository.updateByWorkflowRef(project);
    }

    @Transactional
    public void saveOrUpdate(Bridge bridge, Source source, List<Target> targets) {
        // ID不存在为新增
        if (bridge.getId() == null) {
            bridge = Bridge.Builder.aBridge()
                    .name(bridge.getName())
                    .lastModifiedBy("admin")
                    .build();
            source = Source.Builder.aSource()
                    .bridgeId(bridge.getId())
                    .name(source.getName())
                    .type(Source.Type.WEBHOOK)
                    .build();
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
        var ts = targets.stream().map(target ->
                Target.Builder.aTarget()
                        .bridgeId(bridgeId)
                        .name(target.getName())
                        .ref(target.getRef())
                        .type(Target.Type.WORKFLOW)
                        .transformers(target.getTransformers())
                        .build()
        ).collect(Collectors.toSet());
        var cons = ts.stream().map(target ->
                Connection.Builder.aConnection()
                        .bridgeId(bridgeId)
                        .sourceId(sourceId)
                        .targetId(target.getId())
                        .build()
        ).collect(Collectors.toSet());

        this.bridgeRepository.saveOrUpdate(bridge);
        this.sourceRepository.saveOrUpdate(source);
        this.targetRepository.saveOrUpdateList(ts);
        this.connectionRepository.saveOrUpdateList(cons);
    }

    @Transactional
    public void delete(String projectId) {
        this.targetRepository.findByDestinationId(projectId)
                .ifPresent(target -> {
                    var connections = this.connectionRepository.findByTargetId(target.getId());
                    connections.forEach(connection -> {
                        var source = this.sourceRepository.findById(connection.getSourceId())
                                .orElseThrow(() -> new DataNotFoundException("未找到Source"));
                        this.connectionRepository.deleteById(connection.getId());
                        this.sourceRepository.deleteById(source.getId());
                    });
                    this.targetRepository.deleteById(target.getId());
                });
    }

    public void receiveHttpEvent(String token, String sourceId, HttpServletRequest request) {
        var payload = this.createPayload(request);
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
        target.getTransformers().stream().map(transformer -> (Transformer<Parameter<?>>) transformer).forEach(transformer -> {
            Parameter<?> parameter = transformer.extractParameter(connectionEvent.getPayload());
            var eventParameter = EventParameter.Builder.anEventParameter()
                    .name(transformer.getVariableName())
                    .type(transformer.getVariableType())
                    .parameterId(parameter.getId())
                    .build();
            parameters.add(parameter);
            eventParameters.add(eventParameter);
        });
        var targetEvent = TargetEvent.Builder.aTargetEvent()
                .sourceId(connectionEvent.getSourceId())
                .targetRef(target.getRef())
                .destinationId(target.getDestinationId())
                .payload(connectionEvent.getPayload())
                .eventParameters(eventParameters)
                .build();
        this.parameterRepository.addAll(parameters);
        this.targetEventRepository.save(targetEvent);
        this.publisher.publishEvent(targetEvent);
    }

    private Payload createPayload(HttpServletRequest request) {
        String body = null;
        try {
            body = request.getReader()
                    .lines()
                    .collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        var valid = this.isValidJSON(body);
        if (!valid) {
            throw new RuntimeException("无效的Body");
        }
        Map<String, List<String>> headers = Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        h -> Collections.list(request.getHeaders(h))
                ));
        var path = request.getRequestURI();
        var query = request.getParameterMap();
        return Payload.Builder.aPayload()
                .body(body)
                .header(headers)
                .query(query)
                .path(path)
                .build();
    }

    private boolean isValidJSON(final String json) {
        if (json.isBlank())
            return false;
        try {
            this.objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            return false;
        }
        return true;
    }

    private Set<Transformer> transformerTemplate() {
        var refTf = BodyTransformer.Builder.aBodyTransformer()
                .variableName("gitlab_ref")
                .variableType("STRING")
                .expression("$.ref")
                .build();
        var objectKindTf = BodyTransformer.Builder.aBodyTransformer()
                .variableName("gitlab_object_kind")
                .variableType("STRING")
                .expression("$.object_kind")
                .build();
        var beforeTf = BodyTransformer.Builder.aBodyTransformer()
                .variableName("gitlab_before")
                .variableType("STRING")
                .expression("$.before")
                .build();
        var afterTf = BodyTransformer.Builder.aBodyTransformer()
                .variableName("gitlab_after")
                .variableType("STRING")
                .expression("$.after")
                .build();
        var eventTf = HeaderTransformer.Builder.aHeaderTransformer()
                .variableName("gitlab_event_name")
                .variableType("STRING")
                .expression("X-Gitlab-Event")
                .build();
        var giteeRefTf = BodyTransformer.Builder.aBodyTransformer()
                .variableName("gitee_ref")
                .variableType("STRING")
                .expression("$.ref")
                .build();
        var giteeBeforeTf = BodyTransformer.Builder.aBodyTransformer()
                .variableName("gitlab_before")
                .variableType("STRING")
                .expression("$.before")
                .build();
        var giteeAfterTf = BodyTransformer.Builder.aBodyTransformer()
                .variableName("gitlab_after")
                .variableType("STRING")
                .expression("$.after")
                .build();
        return Set.of(refTf, objectKindTf, beforeTf, afterTf, eventTf, giteeRefTf, giteeBeforeTf, giteeAfterTf);
    }
}
