package dev.jianmu.infrastructure.mybatis.project;

import dev.jianmu.infrastructure.mapper.project.GitRepoMapper;
import dev.jianmu.project.aggregate.GitRepo;
import dev.jianmu.project.repository.GitRepoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @class: GitRepoRepositoryImpl
 * @description: GitRepoRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-05-14 15:21
 **/
@Repository
public class GitRepoRepositoryImpl implements GitRepoRepository {
    private final GitRepoMapper gitRepoMapper;

    public GitRepoRepositoryImpl(GitRepoMapper gitRepoMapper) {
        this.gitRepoMapper = gitRepoMapper;
    }

    @Override
    public void add(GitRepo gitRepo) {
        this.gitRepoMapper.add(gitRepo);
    }

    @Override
    public Optional<GitRepo> findById(String id) {
        return this.gitRepoMapper.findById(id);
    }
}
