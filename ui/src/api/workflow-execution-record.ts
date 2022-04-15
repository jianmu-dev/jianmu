import { restProxy } from '@/api/index';

export const baseUrl = {
  workflow: '/workflow_instances',
};

/**
 * 终止流程执行记录
 * @param id
 */
export function terminate(id: string): Promise<void> {
  return restProxy({
    url: `${baseUrl.workflow}/stop/${id}`,
    method: 'put',
    auth: true,
  });
}

/**
 * 重试任务
 * @param id
 * @param taskRef
 */
export function retryTask(id: string, taskRef: string): Promise<void> {
  return restProxy({
    url: `${baseUrl.workflow}/retry/${id}/${taskRef}`,
    method: 'put',
    auth: true,
  });
}

/**
 * 忽略任务
 * @param id
 * @param taskRef
 */
export function ignoreTask(id: string, taskRef: string): Promise<void> {
  return restProxy({
    url: `${baseUrl.workflow}/ignore/${id}/${taskRef}`,
    method: 'put',
    auth: true,
  });
}
