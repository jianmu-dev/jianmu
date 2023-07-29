package dev.jianmu.application.service;

import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.repository.ParameterRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @class ParameterApplication
 * @description 参数门面类
 * @author Ethan Liu
 * @create 2021-04-07 16:53
*/
@Service
public class ParameterApplication {

    private final ParameterRepository parameterRepository;

    public ParameterApplication(
            ParameterRepository parameterRepository
    ) {
        this.parameterRepository = parameterRepository;
    }

    public List<Parameter> findParameters(Set<String> ids) {
        return this.parameterRepository.findByIds(ids);
    }
}
