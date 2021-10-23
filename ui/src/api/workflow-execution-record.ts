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
