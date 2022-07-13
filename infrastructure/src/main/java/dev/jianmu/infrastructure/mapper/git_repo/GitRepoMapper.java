package dev.jianmu.infrastructure.mapper.git_repo;

import dev.jianmu.git.repo.aggregate.GitRepo;
import dev.jianmu.infrastructure.typehandler.BranchListTypeHandler;
import dev.jianmu.infrastructure.typehandler.FlowListTypeHandler;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * @author Daihw
 * @class GitRepoMapper
 * @description GitRepoMapper
 * @create 2022/7/5 9:55 上午
 */
public interface GitRepoMapper {
    @Insert("INSERT INTO jm_git_repo(id, branches, flows) values(#{id}, " +
            "#{branches, jdbcType=BLOB, typeHandler=dev.jianmu.infrastructure.typehandler.BranchListTypeHandler}, " +
            "#{flows, jdbcType=BLOB, typeHandler=dev.jianmu.infrastructure.typehandler.FlowListTypeHandler}) " +
            "ON DUPLICATE KEY UPDATE " +
            "branches = #{branches, jdbcType=BLOB, typeHandler=dev.jianmu.infrastructure.typehandler.BranchListTypeHandler}, " +
            "flows = #{flows, jdbcType=BLOB, typeHandler=dev.jianmu.infrastructure.typehandler.FlowListTypeHandler}")
    void saveOrUpdate(GitRepo gitRepo);

    @Select("SELECT * FROM jm_git_repo WHERE id = #{id}")
    @Result(column = "branches", property = "branches", typeHandler = BranchListTypeHandler.class)
    @Result(column = "flows", property = "flows", typeHandler = FlowListTypeHandler.class)
    Optional<GitRepo> findById(String id);
}
