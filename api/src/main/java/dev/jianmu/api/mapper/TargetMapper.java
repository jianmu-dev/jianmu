package dev.jianmu.api.mapper;

import dev.jianmu.api.dto.TargetDto;
import dev.jianmu.api.dto.TransformerDto;
import dev.jianmu.eventbridge.aggregate.Target;
import dev.jianmu.eventbridge.aggregate.Transformer;
import dev.jianmu.infrastructure.eventbridge.BodyTransformer;
import dev.jianmu.infrastructure.eventbridge.HeaderTransformer;
import dev.jianmu.infrastructure.eventbridge.QueryTransformer;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
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

    List<Target> toTargetList(List<TargetDto> dtos);

    Transformer map(TransformerDto dto);

    Set<Transformer> map(Set<TransformerDto> transformerDtos);

    @ObjectFactory
    default Transformer createTransformer(TransformerDto dto) {
        switch (dto.getType()) {
            case BODY:
                return BodyTransformer.Builder.aBodyTransformer()
                        .variableName(dto.getVariableName())
                        .variableType(dto.getVariableType())
                        .expression(dto.getExpression())
                        .build();
            case QUERY:
                return QueryTransformer.Builder.aQueryTransformer()
                        .variableName(dto.getVariableName())
                        .variableType(dto.getVariableType())
                        .expression(dto.getExpression())
                        .build();
            case HEADER:
                return HeaderTransformer.Builder.aHeaderTransformer()
                        .variableName(dto.getVariableName())
                        .variableType(dto.getVariableType())
                        .expression(dto.getExpression())
                        .build();
            default:
                throw new RuntimeException("非法Transformer类型");
        }
    }
}
