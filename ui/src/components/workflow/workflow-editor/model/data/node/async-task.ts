import { BaseNode } from './base-node';
import { FailureModeEnum, NodeTypeEnum, ParamTypeEnum } from '../enumeration';
import defaultIcon from '../../../svgs/shape/async-task.svg';
import { CustomRule, CustomRuleItem, ValidateParamFn } from '../common';
import { ISelectableParam } from '../../../../workflow-expression-editor/model/data';
import { INNER_PARAM_TAG } from '../../../../workflow-expression-editor/model/const';

export interface IAsyncTaskParam {
  readonly ref: string;
  readonly name: string;
  readonly type: ParamTypeEnum;
  readonly required: boolean;
  value: string;
  readonly description?: string;
}

export class AsyncTask extends BaseNode {
  readonly ownerRef: string;
  version: string;
  readonly inputs: IAsyncTaskParam[];
  readonly outputs: IAsyncTaskParam[];
  failureMode: FailureModeEnum;
  private readonly validateParam?: ValidateParamFn;

  constructor(ownerRef: string, ref: string, name: string, icon: string = '', version: string = '',
    inputs: IAsyncTaskParam[] = [], outputs: IAsyncTaskParam[] = [],
    failureMode: FailureModeEnum = FailureModeEnum.SUSPEND, validateParam?: ValidateParamFn) {
    super(ref, name, NodeTypeEnum.ASYNC_TASK, icon || defaultIcon, `https://jianmuhub.com/${ownerRef}/${ref}/${version}`);
    this.ownerRef = ownerRef;
    this.version = version;
    this.inputs = inputs;
    this.outputs = outputs;
    this.failureMode = failureMode;
    this.validateParam = validateParam;
  }

  static build({ ownerRef, ref, name, icon, version, inputs, outputs, failureMode }: any,
    validateParam?: ValidateParamFn): AsyncTask {
    return new AsyncTask(ownerRef, ref, name, icon, version, inputs, outputs, failureMode, validateParam);
  }

  buildSelectableParam(): ISelectableParam {
    const children: ISelectableParam[] = this.outputs.map(({ ref, name }) => {
      return {
        value: ref,
        label: name,
      };
    });
    children.push({
      // 文档：https://docs.jianmu.dev/guide/custom-node.html#_4-%E5%86%85%E7%BD%AE%E8%BE%93%E5%87%BA%E5%8F%82%E6%95%B0
      value: INNER_PARAM_TAG,
      label: '内置输出参数',
      children: [
        {
          value: 'execution_status',
          label: '节点任务执行状态',
        },
        {
          value: 'start_time',
          label: '节点任务开始时间',
        },
        {
          value: 'end_time',
          label: '节点任务结束时间',
        },
      ],
    });

    return {
      value: super.getRef(),
      label: super.getName(),
      children,
    };
  }

  getFormRules(): Record<string, CustomRule> {
    const rules = super.getFormRules();

    return {
      ...rules,
      // TODO 待完善校验规则
      version: [],
      inputs: {
        type: 'array',
        required: this.inputs.length > 0,
        len: this.inputs.length,
        fields: {
          value: [
            {
              // TODO 根据required动态确定
              required: true,
              message: '参数值不能为空',
              trigger: 'blur',
            },
            {
              validator: (rule: any, value: any, callback: any) => {
                if (value && this.validateParam) {
                  try {
                    this.validateParam(value);
                  } catch ({ message }) {
                    callback(message);
                    return;
                  }
                }
                callback();
              },
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
      [key: string]: string | number | boolean;
    } = {};
    inputs.forEach(({ ref, type, value }) => {
      switch (type) {
        case ParamTypeEnum.NUMBER: {
          const val = parseFloat(value);
          if (!isNaN(val)) {
            param[ref] = val;
          }
          break;
        }
        case ParamTypeEnum.BOOL: {
          switch (value) {
            case 'true':
              param[ref] = true;
              break;
            case 'false':
              param[ref] = false;
              break;
          }
          break;
        }
      }

      if (!param[ref]) {
        param[ref] = value;
      }
    });

    return {
      alias: name,
      'on-failure': failureMode === FailureModeEnum.SUSPEND ? undefined : failureMode,
      type: `${this.ownerRef}/${super.getRef()}:${version}`,
      param: inputs.length === 0 ? undefined : param,
    };
  }
}

