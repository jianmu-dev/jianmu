import { RuleItem } from 'async-validator';
import { NodeTypeEnum } from './enumeration';
import { ISelectableParam } from '../../../workflow-expression-editor/model/data';

type TriggerValue = 'blur' | 'change';

export interface CustomRuleItem extends RuleItem {
  trigger?: TriggerValue;
}

export type CustomRule = CustomRuleItem | CustomRuleItem[];

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

  buildSelectableParam(): ISelectableParam | undefined;

  getFormRules(): Record<string, CustomRule>;

  /**
   * 校验
   * @throws Error
   */
  validate(): Promise<void>;

  toDsl(): object;
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