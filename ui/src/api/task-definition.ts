import { restProxy } from '@/api';
import { IPageVo } from '@/api/dto/common';
import {
  ITaskDefinitionCreatingDto,
  ITaskDefinitionQueryingDto,
  ITaskDefinitionVersionCreatingDto,
  ITaskDefinitionVersionDetailVo,
  ITaskDefinitionVersionVo,
  ITaskDefinitionVo,
} from '@/api/dto/task-definition';

export const baseUrl = '/task_definitions';

/**
 * 查询任务定义
 * @param dto
 */
export function query(dto: ITaskDefinitionQueryingDto): Promise<IPageVo<ITaskDefinitionVo>> {
  return restProxy({
    url: baseUrl,
    method: 'get',
    payload: dto,
    auth: true,
  });
}

/**
 * 创建任务定义
 * @param dto
 */
export function create(dto: ITaskDefinitionCreatingDto): Promise<void> {
  return restProxy({
    url: baseUrl,
    method: 'post',
    payload: dto,
    auth: true,
  });
}

/**
 * 创建任务定义版本
 * @param dto
 */
export function createVersion(dto: ITaskDefinitionVersionCreatingDto): Promise<void> {
  return restProxy({
    url: `${baseUrl}/versions`,
    method: 'post',
    payload: dto,
    auth: true,
  });
}

/**
 * 获取任务定义版本列表
 * @param ref 任务定义ref
 */
export function listVersion(ref: string): Promise<ITaskDefinitionVersionVo[]> {
  return restProxy({
    url: `${baseUrl}/versions/${ref}`,
    method: 'get',
    auth: true,
  });
}

/**
 * 获取任务定义版本详情
 * @param ref 任务定义ref
 * @param name 任务定义版本
 */
export function fetchVersionDetail(ref: string, name: string): Promise<ITaskDefinitionVersionDetailVo> {
  return restProxy({
    url: `${baseUrl}/versions/${ref}/${name}`,
    method: 'get',
    auth: true,
  });
}

/**
 * 删除任务定义版本
 * @param ref 任务定义ref
 * @param name 任务定义版本
 */
export function deleteVersion(ref: string, name: string): Promise<void> {
  return restProxy({
    url: `${baseUrl}/versions/${ref}/${name}`,
    method: 'delete',
    auth: true,
  });
}