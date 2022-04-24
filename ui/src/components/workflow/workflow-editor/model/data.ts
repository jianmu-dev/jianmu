import { NodeTypeEnum } from './enumeration';

/**
 * 节点数据
 */
export interface INodeData {
  nodeRef: string;
  nodeType: NodeTypeEnum;
  image?: string,
  text: string,
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