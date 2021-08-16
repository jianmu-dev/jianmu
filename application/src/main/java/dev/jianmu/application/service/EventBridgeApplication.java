package dev.jianmu.application.service;

import dev.jianmu.eventbridge.aggregate.*;
import dev.jianmu.eventbridge.repository.ConnectionRepository;
import dev.jianmu.eventbridge.repository.SourceRepository;
import dev.jianmu.eventbridge.repository.TargetEventRepository;
import dev.jianmu.eventbridge.repository.TargetRepository;
import dev.jianmu.parameter.aggregate.Parameter;
import dev.jianmu.parameter.repository.ParameterRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @class: EventBridgeApplication
 * @description: EventBridgeApplication
 * @author: Ethan Liu
 * @create: 2021-08-12 17:13
 **/
@Service
public class EventBridgeApplication {
    private final SourceRepository sourceRepository;
    private final TargetEventRepository targetEventRepository;
    private final ConnectionRepository connectionRepository;
    private final TargetRepository targetRepository;
    private final ParameterRepository parameterRepository;
    private final ApplicationEventPublisher publisher;

    public EventBridgeApplication(
            SourceRepository sourceRepository,
            TargetEventRepository targetEventRepository,
            ConnectionRepository connectionRepository,
            TargetRepository targetRepository,
            ParameterRepository parameterRepository,
            ApplicationEventPublisher publisher
    ) {
        this.sourceRepository = sourceRepository;
        this.targetEventRepository = targetEventRepository;
        this.connectionRepository = connectionRepository;
        this.targetRepository = targetRepository;
        this.parameterRepository = parameterRepository;
        this.publisher = publisher;
    }

    public void receiveHttpEvent(String sourceId, Payload payload) {
        var source = this.sourceRepository.findById(sourceId)
                .orElseThrow(() -> new RuntimeException("未找到该Source: " + sourceId));
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
                .targetId(target.getId())
                .payload(connectionEvent.getPayload())
                .eventParameters(eventParameters)
                .build();
        this.parameterRepository.addAll(parameters);
        this.targetEventRepository.save(targetEvent);
        this.publisher.publishEvent(targetEvent);
    }
}
