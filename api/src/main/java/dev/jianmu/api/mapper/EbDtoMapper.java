package dev.jianmu.api.mapper;

import dev.jianmu.api.dto.EbDto;
import dev.jianmu.api.dto.TransformerDto;
import dev.jianmu.eventbridge.aggregate.Bridge;
import dev.jianmu.eventbridge.aggregate.Source;
import dev.jianmu.eventbridge.aggregate.Target;
import dev.jianmu.eventbridge.aggregate.Transformer;
import dev.jianmu.infrastructure.eventbridge.BodyTransformer;
import dev.jianmu.infrastructure.eventbridge.HeaderTransformer;
import dev.jianmu.infrastructure.eventbridge.QueryTransformer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @class: EbDtoMapper
 * @description: EbDtoMapper
 * @author: Ethan Liu
 * @create: 2021-09-28 09:04
 **/
@Mapper
public interface EbDtoMapper {
    EbDtoMapper INSTANCE = Mappers.getMapper(EbDtoMapper.class);

    EbDto toEbDto(Bridge bridge, Source source, List<Target> targets);

    List<TransformerDto> toTransformerDtos(List<Transformer> transformers);

    default TransformerDto toTransformerDto(Transformer transformer) {
        if (transformer instanceof BodyTransformer) {
            return TransformerDto.builder()
                    .type(TransformerDto.Type.BODY)
                    .variableName(transformer.getVariableName())
                    .variableType(transformer.getVariableType())
                    .expression(transformer.getExpression())
                    .build();
        }
        if (transformer instanceof HeaderTransformer) {
            return TransformerDto.builder()
                    .type(TransformerDto.Type.HEADER)
                    .variableName(transformer.getVariableName())
                    .variableType(transformer.getVariableType())
                    .expression(transformer.getExpression())
                    .build();
        }
        if (transformer instanceof QueryTransformer) {
            return TransformerDto.builder()
                    .type(TransformerDto.Type.QUERY)
                    .variableName(transformer.getVariableName())
                    .variableType(transformer.getVariableType())
                    .expression(transformer.getExpression())
                    .build();
        }
        throw new RuntimeException("错误的转换器类型");
    }
}
