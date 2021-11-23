package dev.jianmu.infrastructure.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.jianmu.infrastructure.jackson2.UnmodifiableSetDeserializer;
import dev.jianmu.workflow.aggregate.definition.Node;
import dev.jianmu.workflow.aggregate.definition.Start;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Set;

/**
 * @class NodeSetTypeHandler
 * @description 自定义类型(Set<Node>)转换器
 * @author Ethan Liu
 * @create 2021-03-21 12:48
*/
public class NodeSetTypeHandler extends BaseTypeHandler<Set<Node>> {
    private ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    public NodeSetTypeHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        SimpleModule module = new SimpleModule();
        Class type1 = Set.of(Start.Builder.aStart().build()).getClass();
        Class type2 = Set.of().getClass();
        module.addDeserializer(type1, new UnmodifiableSetDeserializer());
        module.addDeserializer(type2, new UnmodifiableSetDeserializer());
        this.objectMapper.registerModule(module);
    }

    private Set<Node> toNodeSet(Blob blob) {
        JavaType javaType = this.objectMapper.getTypeFactory().constructCollectionType(Set.class, Node.class);
        try {
            Set<Node> nodes = this.objectMapper.readValue(blob.getBytes(1, (int) blob.length()), javaType);
            return nodes;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Set<Node> parameter, JdbcType jdbcType) throws SQLException {
        try {
            String nodes = this.objectMapper.writerFor(new TypeReference<Set<Node>>() {}).writeValueAsString(parameter);
            Blob blob = new SerialBlob(nodes.getBytes(StandardCharsets.UTF_8));
            ps.setBlob(i, blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<Node> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toNodeSet(rs.getBlob(columnName));
    }

    @Override
    public Set<Node> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toNodeSet(rs.getBlob(columnIndex));
    }

    @Override
    public Set<Node> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toNodeSet(cs.getBlob(columnIndex));
    }
}
