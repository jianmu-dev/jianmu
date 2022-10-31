import { GitRepoEnum, ProjectStatusEnum } from '@/api/dto/enumeration';

/**
 * 查询流水线列表Dto
 */
export interface IGitRepoFlowViewingDto
  extends Readonly<{
    name?: string;
    status?: ProjectStatusEnum;
    branch?: string;
    sortType?: GitRepoEnum;
  }> {}

/**
 * 查询分支列表Vo
 */
export interface IGitRepoBranchVo
  extends Readonly<{
    branchName: string;
    isDefault: boolean;
  }> {}

export interface IGitRepoVo
  extends Readonly<{
    owner: string;
    ref: string;
  }> {}
