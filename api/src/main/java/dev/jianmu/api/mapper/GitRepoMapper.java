package dev.jianmu.api.mapper;

import dev.jianmu.api.dto.GitRepoDto;
import dev.jianmu.api.vo.GitRepoVo;
import dev.jianmu.project.aggregate.GitRepo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Map;

/**
 * @class: GitRepoMapper
 * @description: GitRepoMapper
 * @author: Ethan Liu
 * @create: 2021-05-13 19:23
 **/
@Mapper
public interface GitRepoMapper {
    GitRepoMapper INSTANCE = Mappers.getMapper(GitRepoMapper.class);

    @Mapping(source = "httpsUsername", target = "httpsUsername", defaultValue = "")
    @Mapping(source = "httpsPassword", target = "httpsPassword", defaultValue = "")
    @Mapping(source = "privateKey", target = "privateKey", defaultValue = "")
    GitRepo toGitRepo(GitRepoDto gitRepoDto);

    @Mapping(source = "files", target = "files")
    GitRepoVo toGitRepoVo(GitRepo gitRepo, Map<String, Boolean> files);
}
