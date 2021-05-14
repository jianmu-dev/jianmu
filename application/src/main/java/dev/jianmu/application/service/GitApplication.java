package dev.jianmu.application.service;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.infrastructure.jgit.JgitService;
import dev.jianmu.project.aggregate.GitRepo;
import dev.jianmu.secret.repository.KVPairRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final KVPairRepository kvPairRepository;
    private final JgitService jgitService;

    public GitApplication(KVPairRepository kvPairRepository, JgitService jgitService) {
        this.kvPairRepository = kvPairRepository;
        this.jgitService = jgitService;
    }

    public Map<String, Boolean> cloneGitRepo(GitRepo gitRepo) {
        if (gitRepo.getType().equals(GitRepo.Type.SSH)) {
            if (gitRepo.getPrivateKey().isBlank()) {
                throw new IllegalArgumentException("key参数为空");
            }
            String[] strings = gitRepo.getPrivateKey().split("\\.");
            if (strings.length != 2) {
                throw new IllegalArgumentException("key参数不合法");
            }
            var key = this.kvPairRepository.findByNamespaceNameAndKey(strings[0], strings[1])
                    .orElseThrow(() -> new DataNotFoundException("未找到密钥"));
            gitRepo.setPrivateKey(key.getValue());
        } else {
            if (gitRepo.getHttpsUsername().isBlank()) {
                throw new IllegalArgumentException("username参数为空");
            }
            if (gitRepo.getHttpsPassword().isBlank()) {
                throw new IllegalArgumentException("password参数为空");
            }
            var username = gitRepo.getHttpsUsername().split("\\.");
            if (username.length != 2) {
                throw new IllegalArgumentException("username参数不合法");
            }
            var password = gitRepo.getHttpsPassword().split("\\.");
            if (password.length != 2) {
                throw new IllegalArgumentException("password参数不合法");
            }
            var user = this.kvPairRepository.findByNamespaceNameAndKey(username[0], username[1])
                    .orElseThrow(() -> new DataNotFoundException("未找到密钥"));
            gitRepo.setHttpsUsername(user.getValue());
            var pass = this.kvPairRepository.findByNamespaceNameAndKey(password[0], password[1])
                    .orElseThrow(() -> new DataNotFoundException("未找到密钥"));
            gitRepo.setHttpsPassword(pass.getValue());
        }
        return this.jgitService.cloneRepo(gitRepo);
    }
}
