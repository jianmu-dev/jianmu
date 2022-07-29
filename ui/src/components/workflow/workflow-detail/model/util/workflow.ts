import { WorkflowExecutionRecordStatusEnum } from '@/api/dto/enumeration';

/**
 * 检查是否运行状态
*/
export function checkWorkflowRunning(status: WorkflowExecutionRecordStatusEnum, ignoreSuspended:boolean = true):boolean {
  // status=='' 未启动时 终止按钮隐藏
  if (status==='') {
    return false;
  }
  const statuses = [
    WorkflowExecutionRecordStatusEnum.INIT,
    WorkflowExecutionRecordStatusEnum.RUNNING,
  ];
  if (!ignoreSuspended) {
    statuses.push(WorkflowExecutionRecordStatusEnum.SUSPENDED);
  }
  return statuses.includes(status);
}