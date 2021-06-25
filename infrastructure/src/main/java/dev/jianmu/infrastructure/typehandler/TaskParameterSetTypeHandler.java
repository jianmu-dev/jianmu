package dev.jianmu.infrastructure.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.jianmu.infrastructure.jackson2.UnmodifiableSetDeserializer;
import dev.jianmu.task.aggregate.TaskParameter;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Set;

/**
 * @class: TaskParameterSetTypeHandler
 * @description: TaskParameterSetTypeHandler
 * @author: Ethan Liu
 * @create: 2021-06-21 08:29
 **/
public class TaskParameterSetTypeHandler extends BaseTypeHandler<Set<TaskParameter>> {
    private ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    public TaskParameterSetTypeHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        SimpleModule module = new SimpleModule();
        Class type1 = Set.of(TaskParameter.Builder.aTaskParameter().build()).getClass();
        Class type2 = Set.of().getClass();
        module.addDeserializer(type1, new UnmodifiableSetDeserializer());
        module.addDeserializer(type2, new UnmodifiableSetDeserializer());
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
    public void setNonNullParameter(PreparedStatement ps, int i, Set<TaskParameter> parameter, JdbcType jdbcType) throws SQLException {
        try {
            String taskParameters = this.objectMapper.writerFor(new TypeReference<Set<TaskParameter>>() {}).writeValueAsString(parameter);
            Blob blob = new SerialBlob(taskParameters.getBytes(StandardCharsets.UTF_8));
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
