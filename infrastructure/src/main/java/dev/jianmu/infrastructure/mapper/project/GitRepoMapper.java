package dev.jianmu.infrastructure.mapper.project;

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
    @Insert("insert into git_repo(id, uri, type, https_username, https_password, private_key, branch, is_clone_all_branches, dsl_path) " +
            "values(#{id}, #{uri}, #{type}, #{httpsUsername}, #{httpsPassword}, #{privateKey}, #{branch}, #{isCloneAllBranches}, #{dslPath})")
    void add(GitRepo gitRepo);

    @Select("select * from git_repo where id = #{id}")
    @Result(column = "https_username", property = "httpsUsername")
    @Result(column = "https_password", property = "httpsPassword")
    @Result(column = "private_key", property = "privateKey")
    @Result(column = "is_clone_all_branches", property = "isCloneAllBranches")
    @Result(column = "dsl_path", property = "dslPath")
    Optional<GitRepo> findById(String id);
}
