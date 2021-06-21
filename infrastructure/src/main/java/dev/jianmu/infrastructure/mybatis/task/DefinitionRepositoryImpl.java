package dev.jianmu.infrastructure.mybatis.task;

import dev.jianmu.infrastructure.mapper.task.DefinitionMapper;
import dev.jianmu.task.aggregate.Definition;
import dev.jianmu.task.aggregate.DockerDefinition;
import dev.jianmu.task.repository.DefinitionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @class: DefinitionRepositoryImpl
 * @description: DefinitionRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-06-20 22:19
 **/
@Repository
public class DefinitionRepositoryImpl implements DefinitionRepository {
    private final DefinitionMapper definitionMapper;

    public DefinitionRepositoryImpl(DefinitionMapper definitionMapper) {
        this.definitionMapper = definitionMapper;
    }

    @Override
    public void add(Definition definition) {
        if (definition instanceof DockerDefinition) {
            this.definitionMapper.add((DockerDefinition) definition);
        }
    }

    @Override
    public void addAll(List<Definition> definitions) {
        var dockerDefinitions = definitions.stream()
                .filter(definition -> definition instanceof DockerDefinition)
                .map(definition -> (DockerDefinition) definition)
                .collect(Collectors.toList());
        this.definitionMapper.addAll(dockerDefinitions);
    }

    @Override
    public Optional<? extends Definition> findByRefAndVersion(String ref, String version) {
        return this.definitionMapper.findByRefAndVersion(ref, version);
    }

    @Override
    public void delete(String ref, String version) {
        this.definitionMapper.delete(ref, version);
    }
}
