import { restProxy } from '@/api';
import {
  IGitCloningDto,
  IGitVo,
  IWorkflowDefinitionImportingDto,
  IWorkflowDefinitionQueryingDto,
  IWorkflowDefinitionSavingDto,
  IWorkflowDefinitionSourceVo,
  IWorkflowDefinitionVo,
} from '@/api/dto/workflow-definition';
import { IPageVo } from '@/api/dto/common';

export const baseUrl = {
  project: '/projects',
  git: '/git',
};

/**
 * 查询流程定义
 * @param dto
 */
export function query(dto: IWorkflowDefinitionQueryingDto): Promise<IPageVo<IWorkflowDefinitionVo>> {
  return restProxy({
    url: baseUrl.project,
    method: 'get',
    payload: dto,
    auth: true,
  });
}

/**
 * 保存流程定义
 * @param dto
 */
export function save(dto: IWorkflowDefinitionSavingDto): Promise<void> {
  const url = `${baseUrl.project}${dto.id ? `/${dto.id}` : ''}`;
  const method = dto.id ? 'put' : 'post';

  return restProxy({
    url,
    method,
    payload: dto,
    auth: true,
  });
}

/**
 * 获取流程定义源码
 * @param workflowRef
 * @param workflowVersion
 */
export function fetchSource(workflowRef: string, workflowVersion: string): Promise<IWorkflowDefinitionSourceVo> {
  return restProxy({
    url: `${baseUrl.project}/source/${workflowRef}/${workflowVersion}`,
    method: 'get',
    auth: true,
  });
}

/**
 * 立即执行
 * @param id
 */
export function executeImmediately(id: string): Promise<void> {
  return restProxy({
    url: `${baseUrl.project}/trigger/${id}`,
    method: 'post',
    auth: true,
  });
}

/**
 * 删除流程定义
 * @param id
 */
export function del(id: string): Promise<void> {
  return restProxy({
    url: `${baseUrl.project}/${id}`,
    method: 'delete',
    auth: true,
  });
}

/**
 * 克隆Git库
 * @param dto
 */
export function cloneGit(dto: IGitCloningDto): Promise<IGitVo> {
  return restProxy({
    url: `${baseUrl.git}/clone`,
    method: 'post',
    payload: dto,
    auth: true,
    timeout: 5 * 60 * 1000,
  });
}

/**
 * 获取Git库
 * @param id
 * @param dir
 */
export function listGit(id: string, dir?: string): Promise<{
  [key: string]: boolean;
}> {
  return restProxy({
    url: `${baseUrl.git}/list`,
    method: 'get',
    payload: {
      dir: `${id}${dir || ''}`,
    },
    auth: true,
  });
}

/**
 * 导入流程定义
 * @param dto
 */
export function _import(dto: IWorkflowDefinitionImportingDto): Promise<void> {
  return restProxy({
    url: `${baseUrl.project}/import`,
    method: 'post',
    payload: dto,
    auth: true,
  });
}

/**
 * 同步流程定义
 * @param id
 */
export function synchronize(id: string): Promise<void> {
  return restProxy({
    url: `${baseUrl.project}/sync/${id}`,
    method: 'put',
    auth: true,
    timeout: 5 * 60 * 1000,
  });
}