package dev.jianmu.application.service;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.eventbridge.aggregate.Connection;
import dev.jianmu.eventbridge.aggregate.Source;
import dev.jianmu.eventbridge.aggregate.Target;
import dev.jianmu.eventbridge.aggregate.Transformer;
import dev.jianmu.eventbridge.repository.ConnectionRepository;
import dev.jianmu.eventbridge.repository.SourceRepository;
import dev.jianmu.eventbridge.repository.TargetRepository;
import dev.jianmu.infrastructure.eventbridge.BodyTransformer;
import dev.jianmu.infrastructure.eventbridge.HeaderTransformer;
import dev.jianmu.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * @class: EventBridgeSettingApplication
 * @description: EventBridgeSettingApplication
 * @author: Ethan Liu
 * @create: 2021-08-16 22:40
 **/
@Service
public class EventBridgeSettingApplication {
    private final SourceRepository sourceRepository;
    private final ConnectionRepository connectionRepository;
    private final TargetRepository targetRepository;
    private final ProjectRepository projectApplication;

    public EventBridgeSettingApplication(
            SourceRepository sourceRepository,
            ConnectionRepository connectionRepository,
            TargetRepository targetRepository,
            ProjectRepository projectApplication
    ) {
        this.sourceRepository = sourceRepository;
        this.connectionRepository = connectionRepository;
        this.targetRepository = targetRepository;
        this.projectApplication = projectApplication;
    }

    @Transactional
    public void addWebhook(String projectId) {
        var project = this.projectApplication.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到项目"));
        var source = Source.Builder.aSource()
                .name(project.getWorkflowName() + "_webhook")
                .type(Source.Type.WEBHOOK)
                .build();
        var target = Target.Builder.aTarget()
                .name(project.getWorkflowName())
                .type(Target.Type.WORKFLOW)
                .destinationId(projectId)
                .build();
        target.setTransformers(this.transformerTemplate());
        var connection = Connection.Builder.aConnection()
                .sourceId(source.getId())
                .targetId(target.getId())
                .build();
        this.sourceRepository.save(source);
        this.targetRepository.save(target);
        this.connectionRepository.save(connection);
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
        return Set.of(refTf, objectKindTf, beforeTf, afterTf, eventTf);
    }
}
