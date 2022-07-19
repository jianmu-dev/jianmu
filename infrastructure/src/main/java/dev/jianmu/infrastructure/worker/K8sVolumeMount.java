package dev.jianmu.infrastructure.worker;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class K8sVolumeMount {
    private String name;
    private String path;
    @JsonProperty("sub_path")
    private String subPath;
    @JsonProperty("read_only")
    private Boolean readOnly;
}
