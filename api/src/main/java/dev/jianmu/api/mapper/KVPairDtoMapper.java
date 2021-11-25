package dev.jianmu.api.mapper;

import dev.jianmu.api.dto.KVPairDto;
import dev.jianmu.secret.aggregate.KVPair;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @class KVPairMapper
 * @description 键值对Mapper
 * @author Ethan Liu
 * @create 2021-04-20 13:05
*/
@Mapper
public interface KVPairDtoMapper {
    KVPairDtoMapper INSTANCE = Mappers.getMapper(KVPairDtoMapper.class);

    KVPair toKVPair(KVPairDto kvPairDto);
}
