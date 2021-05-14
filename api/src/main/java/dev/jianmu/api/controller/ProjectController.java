package dev.jianmu.api.controller;

import dev.jianmu.api.dto.AddGroup;
import dev.jianmu.api.dto.GitRepoDto;
import dev.jianmu.api.mapper.GitRepoMapper;
import dev.jianmu.application.service.DslApplication;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @class: ProjectController
 * @description: ProjectController
 * @author: Ethan Liu
 * @create: 2021-05-14 14:00
 **/
@RestController
@RequestMapping("project")
@Tag(name = "Project API", description = "Project API")
public class ProjectController {
    private final DslApplication dslApplication;

    public ProjectController(DslApplication dslApplication) {
        this.dslApplication = dslApplication;
    }

    @PostMapping("/import")
    public void importDsl(@RequestBody @Validated(AddGroup.class) GitRepoDto gitRepoDto) {
        var gitRepo = GitRepoMapper.INSTANCE.toGitRepo(gitRepoDto);
        this.dslApplication.importProject(gitRepo);
    }

    @PutMapping("/sync/{projectId}")
    public void syncProject(@PathVariable String projectId) {
        this.dslApplication.syncProject(projectId);
    }
}
