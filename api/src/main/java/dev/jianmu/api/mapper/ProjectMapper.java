package dev.jianmu.api.mapper;

import dev.jianmu.api.vo.ProjectDetailVo;
import dev.jianmu.api.vo.ProjectVo;
import dev.jianmu.project.aggregate.Project;
import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;
import org.mapstruct.factory.Mappers;

/**
 * @class ProjectMapper
 * @description ProjectMapper
 * @author Ethan Liu
 * @create 2021-06-05 18:28
*/
@Mapper(imports = {AsyncTaskInstance.class})
public interface ProjectMapper {
    ProjectMapper INSTANCE = Mappers.getMapper(ProjectMapper.class);

    @Mapping(target = "source", source = "dslSource")
    @Mapping(target = "name", source = "workflowName")
    @Mapping(target = "description", source = "workflowDescription")
    @Mapping(target = "status", expression = "java(\"INIT\")")
    ProjectVo toProjectVo(Project project);

    @ValueMapping(source = "GIT", target = "GIT")
    @ValueMapping(source = "LOCAL", target = "LOCAL")
    @ValueMapping(source = MappingConstants.NULL, target = "LOCAL")
    ProjectVo.Source enumConverter(Project.DslSource dslSource);

    ProjectDetailVo toProjectDetailVo(Project project);
}
