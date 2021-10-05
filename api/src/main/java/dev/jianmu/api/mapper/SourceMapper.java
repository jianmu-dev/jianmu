package dev.jianmu.api.mapper;

import dev.jianmu.api.dto.SourceDto;
import dev.jianmu.eventbridge.aggregate.Source;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @class: SourceMapper
 * @description: SourceMapper
 * @author: Ethan Liu
 * @create: 2021-10-05 13:03
 **/
@Mapper
public interface SourceMapper {
    SourceMapper INSTANCE = Mappers.getMapper(SourceMapper.class);

    SourceDto toSourceDto(Source source);

    Source toSource(SourceDto dto);
}
