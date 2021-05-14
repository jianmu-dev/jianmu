package dev.jianmu.api.vo;

import dev.jianmu.project.aggregate.GitRepo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class: GitRepoVo
 * @description: GitRepoVo
 * @author: Ethan Liu
 * @create: 2021-05-13 19:46
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Git仓库Vo")
public class GitRepoVo {
    private String id;
    private String uri;
    private GitRepo.Type type;
    private String branch;
    private String directory;
}
