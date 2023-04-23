package dev.jianmu.api.mapper;

import dev.jianmu.api.vo.GlobalParameterVo;
import dev.jianmu.workflow.aggregate.definition.GlobalParameter;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author Daihw
 * @class GlobalParameterMapper
 * @description GlobalParameterMapper
 * @create 2022/7/19 2:54 下午
 */
@Mapper
public interface GlobalParameterMapper {
    GlobalParameterMapper INSTANCE = Mappers.getMapper(GlobalParameterMapper.class);

    GlobalParameterVo toGlobalParameterVo(GlobalParameter globalParameter);
}
