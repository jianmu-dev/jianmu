package dev.jianmu.infrastructure.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.jianmu.git.repo.aggregate.Branch;
import dev.jianmu.infrastructure.jackson2.UnmodifiableListDeserializer;
import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookInstance;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;

/**
 * @author Daihw
 * @class CustomWebhookInstance.EventInstanceTypeHandler
 * @description CustomWebhookInstance.EventInstanceTypeHandler
 * @create 2022/7/5 10:04 上午
 */
public class WebhookEventInstanceListTypeHandler extends BaseTypeHandler<List<CustomWebhookInstance.EventInstance>> {
    private final ObjectMapper objectMapper;

    public WebhookEventInstanceListTypeHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        SimpleModule module = new SimpleModule();
        Class type1 = List.of(new CustomWebhookInstance.EventInstance()).getClass();
        Class type2 = List.of().getClass();
        module.addDeserializer(type1, new UnmodifiableListDeserializer());
        module.addDeserializer(type2, new UnmodifiableListDeserializer());
        this.objectMapper.registerModule(module);
    }

    private List<CustomWebhookInstance.EventInstance> toInstance(Blob blob) {
        var javaType = this.objectMapper.getTypeFactory().constructCollectionType(List.class, CustomWebhookInstance.EventInstance.class);
        try {
            return this.objectMapper.readValue(blob.getBytes(1, (int) blob.length()), javaType);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<CustomWebhookInstance.EventInstance> branches, JdbcType jdbcType) throws SQLException {
        try {
            String branchString = this.objectMapper.writerFor(new TypeReference<List<CustomWebhookInstance.EventInstance>>() {
            }).writeValueAsString(branches);
            Blob blob = new SerialBlob(branchString.getBytes(StandardCharsets.UTF_8));
            ps.setBlob(i, blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<CustomWebhookInstance.EventInstance> getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        return this.toInstance(resultSet.getBlob(columnName));
    }

    @Override
    public List<CustomWebhookInstance.EventInstance> getNullableResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return this.toInstance(resultSet.getBlob(columnIndex));
    }

    @Override
    public List<CustomWebhookInstance.EventInstance> getNullableResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        return this.toInstance(callableStatement.getBlob(columnIndex));
    }
}
