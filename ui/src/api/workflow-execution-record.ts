import { restProxy } from '@/api/index';
import { IPageVo } from '@/api/dto/common';
import { IWorkflowExecutionRecordQueryingDto, IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';

export const baseUrl = {
  workflow: '/workflow_instances',
  log: '/logs',
};

/**
 * 查询流程执行记录
 * @param dto
 */
export function query(dto: IWorkflowExecutionRecordQueryingDto): Promise<IPageVo<IWorkflowExecutionRecordVo>> {
  return restProxy<IPageVo<IWorkflowExecutionRecordVo>>({
    url: baseUrl.workflow,
    method: 'get',
    payload: dto,
    auth: true,
  });
}

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
