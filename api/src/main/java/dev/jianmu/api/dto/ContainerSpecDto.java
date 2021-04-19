package dev.jianmu.api.dto;

import dev.jianmu.task.aggregate.spec.HostConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @class: ContainerSpecDto
 * @description: 容器规约Dto
 * @author: Ethan Liu
 * @create: 2021-04-20 06:54
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "容器规约Dto")
public class ContainerSpecDto {
    private String name;
    private String hostName;
    private String domainName;
    private String user;
    private String[] cmd;
    private String[] entrypoint;
    @Schema(required = true)
    @NotBlank
    private String image;
    private String workingDir;
    private HostConfig hostConfig = new HostConfig();
}
