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
  // name: string;
  // ref: string;
  // type: NodeTypeEnum;
  // description?: string;
  // version: string;
  // versionDescription?: string;
  // icon?: string;
  // inputs: IParamData[];
  // outputs: IParamData[];

  getName(): string;

  getRef(): string;

  getType(): NodeTypeEnum;

  getIcon(): string;

  /**
   * 校验
   * @throws Error
   */
  validate(): void;

  // getDescription(): string | undefined;
  // getVersion(): string;
  // getVersionDescription(): string | undefined;

  // getInputs(): IParamData[];
  // getOutputs(): IParamData[];
}

/**
 * 工作流数据
 */
export interface IWorkflowData {

}