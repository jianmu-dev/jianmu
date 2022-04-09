package dev.jianmu.api.mapper;

import dev.jianmu.project.query.ProjectVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @author Daihw
 * @class ProjectVoMapper
 * @description ProjectVoMapper
 * @create 2022/3/30 9:53 上午
 */
@Mapper
public interface ProjectVoMapper {
    ProjectVoMapper INSTANCE = Mappers.getMapper(ProjectVoMapper.class);

    @Mapping(target = "source", source = "dslSource")
    @Mapping(target = "name", source = "workflowName")
    @Mapping(target = "description", source = "workflowDescription")
    @Mapping(target = "status", expression = "java(\"INIT\")")
    dev.jianmu.api.vo.ProjectVo toProjectVo(ProjectVo projectVo);
}
