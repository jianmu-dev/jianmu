/**
 * 流程执行记录状态枚举
 */
export enum WorkflowExecutionRecordStatusEnum {
  RUNNING = 'RUNNING',
  FINISHED = 'FINISHED',
  TERMINATED = 'TERMINATED',
}

/**
 * 项目状态枚举
 */
export enum ProjectStatusEnum {
  INIT = 'INIT',
  RUNNING = 'RUNNING',
  FAILED = 'FAILED',
  SUCCEEDED = 'SUCCEEDED',
}

/**
 * 任务状态枚举
 */
export enum TaskStatusEnum {
  INIT = 'INIT',
  WAITING = 'WAITING',
  RUNNING = 'RUNNING',
  SKIPPED = 'SKIPPED',
  FAILED = 'FAILED',
  SUCCEEDED = 'SUCCEEDED',
}

/**
 * 流程定义导入类型
 */
export enum WorkflowDefinitionImporterTypeEnum {
  SSH = 'SSH',
  HTTPS = 'HTTPS',
}

/**
 * DSL来源枚举
 */
export enum DslSourceEnum {
  GIT = 'GIT',
  LOCAL = 'LOCAL',
}

/**
 * DSL类型枚举
 */
export enum DslTypeEnum {
  WORKFLOW = 'WORKFLOW',
  PIPELINE = 'PIPELINE',
}

/**
 * 任务参数类型枚举
 */
export enum TaskParamTypeEnum {
  INPUT = 'INPUT',
  OUTPUT = 'OUTPUT',
}

/**
 * 触发类型枚举
 */
export enum TriggerTypeEnum {
  EVENT_BRIDGE = 'EVENT_BRIDGE',
  CRON = 'CRON',
  MANUAL = 'MANUAL',
}

/**
 * 节点类型枚举
 */
export enum NodeTypeEnum {
  DOCKER = 'DOCKER',
  SHELL = 'SHELL',
}

/**
 * 归属类型枚举
 */
export enum OwnerTypeEnum {
  PERSONAL = 'PERSONAL',
  ORGANIZATION = 'ORGANIZATION',
  LOCAL = 'LOCAL',
}

/**
 * 事件桥接器来源类型枚举
 */
export enum EventBridgeSourceTypeEnum {
  WEBHOOK = 'WEBHOOK',
  SERVICE = 'SERVICE',
}

/**
 * 事件桥接器目标转换器类型枚举
 */
export enum EventBridgeTargetTransformerTypeEnum {
  QUERY = 'QUERY',
  HEADER = 'HEADER',
  BODY = 'BODY',
}