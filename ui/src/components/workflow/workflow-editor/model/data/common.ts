import { RuleItem } from 'async-validator';
import { NodeTypeEnum } from './enumeration';
import { ISelectableParam } from '../../../workflow-expression-editor/model/data';

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

  buildSelectableParam(nodeId: string): ISelectableParam | undefined;

  getFormRules(): Record<string, CustomRule>;

  /**
   * 校验
   * @throws Error
   */
  validate(): Promise<void>;

  // eslint-disable-next-line @typescript-eslint/ban-types
  toDsl(): object;
}

export interface ICache {
  ref: string;
  key: string;
}

export interface IGlobal {
  concurrent: number | boolean;
  caches?: ICache[];
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

export type ValidateParamFn = (value: string) => void;

export type ValidateCacheFn = (name: string) => void;
