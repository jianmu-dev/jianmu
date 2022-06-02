package dev.jianmu.infrastructure.docker;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Ethan Liu
 * @class VolumeMount
 * @description VolumeMount
 * @create 2022-06-02 21:11
 */
@Getter
@Setter
@Builder
public class VolumeMount {
    private String source;
    private String target;
}
