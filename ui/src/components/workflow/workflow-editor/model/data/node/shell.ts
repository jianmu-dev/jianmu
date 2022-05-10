import { BaseNode } from './base-node';
import { FailureModeEnum, NodeTypeEnum } from '../enumeration';
import icon from '../../../svgs/shape/shell.svg';
import { CustomRule, CustomRuleItem } from '../common';

export interface IShellEnv {
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

    return {
      ...rules,
      // TODO 待完善校验规则
      image: [],
      envs: {
        type: 'array',
        required: true,
        len: this.envs.length,
        fields: {
          name: [
            {
              required: true,
              message: '环境变量名称不能为空',
              trigger: 'blur',
            },
          ] as CustomRuleItem[],
          type: [],
          exp: [],
        },
      },
      script: [],
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
      script: script.split('\n'),
    };
  }
}

