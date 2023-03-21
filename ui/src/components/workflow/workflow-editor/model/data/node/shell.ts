import { BaseNode, buildSelectableInnerOutputParam } from './base-node';
import { FailureModeEnum, NodeRefEnum, NodeTypeEnum, ParamTypeEnum, RefTypeEnum } from '../enumeration';
import icon from '../../../svgs/shape/shell.svg';
import { CustomRule, ValidateParamFn, ValidateCacheFn } from '../common';
import { ISelectableParam } from '@/components/workflow/workflow-expression-editor/model/data';
import { checkDuplicate } from '../../util/reference';

export interface IShellEnv {
  key: string;
  name: string;
  type: ParamTypeEnum.SECRET | ParamTypeEnum.STRING;
  value: string;
}

/**
 * 模拟缓存定义
 */
export interface ICache {
  key: string;
  name: string;
  value: string;
}

export class Shell extends BaseNode {
  image: string;
  readonly envs: IShellEnv[];
  script: string;
  caches: ICache[];
  failureMode: FailureModeEnum;
  private readonly validateParam?: ValidateParamFn;
  private readonly validateCache?: ValidateCacheFn;

  constructor(
    ref: string = NodeRefEnum.SHELL,
    name = 'shell',
    image = '',
    envs: IShellEnv[] = [],
    script = '',
    caches: ICache[] = [],
    failureMode: FailureModeEnum = FailureModeEnum.SUSPEND,
    validateParam?: ValidateParamFn,
    validateCache?: ValidateCacheFn,
  ) {
    super(ref, name, NodeTypeEnum.SHELL, icon, '');
    this.image = image;
    this.envs = envs;
    this.script = script;
    this.caches = caches;
    this.failureMode = failureMode;
    this.validateParam = validateParam;
    this.validateCache = validateCache;
  }

  static build(
    { ref, name, image, envs, script, caches, failureMode }: any,
    validateParam?: ValidateParamFn,
    validateCache?: ValidateCacheFn,
  ): Shell {
    return new Shell(ref, name, image, envs, script, caches, failureMode, validateParam, validateCache);
  }

  async buildSelectableParam(nodeId: string): Promise<ISelectableParam | undefined> {
    const { ref, name } = this;
    return {
      value: ref,
      label: name || ref,
      children: [await buildSelectableInnerOutputParam()],
    };
  }

  getFormRules(): Record<string, CustomRule> {
    const rules = super.getFormRules();

    const shellEnvFields: Record<string, CustomRule> = {};
    this.envs.forEach((_, index) => {
      shellEnvFields[index] = {
        type: 'object',
        required: true,
        fields: {
          name: [
            { required: true, message: '请输入变量名', trigger: 'blur' },
            {
              validator: (rule: any, value: any, callback: any) => {
                if (!value) {
                  callback();
                  return;
                }
                try {
                  checkDuplicate(
                    this.envs.map(({ name }) => name),
                    RefTypeEnum.SHELL_ENV,
                  );
                } catch ({ message, ref }) {
                  if (ref === value) {
                    callback(message);
                    return;
                  }
                }
                callback();
              },
              trigger: 'blur',
            },
          ],
          value: [
            { required: true, message: '请输入变量值', trigger: 'change' },
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
          ],
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
      image: [{ required: true, message: '请选择或输入镜像', trigger: 'change' }],
      envs: {
        type: 'array',
        required: false,
        len: this.envs.length,
        fields: shellEnvFields,
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

  toDsl(): object {
    const { ref, name, image, envs, script, caches, failureMode } = this;
    const env: {
      [key: string]: string;
    } = {};
    envs.forEach(({ name, type, value }) => {
      // 区分string和secret类型
      if (type === ParamTypeEnum.STRING) {
        env[name] = value;
        return;
      }
      env[name] = value;
    });

    const _cache: {
      [key: string]: string;
    } = {};
    caches.forEach(({ name, value }) => (_cache[name] = value));

    return {
      ref,
      name,
      'on-failure': failureMode === FailureModeEnum.SUSPEND ? undefined : failureMode,
      image,
      cache: caches.length === 0 ? undefined : _cache,
      env: envs.length === 0 ? undefined : env,
      script: script ? script.split('\n') : undefined,
    };
  }
}
