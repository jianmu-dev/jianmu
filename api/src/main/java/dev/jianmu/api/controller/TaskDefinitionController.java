package dev.jianmu.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @class: TaskDefinitionController
 * @description: 任务定义接口
 * @author: Ethan Liu
 * @create: 2021-04-12 11:44
 **/
@RestController
@RequestMapping("task_definition")
@Tag(name = "任务定义接口", description = "提供任务定义创建删除等API")
public class TaskDefinitionController {

}
