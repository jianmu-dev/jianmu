package dev.jianmu.infrastructure.worker.unit;

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
    private String nodeName;
    private Map<String, String> nodeSelector;
    private String serviceAccountName;
}
