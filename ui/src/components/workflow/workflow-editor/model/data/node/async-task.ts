import { BaseNode, buildSelectableInnerOutputParam } from './base-node';
import { FailureModeEnum, NodeTypeEnum, ParamTypeEnum, RefTypeEnum } from '../enumeration';
import defaultIcon from '../../../svgs/shape/async-task.svg';
import { CustomRule, ParamValueType, ValidateParamFn, ValidateCacheFn } from '../common';
import { ISelectableParam } from '../../../../workflow-expression-editor/model/data';
import { TaskStatusEnum } from '@/api/dto/enumeration';
import { checkDuplicate } from '../../util/reference';

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

/**
 * 模拟缓存定义
 */
export interface ICache {
  key: string;
  name: string;
  value: string;
}

export class AsyncTask extends BaseNode {
  readonly ownerRef: string;
  readonly nodeRef: string;
  version: string;
  versionDescription: string;
  readonly inputs: IAsyncTaskParam[];
  readonly outputs: IAsyncTaskParam[];
  caches: ICache[];
  failureMode: FailureModeEnum;
  private readonly validateParam?: ValidateParamFn;
  private readonly validateCache?: ValidateCacheFn;

  constructor(
    ownerRef: string,
    nodeRef: string,
    ref: string,
    name: string,
    icon = '',
    version = '',
    versionDescription = '',
    inputs: IAsyncTaskParam[] = [],
    outputs: IAsyncTaskParam[] = [],
    caches: ICache[] = [],
    failureMode: FailureModeEnum = FailureModeEnum.SUSPEND,
    validateParam?: ValidateParamFn,
    validateCache?: ValidateCacheFn,
  ) {
    super(
      ref,
      name,
      NodeTypeEnum.ASYNC_TASK,
      checkDefaultIcon(icon) ? defaultIcon : icon,
      `https://jianmuhub.com/${ownerRef}/${nodeRef}/${version}`,
    );
    this.ownerRef = ownerRef;
    this.nodeRef = nodeRef;
    this.version = version;
    this.versionDescription = versionDescription;
    this.inputs = inputs;
    this.outputs = outputs;
    this.caches = caches;
    this.failureMode = failureMode;
    this.validateParam = validateParam;
    this.validateCache = validateCache;
  }

  static build(
    { ownerRef, nodeRef, ref, name, icon, version, versionDescription, inputs, outputs, caches, failureMode }: any,
    validateParam?: ValidateParamFn,
    validateCache?: ValidateCacheFn,
  ): AsyncTask {
    return new AsyncTask(
      ownerRef,
      nodeRef,
      ref,
      name,
      icon,
      version,
      versionDescription,
      inputs,
      outputs,
      caches,
      failureMode,
      validateParam,
      validateCache,
    );
  }

  async buildSelectableParam(nodeId: string): Promise<ISelectableParam | undefined> {
    const children: ISelectableParam[] = [];
    if (this.outputs.length > 0) {
      children.push(
        ...this.outputs.map(({ ref, type, name }) => {
          return {
            value: ref,
            type,
            label: name || ref,
          };
        }),
      );
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
          },
        ];
      }
      fields[index] = {
        type: 'object',
        required,
        fields: {
          value,
        } as Record<string, CustomRule>,
      };
    });

    const shellCacheFields: Record<string, CustomRule> = {};
    this.caches.forEach((_, index) => {
      shellCacheFields[index] = {
        type: 'object',
        required: true,
        fields: {
          name: [
            { required: true, message: '请选择缓存', trigger: 'change' },
            {
              validator: (rule: any, name: any, callback: any) => {
                if (name && this.validateCache) {
                  try {
                    this.validateCache(name);
                  } catch ({ message }) {
                    callback(message);
                    return;
                  }
                }
                callback();
              },
              trigger: 'change',
            },
          ],
          value: [
            { required: true, message: '请输入目录', trigger: 'blur' },
            {
              pattern: /^\//,
              message: '请输入以/开头的目录',
              trigger: 'blur',
            },
            {
              validator: (rule: any, _value: any, callback: any) => {
                if (!_value) {
                  callback();
                  return;
                }
                try {
                  checkDuplicate(
                    this.caches.map(({ value }) => value),
                    RefTypeEnum.DIR,
                  );
                } catch ({ message, ref }) {
                  if (ref === _value) {
                    callback(message);
                    return;
                  }
                }
                callback();
              },
              trigger: 'blur',
            },
          ],
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
      caches: {
        type: 'array',
        required: false,
        len: this.caches.length,
        fields: shellCacheFields,
      },
      failureMode: [{ required: true }],
    };
  }

  // eslint-disable-next-line @typescript-eslint/ban-types
  toDsl(): object {
    const { ownerRef, nodeRef, ref, name, version, inputs, caches, failureMode } = this;
    const param: {
      [key: string]: ParamValueType;
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

    const _cache: {
      [key: string]: string;
    } = {};
    caches.forEach(({ name, value }) => (_cache[name] = value));

    return {
      ref,
      name,
      'on-failure': failureMode === FailureModeEnum.SUSPEND ? undefined : failureMode,
      cache: caches.length === 0 ? undefined : _cache,
      task: `${ownerRef === OFFICIAL_NODE_OWNER_REF ? '' : `${ownerRef}/`}${nodeRef}@${version}`,
      input: inputs.length === 0 ? undefined : param,
    };
  }
}
