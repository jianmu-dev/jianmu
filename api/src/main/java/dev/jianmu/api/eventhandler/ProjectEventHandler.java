package dev.jianmu.api.eventhandler;

import dev.jianmu.application.event.InstallDefinitionsEvent;
import dev.jianmu.application.service.ProjectApplication;
import dev.jianmu.application.service.TaskDefinitionApplication;
import dev.jianmu.application.service.WorkflowInstanceApplication;
import dev.jianmu.project.aggregate.Project;
import dev.jianmu.task.aggregate.Definition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @class: DslEventHandler
 * @description: Dsl事件处理器
 * @author: Ethan Liu
 * @create: 2021-04-23 17:21
 **/
@Component
@Slf4j
public class ProjectEventHandler {
    private final WorkflowInstanceApplication workflowInstanceApplication;
    private final ProjectApplication projectApplication;
    private final TaskDefinitionApplication taskDefinitionApplication;

    public ProjectEventHandler(
            WorkflowInstanceApplication workflowInstanceApplication,
            ProjectApplication projectApplication,
            TaskDefinitionApplication taskDefinitionApplication
    ) {
        this.workflowInstanceApplication = workflowInstanceApplication;
        this.projectApplication = projectApplication;
        this.taskDefinitionApplication = taskDefinitionApplication;
    }

    @Async
    @EventListener
    public void handleTriggerEvent(Project project) {
        // 使用project id与WorkflowVersion作为triggerId,用于参数引用查询，参见WorkerApplication#getEnvironmentMap
        this.workflowInstanceApplication.createAndStart(
                project.getId() + project.getWorkflowVersion(),
                project.getWorkflowRef() + project.getWorkflowVersion()
        );
    }

    @EventListener
    // TODO 不要直接用基本类型传递事件
    public void handleGitRepoSyncEvent(String projectId) {
        this.projectApplication.syncProject(projectId);
    }

    @EventListener
    public void handleDefinitionsFromRegistry(InstallDefinitionsEvent event) {
        List<Definition> definitionsFromRegistry = event.getDefinitions();
        definitionsFromRegistry.forEach(definition -> {
            log.info("安装节点定义: {}:{}", definition.getRef(), definition.getVersion());
        });
        this.taskDefinitionApplication.installDefinitions(definitionsFromRegistry);
    }
}
