package dev.jianmu.api.mapper;

import dev.jianmu.api.dto.TargetDto;
import dev.jianmu.api.dto.TransformerDto;
import dev.jianmu.eventbridge.aggregate.Target;
import dev.jianmu.eventbridge.aggregate.Transformer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

/**
 * @class: TargetMapper
 * @description: TargetMapper
 * @author: Ethan Liu
 * @create: 2021-09-26 15:44
 **/
@Mapper
public interface TargetMapper {
    TargetMapper INSTANCE = Mappers.getMapper(TargetMapper.class);

    Target toTarget(TargetDto dto);

    @Mapping(target = "relatedProjectName", source = "projectName")
    @Mapping(source = "target.destinationId", target = "relatedProjectId", defaultValue = "")
    TargetDto toTargetDto(Target target, String projectName);

    List<Target> toTargetList(List<TargetDto> dtos);

    Transformer map(TransformerDto dto);

    Set<Transformer> map(Set<TransformerDto> transformerDtos);

    List<TransformerDto> toTransformerDtos(List<Transformer> transformers);
}
