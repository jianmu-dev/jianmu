package dev.jianmu.api.controller;

import com.github.pagehelper.PageInfo;
import dev.jianmu.api.dto.NamespaceSearchDto;
import dev.jianmu.api.dto.PageDto;
import dev.jianmu.api.mapper.ProjectMapper;
import dev.jianmu.api.mapper.TaskInstanceMapper;
import dev.jianmu.api.mapper.WorkflowInstanceMapper;
import dev.jianmu.api.vo.*;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.service.*;
import dev.jianmu.hub.intergration.aggregate.NodeDefinitionVersion;
import dev.jianmu.infrastructure.storage.StorageService;
import dev.jianmu.secret.aggregate.KVPair;
import dev.jianmu.secret.aggregate.Namespace;
import dev.jianmu.task.aggregate.InstanceParameter;
import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
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
    private final HubApplication hubApplication;
    private final SecretApplication secretApplication;
    private final WorkflowInstanceApplication instanceApplication;
    private final TaskInstanceApplication taskInstanceApplication;
    private final ParameterApplication parameterApplication;
    private final StorageService storageService;

    public ViewController(
            ProjectApplication projectApplication,
            HubApplication hubApplication,
            SecretApplication secretApplication,
            WorkflowInstanceApplication instanceApplication,
            TaskInstanceApplication taskInstanceApplication,
            ParameterApplication parameterApplication,
            StorageService storageService
    ) {
        this.projectApplication = projectApplication;
        this.hubApplication = hubApplication;
        this.secretApplication = secretApplication;
        this.instanceApplication = instanceApplication;
        this.taskInstanceApplication = taskInstanceApplication;
        this.parameterApplication = parameterApplication;
        this.storageService = storageService;
    }

    @GetMapping("/namespaces")
    @Operation(summary = "分页查询命名空间列表", description = "分页查询命名空间列表")
    public PageInfo<Namespace> findAll(NamespaceSearchDto namespaceSearchDto) {
        return this.secretApplication.findAll(namespaceSearchDto.getName(), namespaceSearchDto.getPageNum(), namespaceSearchDto.getPageSize());
    }

    @GetMapping("/namespaces/{name}/keys")
    @Operation(summary = "查询键值对列表", description = "查询键值对列表")
    public List<String> findAll(@PathVariable String name) {
        var kvs = this.secretApplication.findAll(name);
        return kvs.stream().map(KVPair::getKey).collect(Collectors.toList());
    }

    @GetMapping("/nodes")
    @Operation(summary = "节点定义列表", description = "节点定义列表查询")
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
                    } else {
                        projectVo.setStatus(workflowInstance.findLatestAsyncTaskInstance()
                                .orElse(AsyncTaskInstance.Builder.anAsyncTaskInstance().build())
                                .getStatus().name()
                        );
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
    @Operation(summary = "获取DSL源码", description = "获取DSL源码")
    public String findByRefAndVersion(@PathVariable String ref, @PathVariable String version) {
        return this.projectApplication.findByRefAndVersion(ref, version).getDslText();
    }

    @GetMapping("/task_instances/{workflowInstanceId}")
    @Operation(summary = "任务实例列表接口", description = "任务实例列表接口")
    public List<TaskInstanceVo> findByBusinessId(@PathVariable String workflowInstanceId) {
        List<TaskInstanceVo> list = new ArrayList<>();
        var taskInstances = this.taskInstanceApplication.findByBusinessId(workflowInstanceId);
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
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(new FileSystemResource(this.storageService.logFile(logId)));
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
