package dev.jianmu.oauth2.api.impl.vo.gitee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.jianmu.oauth2.api.vo.IBranchesVo;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author huangxi
 * @class BranchesVo
 * @description BranchesVo
 * @create 2022-07-08 16:54
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BranchesVo implements IBranchesVo {
    private List<Branch> branches;

    @Override
    public List<String> getBranchNames() {
        List<String> branchNames = new ArrayList<>();
        for (Branch branch : this.branches) {
            branchNames.add(branch.getName());
        }
        return branchNames;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Branch {
        private String name;
    }
}