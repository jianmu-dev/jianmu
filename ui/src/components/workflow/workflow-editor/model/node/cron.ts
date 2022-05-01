import { INodeData } from '../data';
import { NodeTypeEnum } from '../enumeration';
import icon from '../../svgs/shape/cron.svg';

export class Cron implements INodeData {
  name: string;
  readonly ref: string;
  schedule: string;

  constructor() {
    this.name = 'cron';
    this.ref = 'cron';
    this.schedule = '';
  }

  getName(): string {
    return this.name;
  }

  getRef(): string {
    return this.ref;
  }

  getType(): NodeTypeEnum {
    return NodeTypeEnum.CRON;
  }

  getIcon(): string {
    return icon;
  }

  validate(): void {
    if (!this.name) {
      throw new Error('名称不能为空');
    }

    if (!this.schedule) {
      throw new Error('schedule不能为空');
    }
  }
}

