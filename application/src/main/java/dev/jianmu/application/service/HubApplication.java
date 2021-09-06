package dev.jianmu.application.service;

import dev.jianmu.hub.intergration.aggregate.NodeDef;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @class: HubApplication
 * @description: HubApplication
 * @author: Ethan Liu
 * @create: 2021-09-04 10:03
 **/
@Service
public class HubApplication {
//    private final NodeDefinitionVersionRepository nodeDefinitionVersionRepository;
//
//    public HubApplication(NodeDefinitionVersionRepository nodeDefinitionVersionRepository) {
//        this.nodeDefinitionVersionRepository = nodeDefinitionVersionRepository;
//    }

    public List<NodeDef> findByTypes(Set<String> types) {
        return types.stream().map(type -> {
            String[] strings = type.split(":");
            return NodeDef.Builder.aNodeDef()
                    .name(strings[0])
                    .description("")
                    .type(type)
                    .build();
        }).collect(Collectors.toList());
    }
}
