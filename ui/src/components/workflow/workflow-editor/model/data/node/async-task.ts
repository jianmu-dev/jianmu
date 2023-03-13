import { BaseNode } from './base-node';
import { FailureModeEnum, NodeTypeEnum, ParamTypeEnum, RefTypeEnum } from '../enumeration';
import defaultIcon from '../../../svgs/shape/async-task.svg';
import { CustomRule, ValidateCacheFn, ValidateParamFn } from '../common';
import { ISelectableParam } from '../../../../workflow-expression-editor/model/data';
import { INNER_PARAM_LABEL, INNER_PARAM_TAG } from '../../../../workflow-expression-editor/model/const';
import { TaskStatusEnum } from '@/api/dto/enumeration';
import { checkDuplicate } from '../../util/reference';

export interface IAsyncTaskParam {
  readonly ref: string;
  readonly name: string;
  readonly type: ParamTypeEnum;
  readonly required: boolean;
  value: string;
  readonly description?: string;
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
      `https://jianmuhub.com/${ownerRef}/${ref}/${version}`,
    );
    this.ownerRef = ownerRef;
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
    { ownerRef, ref, name, icon, version, versionDescription, inputs, outputs, caches, failureMode }: any,
    validateParam: ValidateParamFn | undefined,
    validateCache: ValidateCacheFn | undefined,
  ): AsyncTask {
    return new AsyncTask(
      ownerRef,
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

  buildSelectableParam(nodeId: string): ISelectableParam | undefined {
    if (this.outputs.length === 0) {
      return undefined;
    }

    const children: ISelectableParam[] = this.outputs.map(({ ref, name }) => {
      return {
        value: ref,
        label: name,
      };
    });
    children.push({
      // 文档：https://v2.jianmu.dev/guide/custom-node.html#_4-%E5%86%85%E7%BD%AE%E8%BE%93%E5%87%BA%E5%8F%82%E6%95%B0
      value: INNER_PARAM_TAG,
      label: INNER_PARAM_LABEL,
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
      value: nodeId,
      label: super.getName(),
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
    const { name, version, inputs, caches, failureMode } = this;
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

      if (!required && !value && type !== ParamTypeEnum.STRING) {
        delete param[ref];
      }
    });

    const _cache: {
      [key: string]: string;
    } = {};
    caches.forEach(({ name, value }) => (_cache[name] = value));

    return {
      alias: name,
      'on-failure': failureMode === FailureModeEnum.SUSPEND ? undefined : failureMode,
      type: `${this.ownerRef}/${super.getRef()}:${version}`,
      cache: caches.length === 0 ? undefined : _cache,
      param: inputs.length === 0 ? undefined : param,
    };
  }
}
