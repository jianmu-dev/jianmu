package dev.jianmu.application.service;

import com.github.pagehelper.PageInfo;
import dev.jianmu.application.dsl.NodeDsl;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.query.NodeDef;
import dev.jianmu.infrastructure.client.RegistryClient;
import dev.jianmu.infrastructure.mybatis.node.NodeDefinitionRepositoryImpl;
import dev.jianmu.node.definition.aggregate.NodeDefinition;
import dev.jianmu.node.definition.aggregate.NodeDefinitionVersion;
import dev.jianmu.node.definition.aggregate.NodeParameter;
import dev.jianmu.node.definition.aggregate.ShellNode;
import dev.jianmu.node.definition.event.NodeDeletedEvent;
import dev.jianmu.node.definition.event.NodeUpdatedEvent;
import dev.jianmu.node.definition.repository.NodeDefinitionVersionRepository;
import dev.jianmu.node.definition.repository.ShellNodeRepository;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.repository.ParameterRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @class HubApplication
 * @description HubApplication
 * @author Ethan Liu
 * @create 2021-09-04 10:03
*/
@Service
public class HubApplication {
    private final NodeDefinitionRepositoryImpl nodeDefinitionRepository;
    private final NodeDefinitionVersionRepository nodeDefinitionVersionRepository;
    private final ShellNodeRepository shellNodeRepository;
    private final ParameterRepository parameterRepository;
    private final RegistryClient registryClient;
    private final ApplicationEventPublisher publisher;

    public HubApplication(
            NodeDefinitionRepositoryImpl nodeDefinitionRepository,
            NodeDefinitionVersionRepository nodeDefinitionVersionRepository,
            ShellNodeRepository shellNodeRepository,
            ParameterRepository parameterRepository,
            RegistryClient registryClient,
            ApplicationEventPublisher publisher
    ) {
        this.nodeDefinitionRepository = nodeDefinitionRepository;
        this.nodeDefinitionVersionRepository = nodeDefinitionVersionRepository;
        this.shellNodeRepository = shellNodeRepository;
        this.parameterRepository = parameterRepository;
        this.registryClient = registryClient;
        this.publisher = publisher;
    }

    @Transactional
    public void addShellNodes(List<ShellNode> shellNodes) {
        this.shellNodeRepository.addAll(shellNodes);
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
                .deprecated(false)
                .build();

        List<Parameter> parameters = new ArrayList<>();
        var inputParameters = nodeDsl.getInputParameters().stream().map(parameter -> {
            var p = Parameter.Type.getTypeByName(parameter.getType()).newParameter(parameter.getValue(), true);
            parameters.add(p);
            return NodeParameter.Builder.aNodeParameter()
                    .name(parameter.getName())
                    .description(parameter.getDescription())
                    .ref(parameter.getRef())
                    .type(parameter.getType())
                    .parameterId(p.getId())
                    .value(parameter.getValue())
                    .required(parameter.getRequired())
                    .build();
        }).collect(Collectors.toList());

        var outputParameters = nodeDsl.getOutputParameters().stream().map(parameter -> {
            var p = Parameter.Type.getTypeByName(parameter.getType()).newParameter(parameter.getValue(), true);
            parameters.add(p);
            return NodeParameter.Builder.aNodeParameter()
                    .name(parameter.getName())
                    .description(parameter.getDescription())
                    .ref(parameter.getRef())
                    .type(parameter.getType())
                    .parameterId(p.getId())
                    .value(parameter.getValue())
                    .required(parameter.getRequired())
                    .build();
        }).collect(Collectors.toList());

        this.parameterRepository.addAll(parameters);

        var version = NodeDefinitionVersion.Builder.aNodeDefinitionVersion()
                .id("local/" + nodeDsl.getRef() + ":" + nodeDsl.getVersion())
                .creatorName("local")
                .creatorRef("local")
                .ownerRef("local")
                .ref(nodeDsl.getRef())
                .version(nodeDsl.getVersion())
                .description(nodeDsl.getDescription())
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

    public Optional<NodeDefinition> findById(String id) {
        return this.nodeDefinitionRepository.findById(id);
    }

    public PageInfo<NodeDefinition> findPage(int pageNum, int pageSize, String ownerRef, String name) {
        return this.nodeDefinitionRepository.findPage(pageNum, pageSize, ownerRef, name);
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
                .deprecated(defDto.getDeprecated())
                .build();
    }

    private NodeDefinitionVersion downloadNodeDefVersion(String ownerRef, String ref, String version) {
        var dto = this.registryClient.findByRefAndVersion(ownerRef + "/" + ref, version)
                .orElseThrow(() -> new DataNotFoundException("未找到节点定义版本: " + ownerRef + "/" + ref + ":" + version));
        List<Parameter> parameters = new ArrayList<>();
        var inputParameters = dto.getInputParameters().stream().map(parameter -> {
            var p = Parameter.Type.getTypeByName(parameter.getType()).newParameter(parameter.getValue(), true);
            parameters.add(p);
            return NodeParameter.Builder.aNodeParameter()
                    .name(parameter.getName())
                    .description(parameter.getDescription())
                    .ref(parameter.getRef())
                    .type(parameter.getType())
                    .parameterId(p.getId())
                    .value(parameter.getValue())
                    .required(parameter.getRequired())
                    .build();
        }).collect(Collectors.toList());

        var outputParameters = dto.getOutputParameters().stream().map(parameter -> {
            var p = Parameter.Type.getTypeByName(parameter.getType()).newParameter(parameter.getValue(), true);
            parameters.add(p);
            return NodeParameter.Builder.aNodeParameter()
                    .name(parameter.getName())
                    .description(parameter.getDescription())
                    .ref(parameter.getRef())
                    .type(parameter.getType())
                    .parameterId(p.getId())
                    .value(parameter.getValue())
                    .required(parameter.getRequired())
                    .build();
        }).collect(Collectors.toList());

        this.parameterRepository.addAll(parameters);

        return NodeDefinitionVersion.Builder.aNodeDefinitionVersion()
                .id(dto.getOwnerRef() + "/" + dto.getRef() + ":" + dto.getVersion())
                .ownerRef(dto.getOwnerRef())
                .ref(dto.getRef())
                .creatorName(dto.getCreatorName())
                .creatorRef(dto.getCreatorRef())
                .version(dto.getVersion())
                .description(dto.getDescription())
                .resultFile(dto.getResultFile())
                .inputParameters(inputParameters)
                .outputParameters(outputParameters)
                .spec(dto.getSpec())
                .build();
    }

    private NodeDef findShellNodeDef(String type) {
        var shellNode = this.shellNodeRepository.findById(getVersion(type))
                .orElseThrow(() -> new DataNotFoundException("未找到节点定义: shell:" + getVersion(type)));
        var inputParameters = shellNode.getEnvironment().keySet().stream()
                .map(s -> NodeParameter.Builder.aNodeParameter()
                        .ref(s)
                        .name(s)
                        .type("STRING")
                        .value("")
                        .build()
                )
                .collect(Collectors.toList());
        return NodeDef.builder()
                .name("Shell Node")
                .description("Shell Node")
                .workerType("DOCKER")
                .type(type)
                .image(shellNode.getImage())
                .script(shellNode.getScript())
                .inputParameters(inputParameters)
                .build();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public NodeDef getByType(String type) {
        if (type.startsWith("shell:")) {
            return this.findShellNodeDef(type);
        }
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
        if (type.startsWith("shell:")) {
            return this.findShellNodeDef(type);
        }
        var node = this.nodeDefinitionRepository.findById(getOwnerRef(type) + "/" + getRef(type))
                .orElseThrow(() -> new DataNotFoundException("未找到节点定义: " + type));
        var version =
                this.nodeDefinitionVersionRepository.findByOwnerRefAndRefAndVersion(getOwnerRef(type), getRef(type), getVersion(type))
                        .orElseThrow(() -> new DataNotFoundException("未找到节点定义版本: " + type));

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

    public Optional<NodeDefinitionVersion> findByOwnerRefAndRefAndVersion(String ownerRef, String ref, String version) {
        return this.nodeDefinitionVersionRepository.findByOwnerRefAndRefAndVersion(ownerRef, ref, version);
    }
}
