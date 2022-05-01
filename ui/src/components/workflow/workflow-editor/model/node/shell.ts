import { INodeData } from '../data';
import { NodeTypeEnum } from '../enumeration';
import icon from '../../svgs/shape/shell.svg';

export class Shell implements INodeData {
  name: string;
  readonly ref: string;

  constructor() {
    this.name = 'shell';
    this.ref = 'shell';
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

