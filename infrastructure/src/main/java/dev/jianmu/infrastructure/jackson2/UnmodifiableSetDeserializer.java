package dev.jianmu.infrastructure.jackson2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @class: UnmodifiableSetDeserializer
 * @description: 不可变Set反序列化器
 * @author: Ethan Liu
 * @create: 2021-03-21 15:57
 **/
public class UnmodifiableSetDeserializer extends JsonDeserializer<Set> {

    @Override
    public Set deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode node = mapper.readTree(jsonParser);
        Set<Object> resultSet = new HashSet<>();
        if (node != null) {
            if (node instanceof ArrayNode) {
                ArrayNode arrayNode = (ArrayNode) node;
                Iterator<JsonNode> nodeIterator = arrayNode.iterator();
                while (nodeIterator.hasNext()) {
                    JsonNode elementNode = nodeIterator.next();
                    resultSet.add(mapper.readValue(elementNode.traverse(mapper), Object.class));
                }
            } else {
                resultSet.add(mapper.readValue(node.traverse(mapper), Object.class));
            }
        }
        return Collections.unmodifiableSet(resultSet);
    }
}
