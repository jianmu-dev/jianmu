package dev.jianmu.infrastructure.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.jianmu.infrastructure.jackson2.UnmodifiableSetDeserializer;
import dev.jianmu.workflow.aggregate.definition.GlobalParameter;
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
 * @class: ParameterSetTypeHandler
 * @description: 自定义类型(Set < GlobalParameter >)转换器
 * @author: Ethan Liu
 * @create: 2021-04-25 21:17
 **/
public class ParameterSetTypeHandler extends BaseTypeHandler<Set<GlobalParameter>> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public ParameterSetTypeHandler() {
        this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        SimpleModule module = new SimpleModule();
        Map<String, String> aMap = new HashMap<>();
        Class type1 = Set.of(GlobalParameter.Builder.aGlobalParameter().build()).getClass();
        Class type2 = Set.of().getClass();
        Class type3 = aMap.keySet().getClass();
        module.addDeserializer(type1, new UnmodifiableSetDeserializer());
        module.addDeserializer(type2, new UnmodifiableSetDeserializer());
        module.addDeserializer(type3, new UnmodifiableSetDeserializer());
        this.objectMapper.registerModule(module);
    }

    private Set<GlobalParameter> toGlobalParameterSet(Blob blob) {
        JavaType javaType = this.objectMapper.getTypeFactory().constructCollectionType(Set.class, GlobalParameter.class);
        try {
            Set<GlobalParameter> GlobalParameters = this.objectMapper.readValue(blob.getBytes(1, (int) blob.length()), javaType);
            return GlobalParameters;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Set<GlobalParameter> GlobalParameters, JdbcType jdbcType) throws SQLException {
        try {
            String nodes = this.objectMapper.writerFor(new TypeReference<Set<GlobalParameter>>() {}).writeValueAsString(GlobalParameters);
            Blob blob = new SerialBlob(nodes.getBytes(StandardCharsets.UTF_8));
            ps.setBlob(i, blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<GlobalParameter> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toGlobalParameterSet(rs.getBlob(columnName));
    }

    @Override
    public Set<GlobalParameter> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toGlobalParameterSet(rs.getBlob(columnIndex));
    }

    @Override
    public Set<GlobalParameter> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toGlobalParameterSet(cs.getBlob(columnIndex));
    }
}
