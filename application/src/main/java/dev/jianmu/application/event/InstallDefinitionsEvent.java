package dev.jianmu.application.event;

import dev.jianmu.task.aggregate.Definition;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * @class: InstallDefinitionsEvent
 * @description: InstallDefinitionsEvent
 * @author: Ethan Liu
 * @create: 2021-06-22 16:11
 **/
@Builder
@Getter
public class InstallDefinitionsEvent {
    private List<Definition> definitions;
}
