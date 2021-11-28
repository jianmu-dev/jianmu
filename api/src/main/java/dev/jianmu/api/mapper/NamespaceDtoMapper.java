package dev.jianmu.api.mapper;

import dev.jianmu.api.dto.NamespaceDto;
import dev.jianmu.secret.aggregate.Namespace;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @class NamespaceMapper
 * @description 命名空间Mapper
 * @author Ethan Liu
 * @create 2021-04-20 12:53
*/
@Mapper
public interface NamespaceDtoMapper {
    NamespaceDtoMapper INSTANCE = Mappers.getMapper(NamespaceDtoMapper.class);

    Namespace toNamespace(NamespaceDto namespaceDto);
}
