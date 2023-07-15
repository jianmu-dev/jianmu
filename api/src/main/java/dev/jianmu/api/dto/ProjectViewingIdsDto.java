package dev.jianmu.api.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class ProjectViewingIdsDto {
    @NotNull(message = "id不能为空")
    private List<String> ids;
}
