import { BaseNode } from './base-node';
import { FailureModeEnum, NodeTypeEnum, ParamTypeEnum } from '../enumeration';
import defaultIcon from '../../../svgs/shape/async-task.svg';

export type ParamValueType = string | number | boolean;

export interface IAsyncTaskParam {
  readonly ref: string;
  readonly name: string;
  readonly type: ParamTypeEnum;
  readonly required: boolean;
  value: ParamValueType;
  readonly description?: string;
}

export class AsyncTask extends BaseNode {
  version: string;
  readonly inputs: IAsyncTaskParam[];
  readonly outputs: IAsyncTaskParam[];
  failureMode: FailureModeEnum;

  /**
   * 构造方法
   * @param ref 格式：ownerRef/ref
   * @param name
   * @param icon
   * @param version
   * @param inputs
   * @param outputs
   * @param failureMode
   */
  constructor(ref: string, name: string, icon: string = '', version: string = '',
    inputs: IAsyncTaskParam[] = [], outputs: IAsyncTaskParam[] = [],
    failureMode: FailureModeEnum = FailureModeEnum.SUSPEND) {
    super(ref, name, NodeTypeEnum.ASYNC_TASK, icon || defaultIcon, `https://jianmuhub.com/${ref}/${version}`);
    this.version = version;
    this.inputs = inputs;
    this.outputs = outputs;
    this.failureMode = failureMode;
  }

  static build({ ref, name, icon, version, inputs, outputs, failureMode }: any): AsyncTask {
    return new AsyncTask(ref, name, icon, version, inputs, outputs, failureMode);
  }

  getFormRules(): any {
    const rules = super.getFormRules();

    return {
      ...rules,
      // TODO 待完善校验规则
      version: [],
      param_value: [],
    };
  }

  toDsl(): object {
    const { name, version, inputs, failureMode } = this;
    const param: {
      [key: string]: ParamValueType;
    } = {};
    inputs.forEach(({ ref, value }) => (param[ref] = value));

    return {
      alias: name,
      'on-failure': failureMode === FailureModeEnum.SUSPEND ? undefined : failureMode,
      type: `${super.getRef()}:${version}`,
      param: inputs.length === 0 ? undefined : param,
    };
  }
}

