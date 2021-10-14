package dev.jianmu.infrastructure.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class: Parameter
 * @description: Parameter
 * @author: Ethan Liu
 * @create: 2021-09-09 09:56
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Parameter {
    private String ref;
    private String name;
    private String description;
    private String type;
    private Object value;
}
