package dev.jianmu.infrastructure.typehandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import dev.jianmu.eventbridge.aggregate.Payload;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;

/**
 * @class: PayloadTypeHandler
 * @description: PayloadTypeHandler
 * @author: Ethan Liu
 * @create: 2021-10-04 23:37
 **/
public class PayloadTypeHandler extends BaseTypeHandler<Payload> {
    private final ObjectMapper objectMapper;

    public PayloadTypeHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
    }

    private Payload toPayload(Blob blob) {
        try {
            Payload payload = this.objectMapper.readValue(blob.getBytes(1, (int) blob.length()), Payload.class);
            return payload;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Payload payload, JdbcType jdbcType) throws SQLException {
        try {
            String payloadString = this.objectMapper.writeValueAsString(payload);
            Blob blob = new SerialBlob(payloadString.getBytes(StandardCharsets.UTF_8));
            ps.setBlob(i, blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Payload getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toPayload(rs.getBlob(columnName));
    }

    @Override
    public Payload getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toPayload(rs.getBlob(columnIndex));
    }

    @Override
    public Payload getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toPayload(cs.getBlob(columnIndex));
    }
}
