package dev.jianmu.api.dto.gitlink;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class GitCommitter {
    @NotBlank(message = "committer.name不能为空")
    private String name;
    @NotBlank(message = "committer.email不能为空")
    private String email;
}
