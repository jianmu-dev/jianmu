package dev.jianmu.infrastructure.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.jianmu.git.repo.aggregate.Flow;
import dev.jianmu.infrastructure.jackson2.UnmodifiableListDeserializer;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;

/**
 * @author Daihw
 * @class BranchTypeHandler
 * @description BranchTypeHandler
 * @create 2022/7/5 10:04 上午
 */
public class FlowListTypeHandler extends BaseTypeHandler<List<Flow>> {
    private final ObjectMapper objectMapper;

    public FlowListTypeHandler() {
        this.objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        Class type1 = List.of(new Flow()).getClass();
        Class type2 = List.of().getClass();
        module.addDeserializer(type1, new UnmodifiableListDeserializer());
        module.addDeserializer(type2, new UnmodifiableListDeserializer());
        this.objectMapper.registerModule(module);
    }

    private List<Flow> toFlows(Blob blob) {
        var javaType = this.objectMapper.getTypeFactory().constructCollectionType(List.class, Flow.class);
        try {
            return this.objectMapper.readValue(blob.getBytes(1, (int) blob.length()), javaType);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Flow> flows, JdbcType jdbcType) throws SQLException {
        try {
            String flowString = this.objectMapper.writerFor(new TypeReference<List<Flow>>() {
            }).writeValueAsString(flows);
            Blob blob = new SerialBlob(flowString.getBytes(StandardCharsets.UTF_8));
            ps.setBlob(i, blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Flow> getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        return this.toFlows(resultSet.getBlob(columnName));
    }

    @Override
    public List<Flow> getNullableResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return this.toFlows(resultSet.getBlob(columnIndex));
    }

    @Override
    public List<Flow> getNullableResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        return this.toFlows(callableStatement.getBlob(columnIndex));
    }
}
