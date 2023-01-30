import { TaskStatusEnum, WorkflowExecutionRecordStatusEnum } from '../dto/enumeration';
import { IEvent } from './common';

/**
 * 流程实例创建事件
 */
export interface IWorkflowInstanceCreatedEvent extends IEvent {
  /**
   * id
   */
  id: string;
  /**
   * 状态
   */
  status: WorkflowExecutionRecordStatusEnum;
}

/**
 * 流程实例状态更新事件
 */
export interface IWorkflowInstanceStatusUpdatedEvent extends IEvent {
  /**
   * id
   */
  id: string;
  /**
   * 状态
   */
  status: WorkflowExecutionRecordStatusEnum;
  /**
   * startTime
   */
  startTime: string;
  /**
   * endTime
   */
  endTime: string;
  /**
   * suspendedTime
   */
  suspendedTime: string;
}

/**
 * 异步任务实例状态更新事件
 */
export interface IAsyncTaskInstanceStatusUpdatedEvent extends IEvent {
  /**
   * id
   */
  id: string;
  /**
   * 流程实例id
   */
  workflowInstanceId: string;
  /**
   * 状态
   */
  status: TaskStatusEnum;
  startTime?: string;
  endTime?: string;
}
