package dev.jianmu.infrastructure.typehandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import dev.jianmu.project.aggregate.Credential;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;

/**
 * @class: CredentialTypeHandler
 * @description: CredentialTypeHandler
 * @author: Ethan Liu
 * @create: 2021-05-15 17:49
 **/
public class CredentialTypeHandler extends BaseTypeHandler<Credential> {
    private final ObjectMapper objectMapper;

    public CredentialTypeHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
    }

    private Credential toCredential(Blob blob) {
        try {
            Credential credential = this.objectMapper.readValue(blob.getBytes(1, (int) blob.length()), Credential.class);
            return credential;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Credential credential, JdbcType jdbcType) throws SQLException {
        try {
            String credentialString = this.objectMapper.writeValueAsString(credential);
            Blob blob = new SerialBlob(credentialString.getBytes(StandardCharsets.UTF_8));
            ps.setBlob(i, blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Credential getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toCredential(rs.getBlob(columnName));
    }

    @Override
    public Credential getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toCredential(rs.getBlob(columnIndex));
    }

    @Override
    public Credential getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toCredential(cs.getBlob(columnIndex));
    }
}
