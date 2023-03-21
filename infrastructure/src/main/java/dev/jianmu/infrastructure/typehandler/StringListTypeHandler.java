package dev.jianmu.infrastructure.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.jianmu.infrastructure.jackson2.UnmodifiableListDeserializer;
import dev.jianmu.infrastructure.jackson2.UnmodifiableSetDeserializer;
import dev.jianmu.workflow.aggregate.definition.Node;
import dev.jianmu.workflow.aggregate.definition.Start;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;
import java.util.Set;

/**
 * @class StringListTypeHandler
 * @description CacheSetTypeHandler
 * @author Daihw
 * @create 2023/3/1 2:48 下午
 */
public class StringListTypeHandler extends BaseTypeHandler<List<String>> {
    private ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    public StringListTypeHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        SimpleModule module = new SimpleModule();
        Class type1 = List.of(Start.Builder.aStart().build()).getClass();
        Class type2 = List.of().getClass();
        module.addDeserializer(type1, new UnmodifiableListDeserializer());
        module.addDeserializer(type2, new UnmodifiableListDeserializer());
        this.objectMapper.registerModule(module);
    }

    private List<String> toNodeList(Blob blob) {
        JavaType javaType = this.objectMapper.getTypeFactory().constructCollectionType(List.class, String.class);
        if (blob == null) {
            return null;
        }
        try {
            List<String> list = this.objectMapper.readValue(blob.getBytes(1, (int) blob.length()), javaType);
            return list;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        try {
            String str = this.objectMapper.writerFor(new TypeReference<List<String>>() {}).writeValueAsString(parameter);
            Blob blob = new SerialBlob(str.getBytes(StandardCharsets.UTF_8));
            ps.setBlob(i, blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toNodeList(rs.getBlob(columnName));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toNodeList(rs.getBlob(columnIndex));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toNodeList(cs.getBlob(columnIndex));
    }
}
