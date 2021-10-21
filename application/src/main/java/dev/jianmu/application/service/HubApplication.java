package dev.jianmu.application.service;

import com.github.pagehelper.PageInfo;
import dev.jianmu.application.dsl.NodeDsl;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.query.NodeDef;
import dev.jianmu.hub.intergration.aggregate.NodeDefinition;
import dev.jianmu.hub.intergration.aggregate.NodeDefinitionVersion;
import dev.jianmu.hub.intergration.aggregate.NodeParameter;
import dev.jianmu.hub.intergration.event.NodeDeletedEvent;
import dev.jianmu.hub.intergration.event.NodeUpdatedEvent;
import dev.jianmu.hub.intergration.repository.NodeDefinitionVersionRepository;
import dev.jianmu.infrastructure.client.RegistryClient;
import dev.jianmu.infrastructure.mybatis.hub.NodeDefinitionRepositoryImpl;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.repository.ParameterRepository;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final NodeDefinitionRepositoryImpl nodeDefinitionRepository;
    private final NodeDefinitionVersionRepository nodeDefinitionVersionRepository;
    private final ParameterRepository parameterRepository;
    private final RegistryClient registryClient;
    private final ApplicationEventPublisher publisher;

    public HubApplication(
            NodeDefinitionRepositoryImpl nodeDefinitionRepository,
            NodeDefinitionVersionRepository nodeDefinitionVersionRepository,
            ParameterRepository parameterRepository,
            RegistryClient registryClient,
            ApplicationEventPublisher publisher
    ) {
        this.nodeDefinitionRepository = nodeDefinitionRepository;
        this.nodeDefinitionVersionRepository = nodeDefinitionVersionRepository;
        this.parameterRepository = parameterRepository;
        this.registryClient = registryClient;
        this.publisher = publisher;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void addNode(String name, String description, String dsl) {
        var nodeDsl = NodeDsl.parseDsl(dsl);
        var def = NodeDefinition.Builder.aNodeDefinition()
                .id("local/" + nodeDsl.getRef())
                .name(name)
                .description(description)
                .ref(nodeDsl.getRef())
                .ownerName("本地")
                .ownerType("LOCAL")
                .ownerRef("local")
                .creatorName("local")
                .creatorRef("local")
                .type(NodeDefinition.Type.DOCKER)
                .build();

        List<Parameter> parameters = new ArrayList<>();
        var inputParameters = nodeDsl.getInputParameters().stream().map(parameter -> {
            var p = Parameter.Type.getTypeByName(parameter.getType()).newParameter(parameter.getValue());
            parameters.add(p);
            return NodeParameter.Builder.aNodeParameter()
                    .name(parameter.getName())
                    .description(parameter.getDescription())
                    .ref(parameter.getRef())
                    .type(parameter.getType())
                    .parameterId(p.getId())
                    .value(parameter.getValue())
                    .build();
        }).collect(Collectors.toSet());

        var outputParameters = nodeDsl.getOutputParameters().stream().map(parameter -> {
            var p = Parameter.Type.getTypeByName(parameter.getType()).newParameter(parameter.getValue());
            parameters.add(p);
            return NodeParameter.Builder.aNodeParameter()
                    .name(parameter.getName())
                    .description(parameter.getDescription())
                    .ref(parameter.getRef())
                    .type(parameter.getType())
                    .parameterId(p.getId())
                    .value(parameter.getValue())
                    .build();
        }).collect(Collectors.toSet());

        this.parameterRepository.addAll(parameters);

        var version = NodeDefinitionVersion.Builder.aNodeDefinitionVersion()
                .id("local/" + nodeDsl.getRef() + ":" + nodeDsl.getVersion())
                .creatorName("local")
                .creatorRef("local")
                .ownerRef("local")
                .ref(nodeDsl.getRef())
                .version(nodeDsl.getVersion())
                .resultFile(nodeDsl.getResultFile())
                .inputParameters(inputParameters)
                .outputParameters(outputParameters)
                .spec(nodeDsl.getSpecString())
                .build();
        this.nodeDefinitionRepository.saveOrUpdate(def);
        this.nodeDefinitionVersionRepository.saveOrUpdate(version);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void syncNode(String ownerRef, String ref) {
        var node = this.downloadNodeDef(ownerRef + "/" + ref);
        var versions = this.nodeDefinitionVersionRepository.findByOwnerRefAndRef(ownerRef, ref);
        versions.forEach(nodeDefinitionVersion -> {
            var version = this.downloadNodeDefVersion(ownerRef, ref, nodeDefinitionVersion.getVersion());
            this.nodeDefinitionVersionRepository.saveOrUpdate(version);
        });
        this.nodeDefinitionRepository.saveOrUpdate(node);
        var events = versions.stream()
                .map(nodeDefinitionVersion -> NodeUpdatedEvent.Builder.aNodeUpdatedEvent()
                        .ref(nodeDefinitionVersion.getRef())
                        .ownerRef(nodeDefinitionVersion.getOwnerRef())
                        .version(nodeDefinitionVersion.getVersion())
                        .spec(nodeDefinitionVersion.getSpec())
                        .build()
                )
                .collect(Collectors.toList());
        events.forEach(this.publisher::publishEvent);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteNode(String ownerRef, String ref) {
        var versions = this.nodeDefinitionVersionRepository.findByOwnerRefAndRef(ownerRef, ref);
        this.nodeDefinitionRepository.deleteById(ownerRef + "/" + ref);
        this.nodeDefinitionVersionRepository.deleteByOwnerRefAndRef(ownerRef, ref);
        var events = versions.stream()
                .map(nodeDefinitionVersion -> NodeDeletedEvent.Builder.aNodeDeletedEvent()
                        .ref(nodeDefinitionVersion.getRef())
                        .ownerRef(nodeDefinitionVersion.getOwnerRef())
                        .version(nodeDefinitionVersion.getVersion())
                        .spec(nodeDefinitionVersion.getSpec())
                        .build()
                )
                .collect(Collectors.toList());
        events.forEach(this.publisher::publishEvent);
    }

    public PageInfo<NodeDefinition> findPage(int pageNum, int pageSize) {
        return this.nodeDefinitionRepository.findPage(pageNum, pageSize);
    }

    public List<NodeDefinitionVersion> findByOwnerRefAndRef(String ownerRef, String ref) {
        return this.nodeDefinitionVersionRepository.findByOwnerRefAndRef(ownerRef, ref);
    }

    private String getOwnerRef(String type) {
        var ref = type.split(":")[0];
        var strings = ref.split("/");
        if (strings.length == 1) {
            return "_";
        }
        return strings[0];
    }

    private String getRef(String type) {
        var ref = type.split(":")[0];
        var strings = ref.split("/");
        if (strings.length == 1) {
            return ref;
        }
        return strings[1];
    }

    private String getVersion(String type) {
        return type.split(":")[1];
    }

    private NodeDefinition downloadNodeDef(String type) {
        var defDto = this.registryClient.findByRef(getOwnerRef(type) + "/" + getRef(type))
                .orElseThrow(() -> new DataNotFoundException("未找到节点定义: " + type));
        return NodeDefinition.Builder.aNodeDefinition()
                .id(defDto.getOwnerRef() + "/" + defDto.getRef())
                .icon(defDto.getIcon())
                .name(defDto.getName())
                .ownerName(defDto.getOwnerName())
                .ownerType(defDto.getOwnerType())
                .ownerRef(defDto.getOwnerRef())
                .creatorName(defDto.getCreatorName())
                .creatorRef(defDto.getCreatorRef())
                .type(defDto.getType())
                .description(defDto.getDescription())
                .ref(defDto.getRef())
                .sourceLink(defDto.getSourceLink())
                .documentLink(defDto.getDocumentLink())
                .build();
    }

    private NodeDefinitionVersion downloadNodeDefVersion(String ownerRef, String ref, String version) {
        var dto = this.registryClient.findByRefAndVersion(ownerRef + "/" + ref, version)
                .orElseThrow(() -> new DataNotFoundException("未找到节点定义版本: " + ownerRef + "/" + ref + ":" + version));
        List<Parameter> parameters = new ArrayList<>();
        var inputParameters = dto.getInputParameters().stream().map(parameter -> {
            var p = Parameter.Type.getTypeByName(parameter.getType()).newParameter(parameter.getValue());
            parameters.add(p);
            return NodeParameter.Builder.aNodeParameter()
                    .name(parameter.getName())
                    .description(parameter.getDescription())
                    .ref(parameter.getRef())
                    .type(parameter.getType())
                    .parameterId(p.getId())
                    .value(parameter.getValue())
                    .build();
        }).collect(Collectors.toSet());

        var outputParameters = dto.getOutputParameters().stream().map(parameter -> {
            var p = Parameter.Type.getTypeByName(parameter.getType()).newParameter(parameter.getValue());
            parameters.add(p);
            return NodeParameter.Builder.aNodeParameter()
                    .name(parameter.getName())
                    .description(parameter.getDescription())
                    .ref(parameter.getRef())
                    .type(parameter.getType())
                    .parameterId(p.getId())
                    .value(parameter.getValue())
                    .build();
        }).collect(Collectors.toSet());

        this.parameterRepository.addAll(parameters);

        return NodeDefinitionVersion.Builder.aNodeDefinitionVersion()
                .id(dto.getOwnerRef() + "/" + dto.getRef() + ":" + dto.getVersion())
                .ownerRef(dto.getOwnerRef())
                .ref(dto.getRef())
                .creatorName(dto.getCreatorName())
                .creatorRef(dto.getCreatorRef())
                .version(dto.getVersion())
                .resultFile(dto.getResultFile())
                .inputParameters(inputParameters)
                .outputParameters(outputParameters)
                .spec(dto.getSpec())
                .build();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public NodeDef getByType(String type) {
        var node = this.nodeDefinitionRepository.findById(getOwnerRef(type) + "/" + getRef(type))
                .orElseGet(() -> this.downloadNodeDef(type));
        var version =
                this.nodeDefinitionVersionRepository.findByOwnerRefAndRefAndVersion(getOwnerRef(type), getRef(type), getVersion(type))
                        .orElseGet(() -> this.downloadNodeDefVersion(getOwnerRef(type), getRef(type), getVersion(type)));

        var nodeDef = NodeDef.builder()
                .name(node.getName())
                .description(node.getDescription())
                .icon(node.getIcon())
                .ownerName(node.getOwnerName())
                .ownerType(node.getOwnerType())
                .ownerRef(node.getOwnerRef())
                .creatorName(node.getCreatorName())
                .creatorRef(node.getCreatorRef())
                .sourceLink(node.getSourceLink())
                .documentLink(node.getDocumentLink())
                .type(type)
                .workerType(node.getType().name())
                .resultFile(version.getResultFile())
                .inputParameters(version.getInputParameters())
                .outputParameters(version.getOutputParameters())
                .spec(version.getSpec())
                .build();

        this.nodeDefinitionRepository.saveOrUpdate(node);
        this.nodeDefinitionVersionRepository.saveOrUpdate(version);
        return nodeDef;
    }

    public List<NodeDef> getByTypes(Set<String> types) {
        return types.stream().map(this::getByType)
                .collect(Collectors.toList());
    }

    public NodeDef findByType(String type) {
        var node = this.nodeDefinitionRepository.findById(getOwnerRef(type) + "/" + getRef(type))
                .orElseThrow(() -> new DataNotFoundException("未找到节点定义"));
        var version =
                this.nodeDefinitionVersionRepository.findByOwnerRefAndRefAndVersion(getOwnerRef(type), getRef(type), getVersion(type))
                        .orElseThrow(() -> new DataNotFoundException("未找到节点定义版本"));

        return NodeDef.builder()
                .name(node.getName())
                .description(node.getDescription())
                .icon(node.getIcon())
                .ownerName(node.getOwnerName())
                .ownerType(node.getOwnerType())
                .ownerRef(node.getOwnerRef())
                .creatorName(node.getCreatorName())
                .creatorRef(node.getCreatorRef())
                .sourceLink(node.getSourceLink())
                .documentLink(node.getDocumentLink())
                .type(type)
                .workerType(node.getType().name())
                .resultFile(version.getResultFile())
                .inputParameters(version.getInputParameters())
                .outputParameters(version.getOutputParameters())
                .spec(version.getSpec())
                .build();
    }

    public List<NodeDef> findByTypes(Set<String> types) {
        return types.stream().map(this::findByType)
                .collect(Collectors.toList());
    }
}
