package dev.jianmu.api.controller;

import com.github.pagehelper.PageInfo;
import dev.jianmu.api.dto.EbDto;
import dev.jianmu.api.dto.PageDto;
import dev.jianmu.api.dto.TransformerDto;
import dev.jianmu.api.mapper.*;
import dev.jianmu.api.vo.*;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.service.*;
import dev.jianmu.eventbridge.aggregate.Bridge;
import dev.jianmu.eventbridge.aggregate.Transformer;
import dev.jianmu.node.definition.aggregate.NodeDefinitionVersion;
import dev.jianmu.infrastructure.storage.StorageService;
import dev.jianmu.project.aggregate.Project;
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.secret.aggregate.Namespace;
import dev.jianmu.task.aggregate.InstanceParameter;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import dev.jianmu.workflow.aggregate.process.TaskStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @class: ProjectViewController
 * @description: ProjectViewController
 * @author: Ethan Liu
 * @create: 2021-06-04 17:23
 **/
@RestController
@RequestMapping("view")
@Tag(name = "查询API", description = "查询API")
public class ViewController {
    private final ProjectApplication projectApplication;
    private final WorkflowInstanceApplication workflowInstanceApplication;
    private final HubApplication hubApplication;
    private final SecretApplication secretApplication;
    private final EventBridgeApplication eventBridgeApplication;
    private final WorkflowInstanceApplication instanceApplication;
    private final TaskInstanceApplication taskInstanceApplication;
    private final ParameterApplication parameterApplication;
    private final StorageService storageService;

    public ViewController(
            ProjectApplication projectApplication,
            WorkflowInstanceApplication workflowInstanceApplication,
            HubApplication hubApplication,
            SecretApplication secretApplication,
            EventBridgeApplication eventBridgeApplication,
            WorkflowInstanceApplication instanceApplication,
            TaskInstanceApplication taskInstanceApplication,
            ParameterApplication parameterApplication,
            StorageService storageService
    ) {
        this.projectApplication = projectApplication;
        this.workflowInstanceApplication = workflowInstanceApplication;
        this.hubApplication = hubApplication;
        this.secretApplication = secretApplication;
        this.eventBridgeApplication = eventBridgeApplication;
        this.instanceApplication = instanceApplication;
        this.taskInstanceApplication = taskInstanceApplication;
        this.parameterApplication = parameterApplication;
        this.storageService = storageService;
    }

    @GetMapping("/parameters/types")
    @Operation(summary = "参数类型获取接口", description = "参数类型获取接口")
    public Parameter.Type[] getTypes() {
        return Parameter.Type.values();
    }

    @GetMapping("/event_bridges")
    @Operation(summary = "分页查询event bridges列表", description = "分页查询event bridges列表")
    public PageInfo<Bridge> findAllEb(PageDto dto) {
        return this.eventBridgeApplication.findAll(dto.getPageNum(), dto.getPageSize());
    }

    @GetMapping("/event_bridges/{bridgeId}")
    @Operation(summary = "查询event bridge", description = "查询event bridge")
    public EbDto findEbById(@PathVariable String bridgeId) {
        var bridge = this.eventBridgeApplication.findBridgeById(bridgeId);
        var source = this.eventBridgeApplication.findSourceByBridgeId(bridgeId);
        var sourceDto = SourceMapper.INSTANCE.toSourceDto(source);
        var targets = this.eventBridgeApplication.findTargetsByBridgeId(bridgeId).stream()
                .map(target -> {
                    var projectName = "";
                    if (target.getDestinationId() != null) {
                        projectName = this.projectApplication.findById(target.getDestinationId())
                                .map(Project::getWorkflowName).orElse("");
                    }
                    return TargetMapper.INSTANCE.toTargetDto(target, projectName);
                })
                .collect(Collectors.toList());
        return EbDto.builder()
                .bridge(bridge)
                .source(sourceDto)
                .targets(targets)
                .build();
    }

    @GetMapping("/target_events/{triggerId}")
    @Operation(summary = "查询目标event参数", description = "查询目标event参数")
    public TargetEventVo findTargetEvent(@PathVariable String triggerId) {
        var targetEvent = this.eventBridgeApplication.findTargetEvent(triggerId);
        return TargetEventMapper.INSTANCE.toTargetEventVo(targetEvent);
    }

    @GetMapping("/event_bridges/templates")
    @Operation(summary = "转换器模版列表", description = "转换器模版列表")
    public List<String> findTemplates() {
        return List.of("Gitee", "Gitlab");
    }

    @GetMapping("/event_bridges/templates/{name}")
    @Operation(summary = "转换器模版详情", description = "转换器模版详情")
    public List<TransformerDto> findTemplate(@PathVariable String name) {
        List<Transformer> temps = List.of();
        if (name.equals("Gitee")) {
            temps = this.eventBridgeApplication.giteeTemplates();
        }
        if (name.equals("Gitlab")) {
            temps = this.eventBridgeApplication.gitlabTemplates();
        }
        return TargetMapper.INSTANCE.toTransformerDtos(temps);
    }

    @GetMapping("/namespaces")
    @Operation(summary = "查询命名空间列表", description = "查询命名空间列表")
    public NameSpacesVo findAllNamespace() {
        var type = this.secretApplication.getCredentialManagerType();
        var list = this.secretApplication.findAll();
        return NameSpacesVo.builder()
                .credentialManagerType(type)
                .list(list)
                .build();
    }

    @GetMapping("/namespaces/{name}")
    @Operation(summary = "查询命名空间详情", description = "查询命名空间详情")
    public Namespace findByName(@PathVariable String name) {
        return this.secretApplication.findByName(name).orElseThrow(() -> new DataNotFoundException("未找到该命名空间"));
    }

    @GetMapping("/namespaces/{name}/keys")
    @Operation(summary = "查询键值对列表", description = "查询键值对列表")
    public List<String> findAll(@PathVariable String name) {
        var kvs = this.secretApplication.findAllByNamespaceName(name);
        return kvs.stream().map(KVPair::getKey).collect(Collectors.toList());
    }

    @GetMapping("/nodes")
    @Operation(summary = "分页查询节点定义列表", description = "分页查询节点定义列表")
    public PageInfo<NodeDefVo> findNodeAll(PageDto dto) {
        var page = this.hubApplication.findPage(
                dto.getPageNum(),
                dto.getPageSize()
        );
        var nodes = page.getList();
        List<NodeDefVo> nodeDefVos = nodes.stream().map(nodeDefinition -> {
            var versions = this.hubApplication.findByOwnerRefAndRef(nodeDefinition.getOwnerRef(), nodeDefinition.getRef()).stream()
                    .map(NodeDefinitionVersion::getVersion).collect(Collectors.toList());
            return NodeDefVo.builder()
                    .icon(nodeDefinition.getIcon())
                    .name(nodeDefinition.getName())
                    .ownerName(nodeDefinition.getOwnerName())
                    .ownerType(nodeDefinition.getOwnerType())
                    .ownerRef(nodeDefinition.getOwnerRef())
                    .creatorName(nodeDefinition.getCreatorName())
                    .creatorRef(nodeDefinition.getCreatorRef())
                    .type(nodeDefinition.getType())
                    .description(nodeDefinition.getDescription())
                    .ref(nodeDefinition.getRef())
                    .sourceLink(nodeDefinition.getSourceLink())
                    .documentLink(nodeDefinition.getDocumentLink())
                    .versions(versions)
                    .build();
        }).collect(Collectors.toList());
        PageInfo<NodeDefVo> newPage = PageUtils.pageInfo2PageInfoVo(page);
        newPage.setList(nodeDefVos);
        return newPage;
    }

    @GetMapping("/projects")
    @Operation(summary = "查询项目列表", description = "查询项目列表")
    public List<ProjectVo> findAll() {
        var projects = this.projectApplication.findAll();
        return projects.stream().map(project -> this.instanceApplication
                .findByRefAndSerialNoMax(project.getWorkflowRef())
                .map(workflowInstance -> {
                    var projectVo = ProjectMapper.INSTANCE.toProjectVo(project);
                    projectVo.setLatestTime(workflowInstance.getEndTime());
                    projectVo.setNextTime(this.projectApplication.getNextFireTime(project.getId()));
                    if (workflowInstance.getStatus().equals(ProcessStatus.TERMINATED)) {
                        projectVo.setStatus("FAILED");
                    }
                    if (workflowInstance.getStatus().equals(ProcessStatus.FINISHED)) {
                        projectVo.setStatus("SUCCEEDED");
                    }
                    if (workflowInstance.getStatus().equals(ProcessStatus.RUNNING)) {
                        projectVo.setStatus("RUNNING");
                    }
                    return projectVo;
                })
                .orElseGet(() -> ProjectMapper.INSTANCE.toProjectVo(project))).collect(Collectors.toList());
    }

    @GetMapping("/projects/{projectId}")
    @Operation(summary = "获取项目详情", description = "获取项目详情")
    public ProjectDetailVo getProject(@PathVariable String projectId) {
        var project = this.projectApplication.findById(projectId).orElseThrow(() -> new DataNotFoundException("未找到该项目"));
        return ProjectMapper.INSTANCE.toProjectDetailVo(project);
    }

    @GetMapping("/projects/{projectId}/dsl")
    @Operation(summary = "获取项目DSL", description = "获取项目DSL", deprecated = true)
    public String getProjectDsl(@PathVariable String projectId) {
        return this.projectApplication.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目"))
                .getDslText();
    }

    @GetMapping("/repo/{gitRepoId}")
    public void gotoRepo(@PathVariable String gitRepoId, HttpServletResponse response) throws IOException {
        var repo = this.projectApplication.findGitRepoById(gitRepoId);
        response.sendRedirect(repo.getUri());
    }

    @GetMapping("/workflow_instances/{workflowRef}")
    @Operation(summary = "根据workflowRef查询流程实例列表", description = "根据workflowRef查询流程实例列表")
    public List<WorkflowInstanceVo> findByWorkflowRef(@PathVariable String workflowRef) {
        var instances = this.instanceApplication.findByWorkflowRef(workflowRef);
        return WorkflowInstanceMapper.INSTANCE.toWorkflowInstanceVoList(instances);
    }

    @GetMapping("/workflow/{ref}/{version}")
    @Operation(summary = "获取流程定义", description = "获取流程定义")
    public WorkflowVo findByRefAndVersion(@PathVariable String ref, @PathVariable String version) {
        var workflow = this.projectApplication.findByRefAndVersion(ref, version);
        return WorkflowMapper.INSTANCE.toWorkflowVo(workflow);
    }

    @GetMapping("/task_instances/{workflowInstanceId}")
    @Operation(summary = "任务实例列表接口", description = "任务实例列表接口")
    public List<TaskInstanceVo> findByBusinessId(@PathVariable String workflowInstanceId) {
        List<TaskInstanceVo> list = new ArrayList<>();
        var taskInstances = this.taskInstanceApplication.findByBusinessId(workflowInstanceId);
        this.workflowInstanceApplication.findById(workflowInstanceId)
                .ifPresent(workflowInstance -> {
                    workflowInstance.getAsyncTaskInstances()
                            .stream()
                            .filter(asyncTaskInstance -> asyncTaskInstance.getStatus().equals(TaskStatus.SKIPPED))
                            .forEach(asyncTaskInstance -> {
                                var vo = TaskInstanceMapper.INSTANCE.asyncTaskInstanceToTaskInstanceVo(asyncTaskInstance);
                                list.add(vo);
                            });
                });
        taskInstances.forEach(taskInstance -> {
            var vo = TaskInstanceMapper.INSTANCE.toTaskInstanceVo(taskInstance);
            list.add(vo);
        });
        return list;
    }

    @GetMapping("/task_instance/{instanceId}")
    @Operation(summary = "任务实例详情接口", description = "任务实例详情接口")
    public TaskInstanceVo findById(@PathVariable String instanceId) {
        var taskInstance = this.taskInstanceApplication.findById(instanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        return TaskInstanceMapper.INSTANCE.toTaskInstanceVo(taskInstance);
    }

    @GetMapping("/task_instance/{instanceId}/parameters")
    @Operation(summary = "查询任务实例参数接口", description = "查询任务实例参数接口")
    public List<InstanceParameterVo> findParameters(@PathVariable String instanceId) {
        var instanceParameters = this.taskInstanceApplication.findParameters(instanceId);
        var ids = instanceParameters.stream().map(InstanceParameter::getParameterId).collect(Collectors.toSet());
        var parameters = this.parameterApplication.findParameters(ids);
        return parameters.stream()
                .map(parameter -> {
                    var instanceParameterVo = new InstanceParameterVo();
                    instanceParameters.forEach(instanceParameter -> {
                        if (instanceParameter.getParameterId().equals(parameter.getId())) {
                            instanceParameterVo.setRef(instanceParameter.getRef());
                            instanceParameterVo.setType(instanceParameter.getType().toString());
                            instanceParameterVo.setValueType(parameter.getType().toString());
                            instanceParameterVo.setValue(parameter.getStringValue());
                        }
                    });
                    return instanceParameterVo;
                }).collect(Collectors.toList());
    }

    @GetMapping("/logs/{logId}")
    @Operation(summary = "日志获取接口", description = "日志获取接口,可以使用Range方式分段获取")
    public ResponseEntity<FileSystemResource> getLog(@PathVariable String logId) {
        var fileSystemResource = new FileSystemResource(this.storageService.logFile(logId));
        if (fileSystemResource.exists()) {
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(fileSystemResource);
        } else {
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GetMapping("/logs/workflow/{logId}")
    @Operation(summary = "流程日志获取接口", description = "流程日志获取接口,可以使用Range方式分段获取")
    public ResponseEntity<FileSystemResource> getWorkflowLog(@PathVariable String logId) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(new FileSystemResource(this.storageService.workflowLogFile(logId)));
    }
}
