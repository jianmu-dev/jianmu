package dev.jianmu.infrastructure.docker;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @class ContainerSpec
 * @description 容器规格定义
 * @author Daihw
 * @create 2022/5/19 4:21 下午
 */
@Getter
@Setter
@Builder
public class ContainerSpec {
    private String image;
    private String network;
    private String[] networks;
    private String[] labels;
    private String working_dir;
    private String user;
    private String host;
    private String sock;
    private String[] environment;
    private String[] secrets;
    private String[] entrypoint;
    private String[] args;
    private String[] volume_mounts;
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
}
