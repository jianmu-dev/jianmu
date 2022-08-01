package dev.jianmu.api.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @class GitCommit
 * @description GitCommit
 * @author Daihw
 * @create 2022/7/29 4:10 下午
 */
@Data
public class GitCommit {
    @NotNull(message = "commit.added不能为空")
    private List<String> added;
    @NotNull(message = "commit.removed不能为空")
    private List<String> removed;
    @NotNull(message = "commit.modified不能为空")
    private List<String> modified;
}
