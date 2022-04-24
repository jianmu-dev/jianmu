import { NodeTypeEnum } from './enumeration';

/**
 * 节点数据
 */
export interface INodeData {
  nodeType: NodeTypeEnum;
  image?: string,
  text: string,
}

/**
 * 工作流数据
 */
export interface IWorkflowData {

}