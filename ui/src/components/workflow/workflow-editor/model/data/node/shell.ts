import { BaseNode } from './base-node';
import { FailureModeEnum, NodeRefEnum, NodeTypeEnum, RefTypeEnum } from '../enumeration';
import icon from '../../../svgs/shape/shell.svg';
import { CustomRule, ValidateCacheFn, ValidateParamFn } from '../common';
import { checkDuplicate } from '../../util/reference';

export interface IShellEnv {
  key: string;
  name: string;
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
    name = 'shell',
    image = '',
    envs: IShellEnv[] = [],
    script = '',
    caches: ICache[] = [],
    failureMode: FailureModeEnum = FailureModeEnum.SUSPEND,
    validateParam?: ValidateParamFn,
    validateCache?: ValidateCacheFn,
  ) {
    super(NodeRefEnum.SHELL, name, NodeTypeEnum.SHELL, icon, 'https://v2.jianmu.dev/guide/shell-node.html');
    this.image = image;
    this.envs = envs;
    this.script = script;
    this.caches = caches;
    this.failureMode = failureMode;
    this.validateParam = validateParam;
    this.validateCache = validateCache;
  }

  static build(
    { name, image, envs, script, caches, failureMode }: any,
    validateParam: ValidateParamFn | undefined,
    validateCache: ValidateCacheFn | undefined,
  ): Shell {
    return new Shell(name, image, envs, script, caches, failureMode, validateParam, validateCache);
  }

  getFormRules(): Record<string, CustomRule> {
    const rules = super.getFormRules();

    const shellEnvFields: Record<string, CustomRule> = {};
    this.envs.forEach((_, index) => {
      shellEnvFields[index] = {
        type: 'object',
        required: true,
        fields: {
          name: [{ required: true, message: '请输入变量名', trigger: 'blur' }],
          value: [
            { required: true, message: '请输入变量值', trigger: 'blur' },
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

  // eslint-disable-next-line @typescript-eslint/ban-types
  toDsl(): object {
    const { name, image, envs, script, caches, failureMode } = this;
    const environment: {
      [key: string]: string;
    } = {};
    envs.forEach(({ name, value }) => (environment[name] = value));

    const _cache: {
      [key: string]: string;
    } = {};
    caches.forEach(({ name, value }) => (_cache[name] = value));

    return {
      alias: name,
      'on-failure': failureMode === FailureModeEnum.SUSPEND ? undefined : failureMode,
      image,
      cache: caches.length === 0 ? undefined : _cache,
      environment: envs.length === 0 ? undefined : environment,
      script: script ? script.split('\n') : undefined,
    };
  }
}
