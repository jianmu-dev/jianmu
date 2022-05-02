import { BaseNode } from './base-node';
import { NodeTypeEnum } from '../enumeration';
import icon from '../../../svgs/shape/cron.svg';

export class Cron extends BaseNode {
  schedule: string;

  constructor(name: string = 'cron', schedule: string = '') {
    super('cron', name, NodeTypeEnum.CRON, icon);
    this.schedule = schedule;
  }

  static build({ name, schedule }: any): Cron {
    return new Cron(name, schedule);
  }

  validate(): void {
    super.validate();

    if (!this.schedule) {
      throw new Error('schedule不能为空');
    }
  }
}

