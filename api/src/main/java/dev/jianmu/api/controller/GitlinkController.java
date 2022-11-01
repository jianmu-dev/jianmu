package dev.jianmu.api.controller;

import dev.jianmu.api.dto.gitlink.GitLinkWebhookDto;
import dev.jianmu.application.dsl.DslParser;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.service.GitRepoApplication;
import dev.jianmu.application.service.ProjectApplication;
import dev.jianmu.application.util.AssociationUtil;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.enumeration.ThirdPartyTypeEnum;
import dev.jianmu.oauth2.api.impl.OAuth2ApiProxy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashSet;

/**
 * @author huangxi
 * @class GitlinkController
 * @description GitlinkController
 * @create 2022-07-29 18:16
 */
@RestController
@RequestMapping
@Tag(name = "Gitlink控制器", description = "Gitlink控制器")
@Slf4j
public class GitlinkController {
    private final GitRepoApplication gitRepoApplication;
    private final ProjectApplication projectApplication;
    private final OAuth2Properties oAuth2Properties;

    public GitlinkController(
            GitRepoApplication gitRepoApplication,
            ProjectApplication projectApplication,
            OAuth2Properties oAuth2Properties
    ) {
        this.gitRepoApplication = gitRepoApplication;
        this.projectApplication = projectApplication;
        this.oAuth2Properties = oAuth2Properties;
    }

    @PostMapping("webhook/projects/sync")
    @Operation(summary = "同步项目webhook", description = "同步项目webhook")
    public void syncProject(@RequestBody @Valid GitLinkWebhookDto dto) {
        // 校验是否为jianmu用户
        var committer = dto.getHeadCommit().getCommitter();
        if (ProjectApplication.committer.equals(committer.getName()) && ProjectApplication.committerEmail.equals(committer.getEmail())) {
            return;
        }
        var gitRepo = this.gitRepoApplication.findByRefAndOwner(dto.getRepository().getName(), dto.getRepository().getOwner().getLogin())
                .orElseThrow(() -> new DataNotFoundException("未找到Git仓库：" + dto.getRepository().getOwner().getLogin() + "/" + dto.getRepository().getName()));
        // TODO： 暂时使用pusher查询用户
        var user = this.gitRepoApplication.getUserByUserName(dto.getPusher().getLogin());
        var branch = dto.getBranch();
        var accessToken = this.gitRepoApplication.getAccessToken();
        var set = new HashSet<String>();
        dto.getCommits().forEach(commit -> {
            set.addAll(commit.getAdded());
            set.addAll(commit.getModified());
        });
        for (String filepath : set) {
            try {
                if (filepath.matches("^.devops/.*.yml$") && filepath.split("/").length == 2) {
                    var dsl = this.findDslByFilepath(accessToken, user.getUserId(), gitRepo.getOwner(), gitRepo.getRef(), filepath, branch);
                    this.createOrUpdateProject(filepath, dsl, user.getUserId(), gitRepo.getId(), branch);
                }
            } catch (Exception e) {
                log.warn("项目同步失败: ", e);
            }
        }
    }

    private void createOrUpdateProject(String filepath, String dslText, String userId, String repoId, String branch) {
        String filename = filepath.split("/")[1].replace(".yml", "");
        try {
            var dsl = DslParser.parse(dslText);
            if (!dsl.getName().equals(filename)) {
                return;
            }
        } catch (Exception e) {
            log.warn("DSL文件地址：{}, DSL校验失败：{}", filepath, e.getMessage());
            return;
        }
        var projectOptional = this.projectApplication.findByName(repoId, AssociationUtil.AssociationType.GIT_REPO.name(), filename);
        if (projectOptional.isEmpty()) {
            this.projectApplication.createProject(dslText, null, userId, repoId, AssociationUtil.AssociationType.GIT_REPO.name(), branch, false);
            return;
        }
        var project = projectOptional.get();
        this.gitRepoApplication.findById(repoId)
                .findFlowByProjectId(project.getId())
                .filter(flow -> flow.getBranchName().equals(branch))
                .ifPresent(flow -> this.projectApplication.updateProject(project.getId(), dslText, null, userId, repoId, AssociationUtil.AssociationType.GIT_REPO.name(), false));
    }

    private String findDslByFilepath(String accessToken, String userId, String owner, String repo, String filepath, String branch) {
        var oAuth2Api = OAuth2ApiProxy.builder()
                .thirdPartyType(ThirdPartyTypeEnum.valueOf(this.oAuth2Properties.getThirdPartyType()))
                .userId(userId)
                .build();
        try {
            var vo = oAuth2Api.getFile(accessToken, owner, repo, filepath, branch);
            return vo.getContent();
        } catch (Exception e) {
            throw new RuntimeException("未找到项目DSL文件：" + e.getMessage());
        }
    }
}