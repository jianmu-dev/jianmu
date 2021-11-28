package dev.jianmu.infrastructure.mybatis.node;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.infrastructure.mapper.node.ShellNodeMapper;
import dev.jianmu.node.definition.aggregate.ShellNode;
import dev.jianmu.node.definition.repository.ShellNodeRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @class ShellNodeRepositoryImpl
 * @description ShellNodeRepositoryImpl
 * @author Ethan Liu
 * @create 2021-11-09 19:17
 */
@Repository
public class ShellNodeRepositoryImpl implements ShellNodeRepository {
    private final ShellNodeMapper shellNodeMapper;
    private final ObjectMapper mapper;

    public ShellNodeRepositoryImpl(ShellNodeMapper shellNodeMapper, ObjectMapper mapper) {
        this.shellNodeMapper = shellNodeMapper;
        this.mapper = mapper;
    }

    @Override
    public void addAll(List<ShellNode> shellNodes) {
        shellNodes.forEach(shellNode -> {
            String s = null;
            try {
                s = this.mapper.writeValueAsString(shellNode);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            this.shellNodeMapper.add(shellNode.getId(), s);
        });
    }

    @Override
    public Optional<ShellNode> findById(String id) {
        return this.shellNodeMapper.findById(id)
                .map(s -> {
                    try {
                        return this.mapper.readValue(s, ShellNode.class);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
    }
}
