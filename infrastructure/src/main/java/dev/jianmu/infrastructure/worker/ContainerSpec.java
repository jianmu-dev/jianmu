package dev.jianmu.infrastructure.worker;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Daihw
 * @class ContainerSpec
 * @description 容器规格定义
 * @create 2022/5/19 4:21 下午
 */
@Getter
@Setter
@Builder
public class ContainerSpec {
    private String image;
    private String network;
    private String[] networks;
    private Map<String, String> labels;
    private String working_dir;
    private String user;
    private String host;
    private String sock;
    private Map<String, String> environment;
    private Set<WorkerSecret> secrets;
    private String[] entrypoint;
    // command
    private String[] args;
    private List<VolumeMount> volume_mounts;
    private Boolean privileged;
    private Integer shm_size;
    private String[] dns;
    private String[] dns_search;
    private String[] extra_hosts;
    private Integer cpu_period;
    private Integer cpu_quota;
    private Integer cpu_shares;
    private String[] cpu_set;
    private Integer memswap_limit;
    private Integer mem_limit;
    private String[] devices;
    private Boolean detach;

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
