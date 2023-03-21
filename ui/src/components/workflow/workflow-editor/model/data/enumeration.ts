/**
 * 节点ref枚举
 */
export enum NodeRefEnum {
  CRON = 'cron',
  WEBHOOK = 'webhook',
  SHELL = 'shell',
  START = 'start',
  END = 'end',
}

/**
 * 参数类型枚举
 */
export enum ParamTypeEnum {
  SECRET = 'SECRET',
  STRING = 'STRING',
  NUMBER = 'NUMBER',
  BOOL = 'BOOL',
}

/**
 * 节点类型枚举
 */
export enum NodeTypeEnum {
  CRON = 'cron',
  WEBHOOK = 'webhook',
  // CONDITION = 'condition',
  SHELL = 'shell',
  ASYNC_TASK = 'async-task',
  START = 'start',
  END = 'end',
}

/**
 * 缩放类型
 */
export enum ZoomTypeEnum {
  IN = 'IN',
  OUT = 'OUT',
  ORIGINAL = 'ORIGINAL',
  FIT = 'FIT',
  CENTER = 'CENTER',
}

/**
 * 失败处理模式枚举
 */
export enum FailureModeEnum {
  IGNORE = 'ignore',
  SUSPEND = 'suspend',
}

/**
 * 节点分组枚举
 */
export enum NodeGroupEnum {
  TRIGGER = 'trigger',
  INNER = 'inner',
  LOCAL = 'local',
  OFFICIAL = 'official',
  COMMUNITY = 'community',
}

/**
 * 表达式类型
 */
export enum ExpressionTypeEnum {
  GLOBAL_PARAM = 'GLOBAL_PARAM',
  WEBHOOK_PARAM = 'WEBHOOK_PARAM',
  WEBHOOK_TOKEN = 'WEBHOOK_TOKEN',
  WEBHOOK_ONLY = 'WEBHOOK_ONLY',
  NODE_INPUT = 'NODE_INPUT',
  SHELL_ENV = 'SHELL_ENV',
}
