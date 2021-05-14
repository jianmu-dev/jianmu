package dev.jianmu.project.repository;

import dev.jianmu.project.aggregate.GitRepo;

import java.util.Optional;

/**
 * @class: GitRepoRepository
 * @description: GitRepoRepository
 * @author: Ethan Liu
 * @create: 2021-05-14 14:54
 **/
public interface GitRepoRepository {
    void add(GitRepo gitRepo);

    Optional<GitRepo> findById(String id);
}
