import {
  FailureModeEnum,
  TaskParamTypeEnum,
  TaskStatusEnum,
  TriggerTypeEnum,
  WorkflowExecutionRecordStatusEnum,
} from '@/api/dto/enumeration';
import { ITaskCacheVo } from '@/api/dto/cache';

/**
 * 流程执行记录vo
 */
export interface IWorkflowExecutionRecordVo
  extends Readonly<{
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
  }> {}

/**
 * 异步任务实例vo
 */
export interface IAsyncTaskInstanceVo
  extends Readonly<{
    id: string;
    triggerId: string;
    workflowRef: string;
    workflowVersion: string;
    workflowInstanceId: string;
    name: string;
    description: string;
    status: TaskStatusEnum;
    failureMode: FailureModeEnum;
    asyncTaskRef: string;
    asyncTaskType: string;
    serialNo: number;
    startTime: string;
    endTime?: string;
    taskCaches?: ITaskCacheVo[];
  }> {}

/**
 * 任务执行记录vo
 */
export interface ITaskExecutionRecordVo
  extends Readonly<{
    instanceId: string;
    businessId: string;
    nodeName: string;
    defKey: string;
    startTime: string;
    endTime?: string;
    status: TaskStatusEnum;
    taskCaches?: ITaskCacheVo[];
  }> {}

/**
 * 任务参数vo
 */
export interface ITaskParamVo
  extends Readonly<{
    ref: string;
    type: TaskParamTypeEnum;
    valueType: string;
    value: string;
  }> {}
