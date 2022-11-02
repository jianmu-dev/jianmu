package dev.jianmu.application.service;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.util.AssociationUtil;
import dev.jianmu.external_parameter.repository.ExternalParameterLabelRepository;
import dev.jianmu.external_parameter.repository.ExternalParameterRepository;
import dev.jianmu.git.repo.aggregate.Branch;
import dev.jianmu.git.repo.aggregate.Flow;
import dev.jianmu.git.repo.aggregate.GitRepo;
import dev.jianmu.git.repo.aggregate.User;
import dev.jianmu.git.repo.event.GitRepoDeletedEvent;
import dev.jianmu.git.repo.repository.AccessTokenRepository;
import dev.jianmu.git.repo.repository.GitRepoRepository;
import dev.jianmu.git.repo.repository.UserRepository;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.enumeration.ThirdPartyTypeEnum;
import dev.jianmu.oauth2.api.impl.OAuth2ApiProxy;
import dev.jianmu.project.query.ProjectVo;
import dev.jianmu.project.repository.ProjectGroupRepository;
import dev.jianmu.project.repository.ProjectLastExecutionRepository;
import dev.jianmu.project.repository.ProjectLinkGroupRepository;
import dev.jianmu.project.repository.ProjectRepository;
import dev.jianmu.secret.aggregate.CredentialManager;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.trigger.aggregate.Trigger;
import dev.jianmu.trigger.repository.TriggerEventRepository;
import dev.jianmu.trigger.repository.TriggerRepository;
import dev.jianmu.trigger.repository.WebRequestRepository;
import dev.jianmu.trigger.service.CustomWebhookDomainService;
import dev.jianmu.workflow.repository.AsyncTaskInstanceRepository;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
import dev.jianmu.workflow.repository.WorkflowRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static dev.jianmu.application.service.ProjectGroupApplication.DEFAULT_PROJECT_GROUP_NAME;

/**
 * @author Daihw
 * @class GitRepoApplication
 * @description GitRepoApplication
 * @create 2022/7/5 9:45 上午
 */
@Service
@Slf4j
public class GitRepoApplication {
    private final GitRepoRepository gitRepoRepository;
    private final ProjectRepository projectRepository;
    private final ProjectLastExecutionRepository projectLastExecutionRepository;
    private final ProjectLinkGroupRepository projectLinkGroupRepository;
    private final CredentialManager credentialManager;
    private final ExternalParameterRepository externalParameterRepository;
    private final ExternalParameterLabelRepository externalParameterLabelRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final AsyncTaskInstanceRepository asyncTaskInstanceRepository;
    private final TaskInstanceRepository taskInstanceRepository;
    private final TriggerRepository triggerRepository;
    private final TriggerEventRepository triggerEventRepository;
    private final WebRequestRepository webRequestRepository;
    private final Scheduler quartzScheduler;
    private final ProjectGroupRepository projectGroupRepository;
    private final ApplicationEventPublisher publisher;
    private final OAuth2Properties oAuth2Properties;
    private final AccessTokenRepository accessTokenRepository;
    private final CustomWebhookDomainService customWebhookDomainService;
    private final UserRepository userRepository;

    public GitRepoApplication(
            GitRepoRepository gitRepoRepository,
            ProjectRepository projectRepository,
            ProjectLastExecutionRepository projectLastExecutionRepository,
            ProjectLinkGroupRepository projectLinkGroupRepository,
            CredentialManager credentialManager,
            ExternalParameterRepository externalParameterRepository,
            ExternalParameterLabelRepository externalParameterLabelRepository,
            WorkflowRepository workflowRepository,
            WorkflowInstanceRepository workflowInstanceRepository,
            AsyncTaskInstanceRepository asyncTaskInstanceRepository,
            TaskInstanceRepository taskInstanceRepository,
            TriggerRepository triggerRepository,
            TriggerEventRepository triggerEventRepository,
            WebRequestRepository webRequestRepository,
            Scheduler quartzScheduler,
            ProjectGroupRepository projectGroupRepository,
            ApplicationEventPublisher publisher,
            OAuth2Properties oAuth2Properties,
            AccessTokenRepository accessTokenRepository,
            CustomWebhookDomainService customWebhookDomainService,
            UserRepository userRepository
    ) {
        this.gitRepoRepository = gitRepoRepository;
        this.projectRepository = projectRepository;
        this.projectLastExecutionRepository = projectLastExecutionRepository;
        this.projectLinkGroupRepository = projectLinkGroupRepository;
        this.credentialManager = credentialManager;
        this.externalParameterRepository = externalParameterRepository;
        this.externalParameterLabelRepository = externalParameterLabelRepository;
        this.workflowRepository = workflowRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.asyncTaskInstanceRepository = asyncTaskInstanceRepository;
        this.taskInstanceRepository = taskInstanceRepository;
        this.triggerRepository = triggerRepository;
        this.triggerEventRepository = triggerEventRepository;
        this.webRequestRepository = webRequestRepository;
        this.quartzScheduler = quartzScheduler;
        this.projectGroupRepository = projectGroupRepository;
        this.publisher = publisher;
        this.oAuth2Properties = oAuth2Properties;
        this.accessTokenRepository = accessTokenRepository;
        this.customWebhookDomainService = customWebhookDomainService;
        this.userRepository = userRepository;
    }

    public GitRepo findById(String id) {
        return this.gitRepoRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到git仓库：" + id));
    }

    @Transactional
    public void sync(String userId, String id, String owner, String ref, List<Branch> branches) {
        var oAuth2Api = OAuth2ApiProxy.builder()
                .thirdPartyType(ThirdPartyTypeEnum.valueOf(this.oAuth2Properties.getThirdPartyType()))
                .userId(userId)
                .build();
        var accessToken = this.accessTokenRepository.get();
        var isCreated = this.syncBranches(id, owner, ref, branches);
        if (isCreated) {
            var webhookUrl = this.oAuth2Properties.getWebhookHost() + "projects/sync";
            var events = this.customWebhookDomainService.getGitEvents(this.oAuth2Properties.getThirdPartyType(), null);
            oAuth2Api.createWebhook(accessToken, owner, ref, webhookUrl, true, events);
        }
    }

    private Boolean syncBranches(String id, String owner, String ref, List<Branch> branches) {
        var optionalGitRepoById = this.gitRepoRepository.findById(id);
        var optionalGitRepoByRef = this.gitRepoRepository.findByRefAndOwner(ref, owner);
        GitRepo gitRepo;
        if (optionalGitRepoById.isPresent()) {
            gitRepo = optionalGitRepoById.get();
        } else if (optionalGitRepoByRef.isPresent()) {
            this.deleteGitRepo(optionalGitRepoByRef.get());
            gitRepo = new GitRepo(id);
        } else {
            gitRepo = new GitRepo(id);
        }
        boolean isCreated = gitRepo.getOwner() == null;
        // 同步分支
        gitRepo.syncBranches(ref, owner, branches);
        this.gitRepoRepository.saveOrUpdate(gitRepo);
        return isCreated;
    }

    private void deleteGitRepo(GitRepo gitRepo) {
        // 删除gitRepo
        this.gitRepoRepository.deleteById(gitRepo.getId());
        var event = GitRepoDeletedEvent.Builder.aGetRepoDeletedEvent()
                .id(gitRepo.getId())
                .projectIds(gitRepo.getFlows().stream()
                        .map(Flow::getProjectId)
                        .collect(Collectors.toList()))
                .build();
        this.publisher.publishEvent(event);
    }

    @Transactional
    public void addFlow(String projectId, String branch, String gitRepoId) {
        if (gitRepoId == null) {
            return;
        }
        var gitRepo = this.gitRepoRepository.findById(gitRepoId)
                .orElseThrow(() -> new DataNotFoundException("未找到git仓库：" + gitRepoId));
        gitRepo.addFlow(new Flow(projectId, branch));
        this.gitRepoRepository.saveOrUpdate(gitRepo);
    }

    @Transactional
    public void removeFlow(String projectId, String gitRepoId) {
        if (gitRepoId == null) {
            return;
        }
        var gitRepo = this.gitRepoRepository.findById(gitRepoId)
                .orElseThrow(() -> new DataNotFoundException("未找到git仓库：" + gitRepoId));
        gitRepo.removeFlow(projectId);
        this.gitRepoRepository.saveOrUpdate(gitRepo);
    }

    public List<ProjectVo> findFlows(GitRepo gitRepo) {
        var projectIds = gitRepo.getFlows().stream()
                .map(Flow::getProjectId)
                .collect(Collectors.toList());
        if (projectIds.isEmpty()) {
            return Collections.emptyList();
        }
        return this.projectRepository.findVoByIdIn(projectIds);
    }

    public Optional<Flow> findFlowByProjectId(String projectId, String gitRepoId) {
        if (gitRepoId == null) {
            return Optional.empty();
        }
        var gitRepo = this.gitRepoRepository.findById(gitRepoId)
                .orElseThrow(() -> new DataNotFoundException("未找到git仓库：" + gitRepoId));
        return gitRepo.findFlowByProjectId(projectId);
    }

    public List<Branch> findBranches(String id) {
        var gitRepo = this.gitRepoRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到git仓库：" + id));
        return gitRepo.getBranches();
    }

    public Optional<GitRepo> findByRefAndOwner(String ref, String owner) {
        return this.gitRepoRepository.findByRefAndOwner(ref, owner);
    }

    /**
     * 删除GitRepo下的数据
     *
     * @param id
     * @param projectIds
     */
    @Transactional
    public void deleteGitRepoData(String id, List<String> projectIds) {
        // 删除密钥
        this.credentialManager.deleteByAssociationIdAndType(id, AssociationUtil.AssociationType.GIT_REPO.name(), ThirdPartyTypeEnum.GITLINK.name());
        // 删除外部参数
        this.externalParameterRepository.deleteByAssociationIdAndType(id, AssociationUtil.AssociationType.GIT_REPO.name(), ThirdPartyTypeEnum.GITLINK.name());
        this.externalParameterLabelRepository.deleteByAssociationIdAndType(id, AssociationUtil.AssociationType.GIT_REPO.name(), ThirdPartyTypeEnum.GITLINK.name());
        //删除项目关联表
        this.projectLinkGroupRepository.deleteByProjectIdIn(projectIds);
        this.projectGroupRepository.findByName(DEFAULT_PROJECT_GROUP_NAME)
                .ifPresent(p -> this.projectGroupRepository.subProjectCountById(p.getId(), projectIds.size()));
        // 删除项目
        this.projectRepository.findByIdIn(projectIds).forEach(project -> {
            this.projectLastExecutionRepository.deleteByRef(project.getWorkflowRef());
            this.projectRepository.deleteByWorkflowRef(project.getWorkflowRef());
            this.workflowRepository.deleteByRef(project.getWorkflowRef());
            this.workflowInstanceRepository.deleteByWorkflowRef(project.getWorkflowRef());
            this.asyncTaskInstanceRepository.deleteByWorkflowRef(project.getWorkflowRef());
            this.taskInstanceRepository.deleteByWorkflowRef(project.getWorkflowRef());
            // 删除触发器
            this.triggerRepository.findByProjectId(project.getId())
                    .ifPresent(trigger -> {
                        if (trigger.getType() == Trigger.Type.CRON) {
                            try {
                                // 停止触发器
                                quartzScheduler.pauseTrigger(TriggerKey.triggerKey(trigger.getId()));
                                // 卸载任务
                                quartzScheduler.unscheduleJob(TriggerKey.triggerKey(trigger.getId()));
                                // 删除任务
                                quartzScheduler.deleteJob(JobKey.jobKey(trigger.getId()));
                            } catch (SchedulerException e) {
                                log.error("触发器删除失败: {}", e.getMessage());
                            }
                        }
                        this.triggerRepository.deleteById(trigger.getId());
                        this.triggerEventRepository.deleteByTriggerId(trigger.getId());
                    });
            this.webRequestRepository.deleteByProjectId(project.getId());
        });
    }

    /**
     * 获取accessToken
     *
     * @return
     */
    public String getAccessToken() {
        return this.accessTokenRepository.get();
    }

    /**
     * 获取user
     *
     * @param username
     * @return
     */
    public User getUserByUserName(String username) {
        return this.userRepository.getByUsername(username);
    }
}
