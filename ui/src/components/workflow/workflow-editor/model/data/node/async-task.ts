import { BaseNode } from './base-node';
import { FailureModeEnum, NodeTypeEnum, ParamTypeEnum } from '../enumeration';
import defaultIcon from '../../../svgs/shape/async-task.svg';
import { CustomRule, CustomRuleItem } from '../common';

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
  readonly ownerRef: string;
  version: string;
  readonly inputs: IAsyncTaskParam[];
  readonly outputs: IAsyncTaskParam[];
  failureMode: FailureModeEnum;

  constructor(ownerRef: string, ref: string, name: string, icon: string = '', version: string = '',
    inputs: IAsyncTaskParam[] = [], outputs: IAsyncTaskParam[] = [],
    failureMode: FailureModeEnum = FailureModeEnum.SUSPEND) {
    super(ref, name, NodeTypeEnum.ASYNC_TASK, icon || defaultIcon, `https://jianmuhub.com/${ownerRef}/${ref}/${version}`);
    this.ownerRef = ownerRef;
    this.version = version;
    this.inputs = inputs;
    this.outputs = outputs;
    this.failureMode = failureMode;
  }

  static build({ ownerRef, ref, name, icon, version, inputs, outputs, failureMode }: any): AsyncTask {
    return new AsyncTask(ownerRef, ref, name, icon, version, inputs, outputs, failureMode);
  }

  getFormRules(): Record<string, CustomRule> {
    const rules = super.getFormRules();

    return {
      ...rules,
      // TODO 待完善校验规则
      version: [],
      inputs: {
        type: 'array',
        required: true,
        len: this.inputs.length,
        fields: {
          value: [
            {
              // TODO 根据required动态确定
              required: true,
              message: '参数值不能为空',
              trigger: 'blur',
            },
          ] as CustomRuleItem[],
          type: [],
          exp: [],
        },
      },
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
      type: `${this.ownerRef}/${super.getRef()}:${version}`,
      param: inputs.length === 0 ? undefined : param,
    };
  }
}

