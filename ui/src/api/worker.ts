import { restProxy } from '@/api/index';
import { IWorkerVo } from '@/api/dto/worker';

export const baseUrl = '/frontend/workers';

/**
 * 获取worker类型列表
 */
export function listType(): Promise<string[]> {
  return restProxy({
    url: `${baseUrl}/types`,
    method: 'get',
    auth: true,
  });
}

/**
 * 获取worker类型列表
 */
export function fetchWorkerList(): Promise<IWorkerVo[]> {
  return restProxy({
    url: `${baseUrl}`,
    method: 'get',
    auth: true,
  });
}

/**
 * 获取worker类型列表
 */
export function deleteWorker(id: string): Promise<void> {
  return restProxy({
    url: `${baseUrl}/${id}`,
    method: 'delete',
    auth: true,
  });
}