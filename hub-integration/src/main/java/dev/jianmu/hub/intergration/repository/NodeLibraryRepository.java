package dev.jianmu.hub.intergration.repository;

import dev.jianmu.hub.intergration.aggregate.NodeLibrary;

import java.util.List;

/**
 * @class: NodeLibraryRepository
 * @description: NodeLibraryRepository
 * @author: Ethan Liu
 * @create: 2021-09-15 13:52
 **/
public interface NodeLibraryRepository {
    List<NodeLibrary> findAll();
}
