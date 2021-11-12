package dev.jianmu.infrastructure.typehandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import dev.jianmu.trigger.aggregate.Webhook;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;

/**
 * @class: WebhookTypeHandler
 * @description: WebhookTypeHandler
 * @author: Ethan Liu
 * @create: 2021-11-10 22:22
 */
public class WebhookTypeHandler extends BaseTypeHandler<Webhook> {
    private final ObjectMapper objectMapper;

    public WebhookTypeHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
    }

    private Webhook toWebhook(Blob blob) {
        try {
            if (blob == null) {
                return null;
            }
            Webhook webhook = this.objectMapper.readValue(blob.getBytes(1, (int) blob.length()), Webhook.class);
            return webhook;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Webhook webhook, JdbcType jdbcType) throws SQLException {
        try {
            String webhookString = this.objectMapper.writeValueAsString(webhook);
            Blob blob = new SerialBlob(webhookString.getBytes(StandardCharsets.UTF_8));
            ps.setBlob(i, blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Webhook getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toWebhook(rs.getBlob(columnName));
    }

    @Override
    public Webhook getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toWebhook(rs.getBlob(columnIndex));
    }

    @Override
    public Webhook getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toWebhook(cs.getBlob(columnIndex));
    }
}
