package dev.jianmu.node.definition.repository;

import dev.jianmu.node.definition.aggregate.ShellNode;

import java.util.List;
import java.util.Optional;

/**
 * @class: ShellNodeRepository
 * @description: ShellNodeRepository
 * @author: Ethan Liu
 * @create: 2021-11-09 15:09
 */
public interface ShellNodeRepository {
    void addAll(List<ShellNode> shellNodes);

    Optional<ShellNode> findById(String id);
}
