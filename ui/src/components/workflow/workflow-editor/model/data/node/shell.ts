import { BaseNode } from './base-node';
import { FailureModeEnum, NodeTypeEnum } from '../enumeration';
import icon from '../../../svgs/shape/shell.svg';

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

  getFormRules(): any {
    const rules = super.getFormRules();

    return {
      ...rules,
      // TODO 待完善校验规则
      image: [],
      env_name: [],
      env_value: [],
      script: [],
    };
  }
}

