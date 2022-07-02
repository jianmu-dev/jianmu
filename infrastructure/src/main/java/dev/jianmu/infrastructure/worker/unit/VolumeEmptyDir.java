package dev.jianmu.infrastructure.worker.unit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class VolumeEmptyDir
 * @description VolumeEmptyDir
 * @author Daihw
 * @create 2022/6/28 3:28 下午
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VolumeEmptyDir {
    private String id;
    private String name;
    private String medium;
    @JsonProperty("size_limit")
    private Integer sizeLimit;
}
