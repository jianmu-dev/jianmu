import { BaseNode } from './base-node';
import { FailureModeEnum, NodeTypeEnum } from '../enumeration';
import icon from '../../../svgs/shape/shell.svg';
import { CustomRule } from '../common';

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

  constructor(name: string = 'shell', image: string = '',
    envs: IShellEnv[] = [], script: string = '',
    failureMode: FailureModeEnum = FailureModeEnum.SUSPEND) {
    super('shell', name, NodeTypeEnum.SHELL, icon, 'https://docs.jianmu.dev/guide/shell-node.html');
    this.image = image;
    this.envs = envs;
    this.script = script;
    this.failureMode = failureMode;
  }

  static build({ name, image, envs, script, failureMode }: any): Shell {
    return new Shell(name, image, envs, script, failureMode);
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
          value: [{ required: true, message: '请输入变量值', trigger: 'blur' }],
        } as Record<string, CustomRule>,
      };
    });

    return {
      ...rules,
      image: [{ required: true, message: '请输入镜像', trigger: 'blur' }],
      envs: {
        type: 'array',
        required: true,
        len: this.envs.length,
        fields: shellEnvFields,
      },
      script: [{ required: true, message: '请输入shell脚本', trigger: 'blur' }],
    };
  }

  toDsl(): object {
    const { name, image, envs, script, failureMode } = this;
    const environment: {
      [key: string]: string;
    } = {};
    envs.forEach(({ name, value }) => (environment[name] = value));

    return {
      alias: name,
      'on-failure': failureMode === FailureModeEnum.SUSPEND ? undefined : failureMode,
      image,
      environment: envs.length === 0 ? undefined : environment,
      script: script ? script.split('\n') : undefined,
    };
  }
}

