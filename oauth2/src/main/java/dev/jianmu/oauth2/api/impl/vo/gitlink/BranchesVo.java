package dev.jianmu.oauth2.api.impl.vo.gitlink;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchesVo implements IBranchesVo {
    private List<Branch> branches;
    private Integer status;
    private String message;

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
   public static class Branch {
        private String name;
        private String http_url;
        private String zip_url;
        private String tar_url;
    }
}