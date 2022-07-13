package dev.jianmu.infrastructure.typehandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.jianmu.git.repo.aggregate.Branch;
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
public class BranchListTypeHandler extends BaseTypeHandler<List<Branch>> {
    private final ObjectMapper objectMapper;

    public BranchListTypeHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        SimpleModule module = new SimpleModule();
        Class type1 = List.of(new Branch()).getClass();
        Class type2 = List.of().getClass();
        module.addDeserializer(type1, new UnmodifiableListDeserializer());
        module.addDeserializer(type2, new UnmodifiableListDeserializer());
        this.objectMapper.registerModule(module);
    }

    private List<Branch> toBranches(Blob blob) {
        var javaType = this.objectMapper.getTypeFactory().constructCollectionType(List.class, Branch.class);
        try {
            return this.objectMapper.readValue(blob.getBytes(1, (int) blob.length()), javaType);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Branch> branches, JdbcType jdbcType) throws SQLException {
        try {
            String branchString = this.objectMapper.writerFor(new TypeReference<List<Branch>>() {
            }).writeValueAsString(branches);
            Blob blob = new SerialBlob(branchString.getBytes(StandardCharsets.UTF_8));
            ps.setBlob(i, blob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Branch> getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        return this.toBranches(resultSet.getBlob(columnName));
    }

    @Override
    public List<Branch> getNullableResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return this.toBranches(resultSet.getBlob(columnIndex));
    }

    @Override
    public List<Branch> getNullableResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        return this.toBranches(callableStatement.getBlob(columnIndex));
    }
}
