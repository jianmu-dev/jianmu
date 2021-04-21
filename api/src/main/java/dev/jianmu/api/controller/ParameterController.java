package dev.jianmu.api.controller;

import dev.jianmu.parameter.aggregate.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @class: ParameterController
 * @description: 参数API
 * @author: Ethan Liu
 * @create: 2021-04-21 11:39
 **/
@RestController
@RequestMapping("parameters")
@Tag(name = "参数API", description = "参数API")
public class ParameterController {

    @GetMapping("/types")
    public Parameter.Type[] getTypes() {
        return Parameter.Type.values();
    }
}
