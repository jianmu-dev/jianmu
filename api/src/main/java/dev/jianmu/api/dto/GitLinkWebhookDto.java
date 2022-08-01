package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

@Data
@Schema(description = "GitLinkWebhookDto")
public class GitLinkWebhookDto {
    @Pattern(regexp = "^refs/heads/.*$", message = "ref格式错误")
    private String ref;

    @Valid
    @NotNull(message = "commits不能为空")
    private List<GitCommit> commits;

    @Valid
    @NotNull(message = "repository不能为空")
    private GitRepository repository;

    @Valid
    @NotNull(message = "pusher不能为空")
    private GitOwner pusher;


    public String getBranch() {
        return this.ref.split("/", 3)[2];
    }
}
