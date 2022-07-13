package dev.jianmu.api.controller;

import dev.jianmu.api.dto.GitRepoFlowViewingDto;
import dev.jianmu.api.mapper.ProjectVoMapper;
import dev.jianmu.api.vo.GitRepoBranchVo;
import dev.jianmu.api.vo.ProjectVo;
import dev.jianmu.application.service.GitRepoApplication;
import dev.jianmu.application.service.TriggerApplication;
import dev.jianmu.git.repo.aggregate.Flow;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Daihw
 * @class GitRepoController
 * @description GitRepoController
 * @create 2022/7/5 5:22 下午
 */
@RestController
@RequestMapping("git_repos")
@Tag(name = "GitRepo", description = "GitRepo API")
@SecurityRequirement(name = "bearerAuth")
public class GitRepoController {
    private final GitRepoApplication gitRepoApplication;
    private final TriggerApplication triggerApplication;

    public GitRepoController(GitRepoApplication gitRepoApplication, TriggerApplication triggerApplication) {
        this.gitRepoApplication = gitRepoApplication;
        this.triggerApplication = triggerApplication;
    }

    @GetMapping("/{id}/branches")
    @Operation(summary = "查询分支列表", description = "查询分支列表")
    public List<GitRepoBranchVo> findBranches(@PathVariable String id) {
        return this.gitRepoApplication.findBranches(id).stream()
                .map(branch -> GitRepoBranchVo.builder()
                        .branchName(branch.getName())
                        .isDefault(branch.getIsDefault())
                        .build())
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/flows")
    @Operation(summary = "查询流水线列表", description = "查询流水线列表")
    public List<ProjectVo> findFlows(@PathVariable("id") String id, GitRepoFlowViewingDto dto) {
        var gitRepo = this.gitRepoApplication.findById(id);
        return this.gitRepoApplication.findFlows(gitRepo, dto.getName(), dto.getStatus(), dto.getBranch(), dto.getSortTypeName())
                .stream()
                .map(project -> {
                    var projectVo = ProjectVoMapper.INSTANCE.toProjectVo(project);
                    projectVo.setNextTime(this.triggerApplication.getNextFireTime(project.getId()));
                    if (project.getStatus() == null) {
                        return projectVo;
                    }
                    if (project.getStatus().equals(ProcessStatus.TERMINATED.name())) {
                        projectVo.setStatus("FAILED");
                    }
                    if (project.getStatus().equals(ProcessStatus.FINISHED.name())) {
                        projectVo.setStatus("SUCCEEDED");
                    }
                    if (project.getStatus().equals(ProcessStatus.SUSPENDED.name())) {
                        projectVo.setSuspendedTime(project.getSuspendedTime());
                        projectVo.setStatus("SUSPENDED");
                    }
                    if (project.getStatus().equals(ProcessStatus.RUNNING.name())) {
                        projectVo.setStartTime(project.getStartTime());
                        projectVo.setStatus("RUNNING");
                    }
                    projectVo.setBranch(gitRepo.getFlows().stream()
                            .filter(flow -> flow.getProjectId().equals(projectVo.getId()))
                            .findFirst()
                            .map(Flow::getBranchName)
                            .orElse(null));
                    return projectVo;
                }).collect(Collectors.toList());
    }
}
