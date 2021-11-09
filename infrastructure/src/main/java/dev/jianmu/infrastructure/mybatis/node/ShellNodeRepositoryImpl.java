package dev.jianmu.infrastructure.mybatis.node;

import dev.jianmu.node.definition.aggregate.ShellNode;
import dev.jianmu.node.definition.repository.ShellNodeRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @class: ShellNodeRepositoryImpl
 * @description: ShellNodeRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-11-09 19:17
 */
@Repository
public class ShellNodeRepositoryImpl implements ShellNodeRepository {
    @Override
    public void addAll(List<ShellNode> shellNodes) {
        shellNodes.forEach(shellNode -> {
            System.out.println(shellNode.getId());
            System.out.println(shellNode.getImage());
        });
    }

    @Override
    public Optional<ShellNode> findById(String id) {
        return Optional.empty();
    }
}
