package dev.jianmu.infrastructure.typehandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import dev.jianmu.task.aggregate.spec.ContainerSpec;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;

/**
 * @class: ContainerSpecTypeHandler
 * @description: ContainerSpecTypeHandler
 * @author: Ethan Liu
 * @create: 2021-06-21 08:44
 **/
public class ContainerSpecTypeHandler extends BaseTypeHandler<ContainerSpec> {
    private final ObjectMapper objectMapper;

    public ContainerSpecTypeHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
    }

    private ContainerSpec toContainerSpec(Blob blob) {
        try {
            ContainerSpec containerSpec = this.objectMapper.readValue(blob.getBytes(1, (int) blob.length()), ContainerSpec.class);
            return containerSpec;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ContainerSpec spec, JdbcType jdbcType) throws SQLException {
        try {
            String specString = this.objectMapper.writeValueAsString(spec);
            Blob blob = new SerialBlob(specString.getBytes(StandardCharsets.UTF_8));
            ps.setBlob(i, blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ContainerSpec getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toContainerSpec(rs.getBlob(columnName));
    }

    @Override
    public ContainerSpec getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toContainerSpec(rs.getBlob(columnIndex));
    }

    @Override
    public ContainerSpec getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toContainerSpec(cs.getBlob(columnIndex));
    }
}
