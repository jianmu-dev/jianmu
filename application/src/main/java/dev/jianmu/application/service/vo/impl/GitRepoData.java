package dev.jianmu.application.service.vo.impl;

import dev.jianmu.application.service.vo.AssociationData;
import lombok.Builder;
import lombok.Getter;

/**
 * @author huangxi
 * @class GitRepoData
 * @description GitRepoData
 * @create 2022-07-22 17:15
 */
@Getter
@Builder
public class GitRepoData extends AssociationData {
    private String ref;
    private String owner;
}
