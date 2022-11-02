package dev.jianmu.application.service;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.exception.ExternalParameterRepeatException;
import dev.jianmu.application.exception.IncorrectParameterTypeException;
import dev.jianmu.application.exception.NoAssociatedPermissionException;
import dev.jianmu.external_parameter.aggregate.ExternalParameter;
import dev.jianmu.external_parameter.aggregate.ExternalParameterLabel;
import dev.jianmu.external_parameter.repository.ExternalParameterLabelRepository;
import dev.jianmu.external_parameter.repository.ExternalParameterRepository;
import dev.jianmu.git.repo.repository.GitRepoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author huangxi
 * @class ExternalParameterApplication
 * @description ExternalParameterApplication
 * @create 2022-07-13 14:01
 */
@Service
public class ExternalParameterApplication {
    private final ExternalParameterRepository externalParameterRepository;
    private final ExternalParameterLabelRepository externalParameterLabelRepository;

    public ExternalParameterApplication(ExternalParameterRepository externalParameterRepository, ExternalParameterLabelRepository externalParameterLabelRepository, GitRepoRepository gitRepoRepository) {
        this.externalParameterRepository = externalParameterRepository;
        this.externalParameterLabelRepository = externalParameterLabelRepository;
    }

    @Transactional
    public void create(String name, ExternalParameter.Type type, String ref, String label, String value, String associationId, String associationType, String associationPlatform) {
        if (this.externalParameterRepository.findByRef(ref, associationId, associationType, associationPlatform).isPresent()) {
            throw new ExternalParameterRepeatException("外部参数唯一标识重复");
        }

        this.checkParameterType(type, value);
        this.externalParameterRepository.add(
                ExternalParameter.Builder.aReference()
                        .name(name)
                        .label(label)
                        .ref(ref)
                        .type(type)
                        .value(value)
                        .associationId(associationId)
                        .associationType(associationType)
                        .associationPlatform(associationPlatform)
                        .build());
        this.saveLabel(associationId, associationType, associationPlatform, label);
    }

    @Transactional
    public void delete(String id, String associationId, String associationType, String associationPlatform) {
        ExternalParameter externalParameter = this.externalParameterRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到该外部参数"));

        if (associationId != null && associationType != null &&
                (!associationId.equals(externalParameter.getAssociationId()) || !associationType.equals(externalParameter.getAssociationType()) || !associationPlatform.equals(externalParameter.getAssociationPlatform()))) {
            throw new NoAssociatedPermissionException("无此仓库权限", externalParameter.getAssociationId(), externalParameter.getAssociationType(), externalParameter.getAssociationPlatform());
        }

        this.externalParameterRepository.deleteById(id);
    }

    @Transactional
    public void update(String id, String value, String name, String label, ExternalParameter.Type type, String associationId, String associationType, String associationPlatform) {
        ExternalParameter externalParameter = this.externalParameterRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到外部参数：" + "\"" + name + "\""));

        if (associationId != null && associationType != null &&
                (!associationId.equals(externalParameter.getAssociationId()) || !associationType.equals(externalParameter.getAssociationType()) || !associationPlatform.equals(externalParameter.getAssociationPlatform()))) {
            throw new NoAssociatedPermissionException("无此仓库权限", externalParameter.getAssociationId(), externalParameter.getAssociationType(), externalParameter.getAssociationPlatform());
        }

        this.checkParameterType(type, value);
        externalParameter.setLabel(label);
        externalParameter.setName(name);
        externalParameter.setType(type);
        externalParameter.setValue(value);
        externalParameter.setLastModifiedTime();

        this.externalParameterRepository.updateById(externalParameter);
        this.saveLabel(associationId, associationType, label, associationPlatform);
    }

    @Transactional
    public ExternalParameter get(String id) {
        return this.externalParameterRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到该外部参数"));
    }

    @Transactional
    public List<ExternalParameter> findAll(String id, String type, String associationPlatform) {
        return this.externalParameterRepository.findAll(id, type, associationPlatform);
    }

    private void saveLabel(String associationId, String associationType, String associationPlatform, String label) {
        if (this.externalParameterLabelRepository.findByValue(associationId, associationType, associationPlatform, label).isPresent()) {
            return;
        }
        this.externalParameterLabelRepository.add(
                ExternalParameterLabel.Builder.aReference()
                        .associationId(associationId)
                        .associationType(associationType)
                        .associationPlatform(associationPlatform)
                        .value(label)
                        .build());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void checkParameterType(ExternalParameter.Type type, String value) {
        switch (type) {
            case BOOL:
                if (value.equals("true") || value.equals("false")) {
                    break;
                }
                throw new IncorrectParameterTypeException("参数类型不匹配，" + value + "不是" + type + "类型");
            case NUMBER:
                try {
                    new BigDecimal(value).doubleValue();
                } catch (NumberFormatException e) {
                    throw new IncorrectParameterTypeException("参数类型不匹配，" + value + "不是" + type + "类型");
                }
        }
    }
}
