package dev.jianmu.infrastructure.mybatis.git_repo;

import dev.jianmu.git.repo.aggregate.GitRepo;
import dev.jianmu.git.repo.repository.GitRepoRepository;
import dev.jianmu.infrastructure.mapper.git_repo.GitRepoMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @class GitRepoRepositoryImpl
 * @description GitRepoRepositoryImpl
 * @author Daihw
 * @create 2022/7/5 9:51 上午
 */
@Repository
public class GitRepoRepositoryImpl implements GitRepoRepository {
    private final GitRepoMapper gitRepoMapper;

    public GitRepoRepositoryImpl(GitRepoMapper gitRepoMapper) {
        this.gitRepoMapper = gitRepoMapper;
    }

    @Override
    public void saveOrUpdate(GitRepo gitRepo) {
        this.gitRepoMapper.saveOrUpdate(gitRepo);
    }

    @Override
    public Optional<GitRepo> findById(String id) {
        return this.gitRepoMapper.findById(id);
    }

    @Override
    public Optional<GitRepo> findByRefAndOwner(String ref, String owner) {
        return this.gitRepoMapper.findByRefAndOwner(ref, owner);
    }
}
