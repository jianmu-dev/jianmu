import { RuleItem } from 'async-validator';
import { NodeTypeEnum, ParamTypeEnum } from './enumeration';
import { ISelectableParam } from '../../../workflow-expression-editor/model/data';
import { Global } from './global';

type TriggerValue = 'blur' | 'change';

export type ParamValueType = string | number | boolean;

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

  getDisplayName(): string;

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

export interface IGlobalParam {
  ref: string;
  key: string;
  name: string;
  type: ParamTypeEnum;
  required: boolean;
  value: string;
  hidden: boolean;
}

export interface IGlobal {
  concurrent: number | boolean;
  caches: string[];
  params?: IGlobalParam[];
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
  global: IGlobal;
  data: string;
}

export type ValidateParamFn = (value: string) => void;

export type ValidateCacheFn = (name: string) => void;
