import { IWorkflowNode } from '../common';
import { NodeTypeEnum } from '../enumeration';
import icon from '../../../svgs/shape/cron.svg';

export class Cron implements IWorkflowNode {
  readonly ref: string = 'cron';
  name: string;
  schedule: string;

  constructor(name: string = 'cron', schedule: string = '') {
    this.name = name;
    this.schedule = schedule;
  }

  static build({ name, schedule }: any): Cron {
    return new Cron(name, schedule);
  }

  getName(): string {
    return this.name;
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

