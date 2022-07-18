package dev.jianmu.infrastructure.elimpl;

import dev.jianmu.workflow.el.ResultType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultSucceedVo {
    private ResultType type;
    private Object value;
}
