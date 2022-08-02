import { BaseNode, buildSelectableInnerOutputParam } from './base-node';
import { FailureModeEnum, NodeTypeEnum, ParamTypeEnum } from '../enumeration';
import defaultIcon from '../../../svgs/shape/async-task.svg';
import { CustomRule, ValidateParamFn } from '../common';
import { ISelectableParam } from '../../../../workflow-expression-editor/model/data';
import { TaskStatusEnum } from '@/api/dto/enumeration';

const OFFICIAL_NODE_OWNER_REF = '_';

export interface IAsyncTaskParam {
  readonly ref: string;
  readonly name: string;
  readonly type: ParamTypeEnum;
  readonly required: boolean;
  value: string;
  readonly description?: string;
  readonly inner?: boolean;
}

/**
 * 检查是否为默认图标
 * @param icon
 */
export function checkDefaultIcon(icon: string) {
  // TODO 待优化确定默认图标机制
  if (!icon) {
    return true;
  }

  const tags = Object.values(TaskStatusEnum).map(status => `/${status}.`);
  tags.push('/async-task.');
  for (const tag of tags) {
    if (icon.includes(tag)) {
      return true;
    }
  }

  return false;
}

export class AsyncTask extends BaseNode {
  readonly ownerRef: string;
  readonly nodeRef: string;
  version: string;
  versionDescription: string;
  readonly inputs: IAsyncTaskParam[];
  readonly outputs: IAsyncTaskParam[];
  failureMode: FailureModeEnum;
  private readonly validateParam?: ValidateParamFn;

  constructor(ownerRef: string, nodeRef: string, ref: string, name: string, icon: string = '', version: string = '',
    versionDescription: string = '', inputs: IAsyncTaskParam[] = [], outputs: IAsyncTaskParam[] = [],
    failureMode: FailureModeEnum = FailureModeEnum.SUSPEND, validateParam?: ValidateParamFn) {
    super(ref, name, NodeTypeEnum.ASYNC_TASK, checkDefaultIcon(icon) ? defaultIcon : icon,
      `https://jianmuhub.com/${ownerRef}/${nodeRef}/${version}`);
    this.ownerRef = ownerRef;
    this.nodeRef = nodeRef;
    this.version = version;
    this.versionDescription = versionDescription;
    this.inputs = inputs;
    this.outputs = outputs;
    this.failureMode = failureMode;
    this.validateParam = validateParam;
  }

  static build({ ownerRef, nodeRef, ref, name, icon, version, versionDescription, inputs, outputs, failureMode }: any,
    validateParam?: ValidateParamFn): AsyncTask {
    return new AsyncTask(ownerRef, nodeRef, ref, name, icon, version, versionDescription, inputs, outputs, failureMode, validateParam);
  }

  async buildSelectableParam(nodeId: string): Promise<ISelectableParam | undefined> {
    const children: ISelectableParam[] = [];
    if (this.outputs.length > 0) {
      children.push(...this.outputs.map(({ ref, type, name }) => {
        return {
          value: ref,
          type,
          label: name || ref,
        };
      }));
    }

    children.push(await buildSelectableInnerOutputParam());

    const { ref, name } = this;
    return {
      value: ref,
      label: name || ref,
      children,
    };
  }

  getFormRules(): Record<string, CustomRule> {
    const rules = super.getFormRules();
    const fields: Record<string, CustomRule> = {};
    this.inputs.forEach((item, index) => {
      const { required } = item;
      let value;
      if (item.type === ParamTypeEnum.SECRET) {
        value = { required, message: `请选择${item.name}`, trigger: 'change' };
      } else {
        value = [
          { required, message: `请输入${item.name}`, trigger: 'blur' },
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
          }];
      }
      fields[index] = {
        type: 'object',
        required,
        fields: {
          value,
        } as Record<string, CustomRule>,
      };
    });

    return {
      ...rules,
      version: [{ required: true, message: '请选择节点版本', trigger: 'change' }],
      inputs: {
        type: 'array',
        required: this.inputs.length > 0,
        len: this.inputs.length,
        fields,
      },
      failureMode: [{ required: true }],
    };
  }

  toDsl(): object {
    const { ownerRef, nodeRef, ref, name, version, inputs, failureMode } = this;
    const param: {
      [key: string]: string | number | boolean;
    } = {};
    inputs.forEach(({ ref, type, required, value }) => {
      switch (type) {
        case ParamTypeEnum.NUMBER: {
          const val = parseFloat(value);
          if (!isNaN(val)) {
            param[ref] = val;
            return;
          }
          break;
        }
        case ParamTypeEnum.BOOL: {
          switch (value) {
            case 'true':
              param[ref] = true;
              return;
            case 'false':
              param[ref] = false;
              return;
          }
          break;
        }
      }

      if (!param[ref]) {
        param[ref] = value;
      }

      if (!required && !value) {
        delete param[ref];
      }
    });

    return {
      ref,
      name,
      'on-failure': failureMode === FailureModeEnum.SUSPEND ? undefined : failureMode,
      task: `${ownerRef === OFFICIAL_NODE_OWNER_REF ? '' : `${ownerRef}/`}${nodeRef}@${version}`,
      input: inputs.length === 0 ? undefined : param,
    };
  }
}

