package dev.jianmu.api.mapper;

import dev.jianmu.api.vo.TargetEventVo;
import dev.jianmu.eventbridge.aggregate.TargetEvent;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @class: TargetEventMapper
 * @description: TargetEventMapper
 * @author: Ethan Liu
 * @create: 2021-10-14 18:12
 **/
@Mapper
public interface TargetEventMapper {
    TargetEventMapper INSTANCE = Mappers.getMapper(TargetEventMapper.class);

    TargetEventVo toTargetEventVo(TargetEvent targetEvent);
}
