package dev.jianmu.application.service;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.infrastructure.jgit.JgitService;
import dev.jianmu.infrastructure.mybatis.project.ProjectRepositoryImpl;
import dev.jianmu.project.aggregate.Credential;
import dev.jianmu.project.aggregate.GitRepo;
import dev.jianmu.project.repository.GitRepoRepository;
import dev.jianmu.secret.repository.KVPairRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @class: GitApplication
 * @description: GitApplication
 * @author: Ethan Liu
 * @create: 2021-05-14 11:19
 **/
@Service
public class GitApplication {
    private static final Logger logger = LoggerFactory.getLogger(GitApplication.class);

    private final ProjectRepositoryImpl projectRepository;
    private final GitRepoRepository gitRepoRepository;
    private final KVPairRepository kvPairRepository;
    private final ApplicationEventPublisher publisher;
    private final JgitService jgitService;

    public GitApplication(
            ProjectRepositoryImpl projectRepository,
            GitRepoRepository gitRepoRepository,
            KVPairRepository kvPairRepository,
            ApplicationEventPublisher publisher,
            JgitService jgitService
    ) {
        this.projectRepository = projectRepository;
        this.gitRepoRepository = gitRepoRepository;
        this.kvPairRepository = kvPairRepository;
        this.publisher = publisher;
        this.jgitService = jgitService;
    }

    public Map<String, Boolean> listFiles(String dir) {
        return jgitService.listFiles(dir);
    }

    public void syncGitRepo(String projectId) {
        var project = this.projectRepository.findById(projectId)
                .orElseThrow(() -> new DataNotFoundException("未找到该项目"));
        var gitRepo = this.gitRepoRepository.findById(project.getGitRepoId())
                .orElseThrow(() -> new DataNotFoundException("未找到Git仓库配置"));
        this.cloneGitRepo(gitRepo);
        this.publisher.publishEvent(projectId);
    }

    public void cloneGitRepo(GitRepo gitRepo) {
        var credential = gitRepo.getCredential();
        if (null == credential.getType()) {
            this.jgitService.cloneRepo(gitRepo);
            return;
        }
        if (credential.getType().equals(Credential.Type.SSH)) {
            var key = this.kvPairRepository.findByNamespaceNameAndKey(credential.getNamespace(), credential.getPrivateKey())
                    .orElseThrow(() -> new DataNotFoundException("未找到密钥"));
            this.jgitService.cloneRepoWithSshKey(gitRepo, key.getKey());
            return;
        }
        if (credential.getType().equals(Credential.Type.HTTPS)) {
            var user = this.kvPairRepository.findByNamespaceNameAndKey(credential.getNamespace(), credential.getUserKey())
                    .orElseThrow(() -> new DataNotFoundException("未找到用户名"));
            var pass = this.kvPairRepository.findByNamespaceNameAndKey(credential.getNamespace(), credential.getPassKey())
                    .orElseThrow(() -> new DataNotFoundException("未找到密码"));
            this.jgitService.cloneRepoWithUserAndPass(gitRepo, user.getValue(), pass.getValue());
        }
    }
}
