import { restProxy } from '@/api';
import { IGitRepoBranchVo, IGitRepoFlowViewingDto } from '@/api/dto/git-repo';
import { IProjectVo } from '@/api/dto/project';

export const baseUrl = '/git_repos';

/**
 * 查询流水线列表
 * @param dto
 */
export function getAssemblyLineList(dto: IGitRepoFlowViewingDto): Promise<IProjectVo[]> {
  return restProxy<IProjectVo[]>({
    url: `${baseUrl}/flows`,
    method: 'get',
    payload: dto,
    auth: true,
  });
}

/**
 * 查询分支列表
 */
export function getBranches(): Promise<IGitRepoBranchVo[]> {
  return restProxy<IGitRepoBranchVo[]>({
    url: `${baseUrl}/branches`,
    method: 'get',
    auth: true,
  });
}
