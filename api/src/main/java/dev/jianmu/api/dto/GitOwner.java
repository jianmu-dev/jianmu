package dev.jianmu.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class GitOwner {
    @NotBlank(message = "repository.owner.login或pusher.login 不能为空")
    private String login;
}
