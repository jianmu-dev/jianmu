import { restProxy } from '@/api';
import {
  IProjectGroupCreatingDto,
  IProjectGroupEditingDto,
  IProjectGroupSortUpdatingDto,
  IProjectGroupAddingDto,
  IProjectSortUpdatingDto,
} from '@/api/dto/project-group';
const baseUrl = '/projects/groups';
/**
 * 创建项目组
 */
export function createProjectGroup(
  dto: IProjectGroupCreatingDto,
): Promise<void> {
  return restProxy({
    url: `${baseUrl}`,
    method: 'post',
    payload: dto,
    auth: true,
  });
}

/**
 * 编辑项目组
 */
export function editProjectGroup(
  projectGroupId: string,
  dto: IProjectGroupEditingDto,
): Promise<void> {
  return restProxy({
    url: `${baseUrl}/${projectGroupId}`,
    method: 'put',
    payload: dto,
    auth: true,
  });
}

/**
 * 删除项目组
 * @param projectGroupId 项目组id
 */
export function deleteProjectGroup(projectGroupId: string): Promise<void> {
  return restProxy({
    url: `${baseUrl}/${projectGroupId}`,
    method: 'delete',
    auth: true,
  });
}

/**
 * 修改项目组排序
 * @param dto
 */
export function updateProjectGroupSort(
  dto: IProjectGroupSortUpdatingDto,
): Promise<void> {
  return restProxy({
    url: `${baseUrl}/sort`,
    method: 'patch',
    payload: dto,
    auth: true,
  });
}

/**
 * 修改项目组排序
 * @param dto
 */
export function projectGroupAddProject(
  dto: IProjectGroupAddingDto,
): Promise<void> {
  return restProxy({
    url: `${baseUrl}/projects`,
    method: 'post',
    payload: dto,
    auth: true,
  });
}

/**
 * 修改项目组的项目排序
 */
export function updateProjectGroupProjectSort(
  projectGroupId: string,
  dto: IProjectSortUpdatingDto,
): Promise<void> {
  return restProxy({
    url: `${baseUrl}/${projectGroupId}/projects/sort`,
    method: 'patch',
    payload: dto,
    auth: true,
  });
}

/**
 * 项目组删除项目
 */
export function deleteProjectGroupProject(
  projectLinkGroupId: string,
): Promise<void> {
  return restProxy({
    url: `${baseUrl}/projects/${projectLinkGroupId}`,
    method: 'delete',
    auth: true,
  });
}

/**
 * 修改项目组是否展示
 */
export function updateProjectGroupShow(projectGroupId: string): Promise<void> {
  return restProxy({
    url: `${baseUrl}/${projectGroupId}/is_show`,
    method: 'put',
    auth: true,
  });
}
/**
 * 项目组添加项目
 */
export function addProject(dto: IProjectGroupAddingDto): Promise<void> {
  return restProxy({
    url: `${baseUrl}/projects`,
    method: 'post',
    auth: true,
    payload: dto,
  });
}
