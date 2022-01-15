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
 * 项目导入类型
 */
export enum ProjectImporterTypeEnum {
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
  WEBHOOK = 'WEBHOOK',
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
 * 密钥管理器类型
 */
export enum CredentialManagerTypeEnum {
  LOCAL = 'local',
  VAULT = 'vault',
}

/**
 * webhook请求状态
 */
export enum WebhookRequstStateEnum {
  OK = 'OK',
  NOT_ACCEPTABLE = 'NOT_ACCEPTABLE',
  UNAUTHORIZED = 'UNAUTHORIZED',
  NOT_FOUND = 'NOT_FOUND',
  UNKNOWN = 'UNKNOWN',
}

/**
 * 参数类型
 */
export enum ParamTypeEnum {
  SECRET = 'SECRET'
}