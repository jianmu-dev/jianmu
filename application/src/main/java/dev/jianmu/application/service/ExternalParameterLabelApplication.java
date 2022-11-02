package dev.jianmu.application.service;

import dev.jianmu.external_parameter.aggregate.ExternalParameterLabel;
import dev.jianmu.external_parameter.repository.ExternalParameterLabelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author huangxi
 * @class ExternalParameterLabelApplication
 * @description ExternalParameterLabelApplication
 * @create 2022-07-13 16:28
 */
@Service
public class ExternalParameterLabelApplication {
    private final ExternalParameterLabelRepository externalParameterLabelRepository;

    public ExternalParameterLabelApplication(ExternalParameterLabelRepository externalParameterLabelRepository) {
        this.externalParameterLabelRepository = externalParameterLabelRepository;
    }

    @Transactional
    public List<ExternalParameterLabel> findAll(String id, String type, String platform) {
        return this.externalParameterLabelRepository.findAll(id, type, platform);
    }
}
