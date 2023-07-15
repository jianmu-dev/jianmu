package dev.jianmu.api.controller;

import dev.jianmu.api.dto.AddGroup;
import dev.jianmu.api.dto.DslTextDto;
import dev.jianmu.api.dto.GitRepoDto;
import dev.jianmu.api.dto.ProjectTriggeringDto;
import dev.jianmu.api.mapper.GitRepoMapper;
import dev.jianmu.api.vo.ProjectIdVo;
import dev.jianmu.api.vo.TriggerDefinitionVo;
import dev.jianmu.api.vo.TriggerProjectVo;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.service.GitApplication;
import dev.jianmu.application.service.ProjectApplication;
import dev.jianmu.application.service.TriggerApplication;
import dev.jianmu.trigger.aggregate.Trigger;
import dev.jianmu.trigger.aggregate.WebRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * @author Ethan Liu
 * @class ProjectController
 * @description ProjectController
 * @create 2021-05-14 14:00
 */
@RestController
@RequestMapping("projects")
@Tag(name = "项目API", description = "项目API")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {
    private final ProjectApplication projectApplication;
    private final GitApplication gitApplication;
    private final TriggerApplication triggerApplication;

    public ProjectController(ProjectApplication projectApplication, GitApplication gitApplication, TriggerApplication triggerApplication) {
        this.projectApplication = projectApplication;
        this.gitApplication = gitApplication;
        this.triggerApplication = triggerApplication;
    }

    @PutMapping("/enable/{projectId}")
    @Operation(summary = "激活项目", description = "激活项目")
    public void enable(@PathVariable String projectId) {
        this.projectApplication.switchEnabled(projectId, true);
    }

    @PutMapping("/disable/{projectId}")
    @Operation(summary = "禁用项目", description = "禁用项目")
    public void disable(@PathVariable String projectId) {
        this.projectApplication.switchEnabled(projectId, false);
    }

    @PostMapping("/trigger/{projectId}")
    @Operation(summary = "触发项目", description = "触发项目启动")
    public TriggerProjectVo trigger(@Parameter(description = "触发器ID") @PathVariable String projectId, @Valid @RequestBody ProjectTriggeringDto dto) {
        var triggerEvent = this.projectApplication.triggerByManual(projectId, dto.toMap());
        return TriggerProjectVo.builder()
                .triggerId(triggerEvent.getTriggerId())
                .build();
    }

    @PostMapping
    @Operation(summary = "创建项目", description = "上传DSL并创建项目")
    public ProjectIdVo createProject(@RequestBody @Valid DslTextDto dslTextDto) {
        var project = this.projectApplication.createProject(dslTextDto.getDslText(), dslTextDto.getProjectGroupId());
        return ProjectIdVo.builder().id(project.getId()).build();
    }

//    @PostMapping("/import")
//    @Operation(summary = "导入DSL", description = "导入Git库中的DSL文件创建项目")
    public void importDsl(@RequestBody @Validated(AddGroup.class) GitRepoDto gitRepoDto) {
        var gitRepo = GitRepoMapper.INSTANCE.toGitRepo(gitRepoDto);
        this.projectApplication.importProject(gitRepo, gitRepoDto.getProjectGroupId());
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "更新项目", description = "根据ID更新项目DSL定义")
    public void updateProject(@PathVariable String projectId, @RequestBody @Valid DslTextDto dslTextDto) {
        this.projectApplication.updateProject(projectId, dslTextDto.getDslText(), dslTextDto.getProjectGroupId());
    }

    @PutMapping("/sync/{projectId}")
    @Operation(summary = "同步DSL", description = "同步Git库中的DSL文件更新项目")
    public void syncProject(@PathVariable String projectId) {
        this.gitApplication.syncGitRepo(projectId);
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "删除项目", description = "删除项目")
    public void deleteById(@PathVariable String projectId) {
        this.projectApplication.deleteById(projectId);
    }

    @GetMapping("/{projectId}/trigger_def")
    @Operation(summary = "获取webhook触发器定义")
    public TriggerDefinitionVo findTriggerDef(@PathVariable String projectId) {
        var trigger = this.triggerApplication.findTrigger(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到触发器"));
        if (trigger.getType() != Trigger.Type.WEBHOOK) {
            throw new RuntimeException("非webhook触发器");
        }
        var webRequest = this.triggerApplication.findLatestWebRequest(projectId);
        return TriggerDefinitionVo.builder()
                .params(trigger.getWebhook().getParam())
                .auth(trigger.getWebhook().getAuth())
                .only(trigger.getWebhook().getOnly())
                .latestWebRequestId(webRequest.map(WebRequest::getId).orElse(null))
                .build();
    }
}
