package dev.jianmu.infrastructure.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.jianmu.node.definition.aggregate.NodeParameter;
import dev.jianmu.infrastructure.jackson2.UnmodifiableSetDeserializer;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @class: NodeParameterSetTypeHandler
 * @description: 自定义类型(Set < NodeParameter >)转换器
 * @author: Ethan Liu
 * @create: 2021-09-09 14:17
 **/
public class NodeParameterSetTypeHandler extends BaseTypeHandler<Set<NodeParameter>> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NodeParameterSetTypeHandler() {
        this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        SimpleModule module = new SimpleModule();
        Map<String, String> aMap = new HashMap<>();
        Class type1 = Set.of(NodeParameter.Builder.aNodeParameter().build()).getClass();
        Class type2 = Set.of().getClass();
        Class type3 = aMap.keySet().getClass();
        module.addDeserializer(type1, new UnmodifiableSetDeserializer());
        module.addDeserializer(type2, new UnmodifiableSetDeserializer());
        module.addDeserializer(type3, new UnmodifiableSetDeserializer());
        this.objectMapper.registerModule(module);
    }

    private Set<NodeParameter> toNodeParameterSet(Blob blob) {
        JavaType javaType = this.objectMapper.getTypeFactory().constructCollectionType(Set.class, NodeParameter.class);
        try {
            Set<NodeParameter> nodeParameters = this.objectMapper.readValue(blob.getBytes(1, (int) blob.length()), javaType);
            return nodeParameters;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Set<NodeParameter> nodeParameters, JdbcType jdbcType) throws SQLException {
        try {
            String parameters = this.objectMapper.writerFor(new TypeReference<Set<NodeParameter>>() {}).writeValueAsString(nodeParameters);
            Blob blob = new SerialBlob(parameters.getBytes(StandardCharsets.UTF_8));
            ps.setBlob(i, blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<NodeParameter> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toNodeParameterSet(rs.getBlob(columnName));
    }

    @Override
    public Set<NodeParameter> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toNodeParameterSet(rs.getBlob(columnIndex));
    }

    @Override
    public Set<NodeParameter> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toNodeParameterSet(cs.getBlob(columnIndex));
    }
}
