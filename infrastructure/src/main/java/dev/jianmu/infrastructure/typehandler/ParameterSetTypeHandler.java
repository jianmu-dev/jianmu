package dev.jianmu.infrastructure.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.jianmu.infrastructure.jackson2.UnmodifiableSetDeserializer;
import dev.jianmu.task.aggregate.TaskParameter;
import dev.jianmu.workflow.aggregate.definition.Node;
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
 * @description: 自定义类型(Set < TaskParameter >)转换器
 * @author: Ethan Liu
 * @create: 2021-04-25 21:17
 **/
public class ParameterSetTypeHandler extends BaseTypeHandler<Set<TaskParameter>> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public ParameterSetTypeHandler() {
        this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        SimpleModule module = new SimpleModule();
        Map<String, String> aMap = new HashMap<>();
        Class type1 = Set.of(TaskParameter.Builder.aTaskParameter().build()).getClass();
        Class type2 = Set.of().getClass();
        Class type3 = aMap.keySet().getClass();
        module.addDeserializer(type1, new UnmodifiableSetDeserializer());
        module.addDeserializer(type2, new UnmodifiableSetDeserializer());
        module.addDeserializer(type3, new UnmodifiableSetDeserializer());
        this.objectMapper.registerModule(module);
    }

    private Set<TaskParameter> toTaskParameterSet(Blob blob) {
        JavaType javaType = this.objectMapper.getTypeFactory().constructCollectionType(Set.class, TaskParameter.class);
        try {
            Set<TaskParameter> taskParameters = this.objectMapper.readValue(blob.getBytes(1, (int) blob.length()), javaType);
            return taskParameters;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Set<TaskParameter> taskParameters, JdbcType jdbcType) throws SQLException {
        try {
            String nodes = this.objectMapper.writerFor(new TypeReference<Set<Node>>() {
            }).writeValueAsString(taskParameters);
            Blob blob = new SerialBlob(nodes.getBytes(StandardCharsets.UTF_8));
            ps.setBlob(i, blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<TaskParameter> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toTaskParameterSet(rs.getBlob(columnName));
    }

    @Override
    public Set<TaskParameter> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toTaskParameterSet(rs.getBlob(columnIndex));
    }

    @Override
    public Set<TaskParameter> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toTaskParameterSet(cs.getBlob(columnIndex));
    }
}
