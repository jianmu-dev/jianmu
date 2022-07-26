package dev.jianmu.git.repo.repository;

import dev.jianmu.git.repo.aggregate.GitRepo;

import java.util.Optional;

/**
 * @class GitRepoRepository
 * @description GitRepoRepository
 * @author Daihw
 * @create 2022/7/5 9:52 上午
 */
public interface GitRepoRepository {
    void saveOrUpdate(GitRepo gitRepo);

    Optional<GitRepo> findById(String id);

    Optional<GitRepo> findByRefAndOwner(String ref, String owner);
}
