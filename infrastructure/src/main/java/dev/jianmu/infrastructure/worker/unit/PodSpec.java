package dev.jianmu.infrastructure.worker.unit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PodSpec {
    private String name;
    private String namespace;
    private Map<String, String> annotations;
    private Map<String, String> labels;
    @JsonProperty("node_name")
    private String nodeName;
    @JsonProperty("node_selector")
    private Map<String, String> nodeSelector;
    @JsonProperty("service_account_name")
    private String serviceAccountName;
}
