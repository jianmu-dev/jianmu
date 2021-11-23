package dev.jianmu.infrastructure.typehandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import dev.jianmu.task.aggregate.NodeInfo;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;

/**
 * @class NodeInfoTypeHandler
 * @description NodeInfoTypeHandler
 * @author Ethan Liu
 * @create 2021-09-19 13:27
*/
public class NodeInfoTypeHandler extends BaseTypeHandler<NodeInfo> {
    private final ObjectMapper objectMapper;

    public NodeInfoTypeHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
    }

    private NodeInfo toNodeInfo(Blob blob) {
        try {
            NodeInfo nodeInfo = this.objectMapper.readValue(blob.getBytes(1, (int) blob.length()), NodeInfo.class);
            return nodeInfo;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, NodeInfo nodeInfo, JdbcType jdbcType) throws SQLException {
        try {
            String nodeInfoString = this.objectMapper.writeValueAsString(nodeInfo);
            Blob blob = new SerialBlob(nodeInfoString.getBytes(StandardCharsets.UTF_8));
            ps.setBlob(i, blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public NodeInfo getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toNodeInfo(rs.getBlob(columnName));
    }

    @Override
    public NodeInfo getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toNodeInfo(rs.getBlob(columnIndex));
    }

    @Override
    public NodeInfo getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toNodeInfo(cs.getBlob(columnIndex));
    }
}
