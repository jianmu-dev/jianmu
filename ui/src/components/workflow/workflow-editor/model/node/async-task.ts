import { INodeData } from '../data';
import { NodeTypeEnum } from '../enumeration';
import defaultIcon from '../../svgs/shape/async-task.svg';

export class AsyncTask implements INodeData {
  name: string;
  readonly ref: string;
  readonly icon?: string;

  constructor(name: string, ref: string, icon: string | undefined) {
    this.name = name;
    this.ref = ref;
    this.icon = icon;
  }

  getName(): string {
    return this.name;
  }

  getRef(): string {
    return this.ref;
  }

  getType(): NodeTypeEnum {
    return NodeTypeEnum.ASYNC_TASK;
  }

  getIcon(): string {
    return this.icon || defaultIcon;
  }

  validate(): void {
    if (!this.name) {
      throw new Error('名称不能为空');
    }
  }
}

