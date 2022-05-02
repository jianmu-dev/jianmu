import { IWorkflowNode } from '../common';
import { NodeTypeEnum } from '../enumeration';
import icon from '../../../svgs/shape/shell.svg';

export interface IShellEnv {
  name: string;
  value: string;
}

export class Shell implements IWorkflowNode {
  readonly ref: string = 'shell';
  name: string;
  image: string;
  readonly envs: IShellEnv[];
  readonly scripts: string[];

  constructor(name: string = 'shell', image: string = '', envs: IShellEnv[] = [], scripts: string[] = []) {
    this.name = name;
    this.image = image;
    this.envs = envs;
    this.scripts = scripts;
  }

  static build({ name, image, envs, scripts }: any): Shell {
    return new Shell(name, image, envs, scripts);
  }

  getName(): string {
    return this.name;
  }

  getType(): NodeTypeEnum {
    return NodeTypeEnum.SHELL;
  }

  getIcon(): string {
    return icon;
  }

  validate(): void {
    if (!this.name) {
      throw new Error('名称不能为空');
    }

    // TODO 待完善校验规则
  }
}

