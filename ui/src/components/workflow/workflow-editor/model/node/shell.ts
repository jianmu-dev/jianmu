import { INodeData } from '../data';
import { NodeTypeEnum } from '../enumeration';
import icon from '../../svgs/shape/shell.svg';

export class Shell implements INodeData {
  name: string;
  readonly ref: string;

  constructor(name: string = 'shell') {
    this.name = name;
    this.ref = 'shell';
  }

  static build({ name }: any): Shell {
    return new Shell(name);
  }

  getName(): string {
    return this.name;
  }

  getRef(): string {
    return this.ref;
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
  }
}

