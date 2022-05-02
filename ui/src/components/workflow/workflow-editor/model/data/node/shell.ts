import { BaseNode } from './base-node';
import { NodeTypeEnum } from '../enumeration';
import icon from '../../../svgs/shape/shell.svg';

export interface IShellEnv {
  name: string;
  value: string;
}

export class Shell extends BaseNode {
  image: string;
  readonly envs: IShellEnv[];
  readonly scripts: string[];

  constructor(name: string = 'shell', image: string = '',
    envs: IShellEnv[] = [], scripts: string[] = []) {
    super('shell', name, NodeTypeEnum.SHELL, icon, 'https://docs.jianmu.dev/guide/shell-node.html');
    this.image = image;
    this.envs = envs;
    this.scripts = scripts;
  }

  static build({ name, image, envs, scripts }: any): Shell {
    return new Shell(name, image, envs, scripts);
  }

  validate(): void {
    super.validate();

    // TODO 待完善校验规则
  }
}

