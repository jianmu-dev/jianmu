package dev.jianmu.infrastructure.mapper.project;

import dev.jianmu.infrastructure.typehandler.CredentialTypeHandler;
import dev.jianmu.project.aggregate.GitRepo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * @class: GitRepoMapper
 * @description: GitRepoMapper
 * @author: Ethan Liu
 * @create: 2021-05-14 19:05
 **/
public interface GitRepoMapper {
    @Insert("insert into git_repo(id, uri, type, credential, branch, is_clone_all_branches, dsl_path) " +
            "values(#{id}, #{uri}, #{type}, #{credential, jdbcType=BLOB,typeHandler=dev.jianmu.infrastructure.typehandler.CredentialTypeHandler}, #{branch}, #{isCloneAllBranches}, #{dslPath})")
    void add(GitRepo gitRepo);

    @Select("select * from git_repo where id = #{id}")
    @Result(column = "is_clone_all_branches", property = "isCloneAllBranches")
    @Result(column = "dsl_path", property = "dslPath")
    @Result(column = "credential", property = "credential", typeHandler = CredentialTypeHandler.class)
    Optional<GitRepo> findById(String id);
}
