import {
  TaskParamTypeEnum,
  TaskStatusEnum,
  TriggerTypeEnum,
  WorkflowExecutionRecordStatusEnum,
} from '@/api/dto/enumeration';

/**
 * 流程执行记录vo
 */
export interface IWorkflowExecutionRecordVo extends Readonly<{
  id: string;
  serialNo: number;
  name: string;
  workflowRef: string;
  workflowVersion: string;
  description?: string;
  startTime: string;
  endTime?: string;
  suspendedTime?: string;
  status: WorkflowExecutionRecordStatusEnum | '';
  triggerId: string;
  triggerType: TriggerTypeEnum;
}> {
}

/**
 * 任务执行记录vo
 */
export interface ITaskExecutionRecordVo extends Readonly<{
  instanceId: string;
  nodeName: string;
  defKey: string;
  startTime: string;
  endTime?: string;
  status: TaskStatusEnum;
}> {
}

/**
 * 任务参数vo
 */
export interface ITaskParamVo extends Readonly<{
  ref: string;
  type: TaskParamTypeEnum;
  valueType: string;
  value: string;
}> {
}