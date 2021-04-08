package dev.jianmu.infrastructure.jackson2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @class: UnmodifiableListDeserializer
 * @description: 不可变List反序列化器
 * @author: Ethan Liu
 * @create: 2021-03-21 16:11
 **/
public class UnmodifiableListDeserializer extends JsonDeserializer<List> {
    @Override
    public List deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode node = mapper.readTree(jsonParser);
        List<Object> result = new ArrayList<>();
        if (node != null) {
            if (node instanceof ArrayNode) {
                ArrayNode arrayNode = (ArrayNode) node;
                for (JsonNode elementNode : arrayNode) {
                    result.add(mapper.readValue(elementNode.traverse(mapper), Object.class));
                }
            }
            else {
                result.add(mapper.readValue(node.traverse(mapper), Object.class));
            }
        }
        return Collections.unmodifiableList(result);
    }
}
