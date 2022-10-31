import { restProxy } from '@/api';
import { IGitRepoBranchVo, IGitRepoFlowViewingDto, IGitRepoVo } from '@/api/dto/git-repo';
import { IProjectVo } from '@/api/dto/project';
import { API_PREFIX } from '@/utils/constants';

export const baseUrl = `${API_PREFIX}/git_repos`;

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

/**
 * 获取gitRepo信息
 */

export function getGitRepo(id: string): Promise<IGitRepoVo> {
  return restProxy({
    url: `${baseUrl}/${id}`,
    method: 'get',
    auth: true,
  });
}
