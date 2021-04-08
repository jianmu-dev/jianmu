package dev.jianmu.infrastructure.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.jianmu.infrastructure.jackson2.UnmodifiableListDeserializer;
import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.*;
import java.util.List;

/**
 * @class: TaskInstanceListTypeHandler
 * @description: 自定义类型(List<AsyncTaskInstance>)转换器
 * @author: Ethan Liu
 * @create: 2021-03-21 21:32
 **/
public class TaskInstanceListTypeHandler extends BaseTypeHandler<List<AsyncTaskInstance>> {
    private ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    public TaskInstanceListTypeHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        // 注册不可变List自定义反序列化器
        SimpleModule module = new SimpleModule();
        Class type1 = List.of(AsyncTaskInstance.Builder.anAsyncTaskInstance().build()).getClass();
        Class type2 = List.of().getClass();
        module.addDeserializer(type1, new UnmodifiableListDeserializer());
        module.addDeserializer(type2, new UnmodifiableListDeserializer());
        this.objectMapper.registerModule(module);
        // 注册JavaTime模块来支持LocalDataTime类型
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    private List<AsyncTaskInstance> toTaskList(Blob blob) {
        JavaType javaType = this.objectMapper.getTypeFactory().constructCollectionType(List.class, AsyncTaskInstance.class);
        try {
            List<AsyncTaskInstance> taskInstances = this.objectMapper.readValue(blob.getBytes(1, (int) blob.length()), javaType);
            return taskInstances;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<AsyncTaskInstance> parameter, JdbcType jdbcType) throws SQLException {
        try {
            String tasks = this.objectMapper.writerFor(new TypeReference<List<AsyncTaskInstance>>() {}).writeValueAsString(parameter);
            Blob blob = new SerialBlob(tasks.getBytes());
            ps.setBlob(i, blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<AsyncTaskInstance> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toTaskList(rs.getBlob(columnName));
    }

    @Override
    public List<AsyncTaskInstance> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toTaskList(rs.getBlob(columnIndex));
    }

    @Override
    public List<AsyncTaskInstance> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toTaskList(cs.getBlob(columnIndex));
    }
}
