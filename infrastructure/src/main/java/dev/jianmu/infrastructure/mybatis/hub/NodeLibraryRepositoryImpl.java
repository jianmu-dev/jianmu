package dev.jianmu.infrastructure.mybatis.hub;

import dev.jianmu.hub.intergration.aggregate.NodeLibrary;
import dev.jianmu.hub.intergration.repository.NodeLibraryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @class: NodeLibraryRepositoryImpl
 * @description: NodeLibraryRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-09-15 13:54
 **/
@Repository
public class NodeLibraryRepositoryImpl implements NodeLibraryRepository {
    @Override
    public List<NodeLibrary> findAll() {
        var library = new NodeLibrary("建木官方节点库", "https://hub.jianmu.dev/hub");
        return List.of(library);
    }
}
