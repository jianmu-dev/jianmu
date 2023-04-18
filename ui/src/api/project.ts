import { restProxy } from '@/api';
import {
  IGitCloningDto,
  IGitVo,
  IProjectIdVo,
  IProjectImportingDto,
  IProjectSavingDto,
  IProjectTriggeringDto,
  ITriggerDefinitionVo,
  ITriggerProjectVo,
} from '@/api/dto/project';

export const baseUrl = {
  project: '/projects',
  git: '/git',
};

/**
 * 保存项目
 * @param dto
 */
export async function save(dto: IProjectSavingDto): Promise<IProjectIdVo> {
  const url = `${baseUrl.project}${dto.id ? `/${dto.id}` : ''}`;
  const method = dto.id ? 'put' : 'post';

  const res = await restProxy({
    url,
    method,
    payload: dto,
    auth: true,
  });

  return dto.id
    ? {
      id: dto.id,
    }
    : res;
}

/**
 * 立即执行
 * @param id
 * @param dto (项目触发dto，没传入dto请求时默认转换成空对象)
 */
export function executeImmediately(id: string, dto?: IProjectTriggeringDto): Promise<ITriggerProjectVo> {
  return restProxy({
    url: `${baseUrl.project}/trigger/${id}`,
    method: 'post',
    auth: true,
    payload: dto ? dto : {},
  });
}

/**
 * 删除
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
export function listGit(
  id: string,
  dir?: string,
): Promise<{
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
 * 导入项目
 * @param dto
 */
export function _import(dto: IProjectImportingDto): Promise<void> {
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

/**
 * 激活项目
 * @param id
 * @param enable
 */
export function active(id: string, enable: boolean): Promise<void> {
  return restProxy({
    url: `${baseUrl.project}/${enable ? 'enable' : 'disable'}/${id}`,
    method: 'put',
    auth: true,
  });
}

/**
 * 获取webhook触发器定义
 */
export function fetchWebhookDefinition(projectId: string): Promise<ITriggerDefinitionVo> {
  return restProxy({
    url: `${baseUrl.project}/${projectId}/trigger_def`,
    method: 'get',
    auth: true,
  });
}
