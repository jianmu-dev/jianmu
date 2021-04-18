package dev.jianmu.task.aggregate;

import java.util.Set;

/**
 * @class: TaskDefinition
 * @description: 任务定义接口
 * @author: Ethan Liu
 * @create: 2021-04-15 10:45
 **/
public interface Definition {
    String getKey();

    Worker.Type getType();

    Set<TaskParameter> getParameters();
}