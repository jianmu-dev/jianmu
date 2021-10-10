import { IPageDto } from '@/api/dto/common';
import { TaskParamTypeEnum, TaskStatusEnum, TriggerTypeEnum, WorkflowExecutionRecordStatusEnum } from '@/api/dto/enumeration';

/**
 * 查询流程执行记录dto
 */
export interface IWorkflowExecutionRecordQueryingDto extends Readonly<IPageDto & {
  id: string;
  name: string;
  workflowVersion: string;
  status: WorkflowExecutionRecordStatusEnum,
}> {
}

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
  status: WorkflowExecutionRecordStatusEnum | '';
  latestTaskStatus?: TaskStatusEnum;
  triggerId: string;
  triggerType: TriggerTypeEnum;
}> {
}

export interface INodeInfoVo extends Readonly<{
  name: string;
  description?: string;
  icon?: string;
  ownerName: string;
  ownerType: string;
  ownerRef: string;
  creatorName: string;
  creatorRef: string;
  sourceLink?: string;
  documentLink?: string;
  type: string;
  workerType: string;
}> {
}

/**
 * 任务执行记录vo
 */
export interface ITaskExecutionRecordVo extends Readonly<{
  instanceId: string;
  nodeName: string;
  defKey: string;
  nodeInfo: INodeInfoVo;
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