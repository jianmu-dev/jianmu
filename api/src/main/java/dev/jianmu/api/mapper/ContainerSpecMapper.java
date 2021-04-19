package dev.jianmu.api.mapper;

import dev.jianmu.api.dto.ContainerSpecDto;
import dev.jianmu.task.aggregate.spec.ContainerSpec;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @class: ContainerSpecMapper
 * @description: 容器规约Dto Mapper
 * @author: Ethan Liu
 * @create: 2021-04-20 06:58
 **/
@Mapper
public interface ContainerSpecMapper {
    ContainerSpecMapper INSTANCE = Mappers.getMapper(ContainerSpecMapper.class);

    ContainerSpec toContainerSpec(ContainerSpecDto containerSpecDto);
}
