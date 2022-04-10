/**
 * 节点类型枚举
 */
export enum NodeTypeEnum {
  CRON = 'cron',
  START = 'start',
  END = 'end',
  CONDITION = 'condition',
  ASYNC_TASK = 'async-task',
  WEBHOOK = 'webhook',
}

/**
 * 节点工具栏tab类型枚举
 */
export enum NodeToolbarTabTypeEnum {
  RETRY = 'retry',
  IGNORE = 'ignore',
  LOG = 'log',
  PARAMS = 'params',
}