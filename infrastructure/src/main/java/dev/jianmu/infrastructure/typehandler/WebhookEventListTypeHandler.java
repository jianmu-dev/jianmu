package dev.jianmu.infrastructure.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.jianmu.infrastructure.jackson2.UnmodifiableListDeserializer;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookDefinitionVersion;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;

/**
 * @author Daihw
 * @class CustomWebhookDefinitionVersion.EventTypeHandler
 * @description CustomWebhookDefinitionVersion.EventTypeHandler
 * @create 2022/7/5 10:04 上午
 */
public class WebhookEventListTypeHandler extends BaseTypeHandler<List<CustomWebhookDefinitionVersion.Event>> {
    private final ObjectMapper objectMapper;

    public WebhookEventListTypeHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        SimpleModule module = new SimpleModule();
        Class type1 = List.of(new CustomWebhookDefinitionVersion.Event()).getClass();
        Class type2 = List.of().getClass();
        module.addDeserializer(type1, new UnmodifiableListDeserializer());
        module.addDeserializer(type2, new UnmodifiableListDeserializer());
        this.objectMapper.registerModule(module);
    }

    private List<CustomWebhookDefinitionVersion.Event> toInstance(Blob blob) {
        var javaType = this.objectMapper.getTypeFactory().constructCollectionType(List.class, CustomWebhookDefinitionVersion.Event.class);
        try {
            return this.objectMapper.readValue(blob.getBytes(1, (int) blob.length()), javaType);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<CustomWebhookDefinitionVersion.Event> events, JdbcType jdbcType) throws SQLException {
        try {
            String branchString = this.objectMapper.writerFor(new TypeReference<List<CustomWebhookDefinitionVersion.Event>>() {
            }).writeValueAsString(events);
            Blob blob = new SerialBlob(branchString.getBytes(StandardCharsets.UTF_8));
            ps.setBlob(i, blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<CustomWebhookDefinitionVersion.Event> getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        return this.toInstance(resultSet.getBlob(columnName));
    }

    @Override
    public List<CustomWebhookDefinitionVersion.Event> getNullableResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return this.toInstance(resultSet.getBlob(columnIndex));
    }

    @Override
    public List<CustomWebhookDefinitionVersion.Event> getNullableResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        return this.toInstance(callableStatement.getBlob(columnIndex));
    }
}
