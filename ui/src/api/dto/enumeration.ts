/**
 * 流程执行记录状态枚举
 */
export enum WorkflowExecutionRecordStatusEnum {
  INIT = 'INIT',
  RUNNING = 'RUNNING',
  FINISHED = 'FINISHED',
  TERMINATED = 'TERMINATED',
  SUSPENDED = 'SUSPENDED',
}

/**
 * 项目状态枚举
 */
export enum ProjectStatusEnum {
  INIT = 'INIT',
  RUNNING = 'RUNNING',
  FAILED = 'FAILED',
  SUCCEEDED = 'SUCCEEDED',
  SUSPENDED = 'SUSPENDED',
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
  SUSPENDED = 'SUSPENDED',
  IGNORED = 'IGNORED',
}

/**
 * 项目导入类型枚举
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
  LOCAL = 'LOCAL',
  COMMUNITY = 'COMMUNITY'
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
 * 密钥管理器类型枚举
 */
export enum CredentialManagerTypeEnum {
  LOCAL = 'local',
  VAULT = 'vault',
}

/**
 * webhook请求状态枚举
 */
export enum WebhookRequstStateEnum {
  OK = 'OK',
  NOT_ACCEPTABLE = 'NOT_ACCEPTABLE',
  UNAUTHORIZED = 'UNAUTHORIZED',
  NOT_FOUND = 'NOT_FOUND',
  UNKNOWN = 'UNKNOWN',
}

/**
 * 参数类型枚举
 */
export enum ParamTypeEnum {
  SECRET = 'SECRET'
}

/**
 * 项目排序枚举
 */
export enum SortTypeEnum {
  DEFAULT_SORT = 'DEFAULT_SORT',
  LAST_MODIFIED_TIME = 'LAST_MODIFIED_TIME',
  LAST_EXECUTION_TIME = 'LAST_EXECUTION_TIME'
}

/**
 * 失败处理模式枚举
 */
export enum FailureModeEnum {
  IGNORE = 'IGNORE',
  SUSPEND = 'SUSPEND',
}

/**
 * 节点定义可见类型枚举
 */
export enum VisibleTypeEnum {
  PUBLIC = 'PUBLIC',
  PRIVATE = 'PRIVATE',
  ORGANIZATION_VISIBLE = 'ORGANIZATION_VISIBLE',
}
