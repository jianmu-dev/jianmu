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
export interface IWorkflowNode {
  getRef(): string;

  getName(): string;

  getType(): NodeTypeEnum;

  getIcon(): string;

  getDocUrl(): string;

  /**
   * 校验
   * @throws Error
   */
  validate(): void;
}

export interface IGlobal {
  concurrent: boolean;
}

/**
 * 工作流数据
 */
export interface IWorkflow {
  name: string;
  description?: string;
  groupId: string;
  global: IGlobal;
  data: string;
}