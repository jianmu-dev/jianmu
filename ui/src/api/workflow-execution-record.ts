import { restProxy } from '@/api/index';

export const baseUrl = {
  workflow: '/workflow_instances',
};
const nodeLogUrl = '/view/logs/task/download';
const workflowLogUrl = '/view/logs/workflow/download';

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
 * 终止所有流程执行
 * @param workflowRef
 */
export function terminateAll(workflowRef: string): Promise<void> {
  return restProxy({
    url: `${baseUrl.workflow}/${workflowRef}/stop`,
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

/**
 * 任务日志下载接口
 * @param id
 */
export function downloadNodeLogs(id: string): Promise<void> {
  return restProxy({
    url: `${nodeLogUrl}/${id}`,
    method: 'get',
  });
}

/**
 * 流程日志下载接口
 * @param id
 */
export function downloadWorkflowLogs(id: string): Promise<void> {
  return restProxy({
    url: `${workflowLogUrl}/${id}`,
    method: 'get',
  });
}
