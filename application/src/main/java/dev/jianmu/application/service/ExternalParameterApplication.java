package dev.jianmu.application.service;

import dev.jianmu.application.exception.ExternalParameterNotFoundException;
import dev.jianmu.application.exception.IncorrectParameterTypeException;
import dev.jianmu.externalParameter.aggregate.ExternalParameter;
import dev.jianmu.externalParameter.aggregate.ExternalParameterLabel;
import dev.jianmu.externalParameter.repository.ExternalParameterLabelRepository;
import dev.jianmu.externalParameter.repository.ExternalParameterRepository;
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

    public ExternalParameterApplication(ExternalParameterRepository externalParameterRepository, ExternalParameterLabelRepository externalParameterLabelRepository) {
        this.externalParameterRepository = externalParameterRepository;
        this.externalParameterLabelRepository = externalParameterLabelRepository;
    }

    @Transactional
    public void create(String name, ExternalParameter.Type type, String ref, String label, String value) {
        this.checkParameterType(type, value);
        this.externalParameterRepository.add(
                ExternalParameter.Builder.aReference()
                        .name(name)
                        .label(label)
                        .ref(ref)
                        .type(type)
                        .value(value)
                        .build());
        this.saveLabel(label);
    }

    @Transactional
    public void delete(String id) {
        this.externalParameterRepository.deleteById(id);
    }

    @Transactional
    public void update(String id, String value, String name, String label, ExternalParameter.Type type) {
        this.checkParameterType(type, value);

        ExternalParameter externalParameter = this.externalParameterRepository.findById(id).orElseThrow(() -> new ExternalParameterNotFoundException("未找到外部参数：" + "\"" + name + "\""));
        externalParameter.setLabel(label);
        externalParameter.setName(name);
        externalParameter.setType(type);
        externalParameter.setValue(value);

        this.externalParameterRepository.updateById(externalParameter);
        this.saveLabel(label);
    }

    @Transactional
    public ExternalParameter get(String id) {
        return this.externalParameterRepository.findById(id).orElseThrow(() -> new ExternalParameterNotFoundException("未找到该外部参数"));
    }

    @Transactional
    public List<ExternalParameter> findAll() {
        return this.externalParameterRepository.findAll();
    }

    private void saveLabel(String label) {
        if (this.externalParameterLabelRepository.findByValue(label).isPresent()) {
            return;
        }
        this.externalParameterLabelRepository.add(
                ExternalParameterLabel.Builder.aReference()
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
