/**
 * 画布类型枚举
 */
export enum GraphTypeEnum {
  G6 = 'G6',
  X6 = 'X6',
}

/**
 * 画布方向枚举
 */
export enum GraphDirectionEnum {
  HORIZONTAL = 'HORIZONTAL',
  VERTICAL = 'VERTICAL',
}

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
  FLOW_NODE = 'flow-node',
}

/**
 * 节点工具栏tab类型枚举
 */
export enum NodeToolbarTabTypeEnum {
  RETRY = 'retry',
  IGNORE = 'ignore',
  LOG = 'log',
  PARAMS = 'params',
  CACHE = 'cache',
}
