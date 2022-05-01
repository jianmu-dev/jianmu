import { NodeTypeEnum } from './enumeration';

/**
 * 密钥
 */
export interface ISecretKey {
  namespace: string;
  key: string;
}

/**
 * 节点数据
 */
export interface INodeData {
  getName(): string;

  getType(): NodeTypeEnum;

  getIcon(): string;

  /**
   * 校验
   * @throws Error
   */
  validate(): void;
}

/**
 * 工作流数据
 */
export interface IWorkflowData {

}