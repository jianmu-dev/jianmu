package dev.jianmu.api.mapper;

import dev.jianmu.api.dto.ProjectGroupDto;
import dev.jianmu.project.aggregate.ProjectGroup;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author Daihw
 * @class ProjectGroupMapper
 * @description 项目组Mapper
 * @create 2021/11/25 3:44 下午
 */
@Mapper
public interface ProjectGroupDtoMapper {
    ProjectGroupDtoMapper INSTANCE = Mappers.getMapper(ProjectGroupDtoMapper.class);

    ProjectGroup toProjectGroup(ProjectGroupDto projectGroupDto);
}
