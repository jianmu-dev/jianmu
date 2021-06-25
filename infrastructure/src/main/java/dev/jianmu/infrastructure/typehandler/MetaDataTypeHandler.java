package dev.jianmu.infrastructure.typehandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import dev.jianmu.task.aggregate.MetaData;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;

/**
 * @class: MetaDataTypeHandler
 * @description: MetaDataTypeHandler
 * @author: Ethan Liu
 * @create: 2021-06-21 08:38
 **/
public class MetaDataTypeHandler extends BaseTypeHandler<MetaData> {
    private final ObjectMapper objectMapper;

    public MetaDataTypeHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
    }

    private MetaData toMetaData(Blob blob) {
        try {
            MetaData metaData = this.objectMapper.readValue(blob.getBytes(1, (int) blob.length()), MetaData.class);
            return metaData;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, MetaData metaData, JdbcType jdbcType) throws SQLException {
        try {
            String metaDataString = this.objectMapper.writeValueAsString(metaData);
            Blob blob = new SerialBlob(metaDataString.getBytes(StandardCharsets.UTF_8));
            ps.setBlob(i, blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MetaData getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toMetaData(rs.getBlob(columnName));
    }

    @Override
    public MetaData getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toMetaData(rs.getBlob(columnIndex));
    }

    @Override
    public MetaData getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toMetaData(cs.getBlob(columnIndex));
    }
}
