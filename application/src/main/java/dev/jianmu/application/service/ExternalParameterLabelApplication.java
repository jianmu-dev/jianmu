package dev.jianmu.application.service;

import dev.jianmu.externalParameter.aggregate.ExternalParameterLabel;
import dev.jianmu.externalParameter.repository.ExternalParameterLabelRepository;
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
    public List<ExternalParameterLabel> findAll() {
        return this.externalParameterLabelRepository.findAll();
    }
}
