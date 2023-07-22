package dev.jianmu.infrastructure.worker.unit;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jianmu.infrastructure.worker.K8sVolumeMount;
import dev.jianmu.infrastructure.worker.WorkerSecret;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Runner {
    private String id;
    private Integer version;
    private String[] command;
    private Boolean detach;
    @JsonProperty("depends_on")
    private String[] dependsOn;
    private String[] entrypoint;
    private Map<String, String> envs;
    private String image;
    private String name;
    private String placeholder;
    private Boolean privileged;
    private String pull;
    private List<SecretVar> secrets;
    @JsonProperty("spec_secrets")
    private List<WorkerSecret> specSecrets;
    private Integer user;
    private Integer group;
    private List<K8sVolumeMount> volumes;
    @JsonProperty("working_dir")
    private String workingDir;
    private String resultFile;

    public void setRegistryAddress(String registryAddress) {
        if (ObjectUtils.isEmpty(registryAddress)) {
            return;
        }
        var nameParts = this.image.split("/", 2);
        if (nameParts.length == 1) {
            this.image = String.join("/", registryAddress, "library", this.image);
        }
        if (!nameParts[0].contains(".") && !nameParts[0].contains(":") && !nameParts[0].equals("localhost")) {
            this.image = String.join("/", registryAddress, this.image);
        }
    }
}
