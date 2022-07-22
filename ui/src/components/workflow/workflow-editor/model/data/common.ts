import { RuleItem } from 'async-validator';
import { NodeTypeEnum } from './enumeration';
import { ISelectableParam } from '../../../workflow-expression-editor/model/data';
import { Global } from './global';

type TriggerValue = 'blur' | 'change';

export interface CustomRuleItem extends RuleItem {
  trigger?: TriggerValue;
}

export type CustomRule = CustomRuleItem | CustomRuleItem[];

/**
 * 节点数据
 */
export interface IWorkflowNode {
  getRef(): string;

  getName(): string;

  getType(): NodeTypeEnum;

  getIcon(): string;

  getDocUrl(): string;

  buildSelectableParam(nodeId: string): Promise<ISelectableParam | undefined>;

  getFormRules(): Record<string, CustomRule>;

  /**
   * 校验
   * @throws Error
   */
  validate(): Promise<void>;

  toDsl(): object;
}

export interface IAssociation {
  branch?: string;
  entry: boolean;
  entryUrl?: string;
}

/**
 * 工作流数据
 */
export interface IWorkflow {
  name: string;
  description?: string;
  groupId: string;
  association: IAssociation;
  global: Global;
  data: string;
}

export type ValidateParamFn = (value: string) => void;
