import { NodeTypeEnum, ParamTypeEnum } from './enumeration';

/**
 * 参数值类型
 */
export type ParamValueType = string | number | boolean;

/**
 * 参数数据
 */
export interface IParamData {
  ref: string;
  name: string;
  type: ParamTypeEnum;
  required: boolean;
  value: ParamValueType;
  description?: string;
}

/**
 * 节点数据
 */
export interface INodeData {
  name: string;
  ref: string;
  type: NodeTypeEnum;
  description?: string;
  version: string;
  versionDescription?: string;
  icon?: string;
  inputs: IParamData[];
  outputs: IParamData[];
}

/**
 * 工作流数据
 */
export interface IWorkflowData {

}

/**
 * Cron节点数据
 */
export interface ICronData extends INodeData {

}

/**
 * Webhook节点数据
 */
export interface IWebhookData extends INodeData {

}

/**
 * Shell节点数据
 */
export interface IShellData extends INodeData {

}

/**
 * 异步任务节点数据
 */
export interface IAsyncTaskData extends INodeData {

}