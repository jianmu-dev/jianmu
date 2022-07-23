import { BaseNode } from './base-node';
import { FailureModeEnum, NodeRefEnum, NodeTypeEnum } from '../enumeration';
import icon from '../../../svgs/shape/shell.svg';
import { CustomRule, ValidateParamFn } from '../common';

export interface IShellEnv {
  key: string;
  name: string;
  value: string;
}

export class Shell extends BaseNode {
  image: string;
  readonly envs: IShellEnv[];
  script: string;
  failureMode: FailureModeEnum;
  private readonly validateParam?: ValidateParamFn;

  constructor(ref: string = NodeRefEnum.SHELL, name: string = 'shell', image: string = '',
              envs: IShellEnv[] = [], script: string = '',
              failureMode: FailureModeEnum = FailureModeEnum.SUSPEND, validateParam?: ValidateParamFn) {
    super(ref, name, NodeTypeEnum.SHELL, icon, 'https://docs.jianmu.dev/guide/shell-node.html');
    this.image = image;
    this.envs = envs;
    this.script = script;
    this.failureMode = failureMode;
    this.validateParam = validateParam;
  }

  static build({ref, name, image, envs, script, failureMode}: any,
               validateParam?: ValidateParamFn): Shell {
    return new Shell(ref, name, image, envs, script, failureMode, validateParam);
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

    return {
      ...rules,
      image: [{ required: true, message: '请选择或输入镜像', trigger: 'change' }],
      envs: {
        type: 'array',
        required: false,
        len: this.envs.length,
        fields: shellEnvFields,
      },
      failureMode: [{ required: true }],
    };
  }

  toDsl(): object {
    const { ref, name, image, envs, script, failureMode } = this;
    const environment: {
      [key: string]: string;
    } = {};
    envs.forEach(({ name, value }) => (environment[name] = value));

    return {
      ref,
      name,
      'on-failure': failureMode === FailureModeEnum.SUSPEND ? undefined : failureMode,
      image,
      environment: envs.length === 0 ? undefined : environment,
      script: script ? script.split('\n') : undefined,
    };
  }
}

