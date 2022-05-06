import { BaseNode } from './base-node';
import { NodeTypeEnum } from '../enumeration';
import icon from '../../../svgs/shape/cron.svg';

export class Cron extends BaseNode {
  schedule: string;

  constructor(name: string = 'cron', schedule: string = '') {
    super('cron', name, NodeTypeEnum.CRON, icon, 'https://docs.jianmu.dev/guide/cron.html');
    this.schedule = schedule;
  }

  static build({ name, schedule }: any): Cron {
    return new Cron(name, schedule);
  }

  getFormRules(): any {
    const rules = super.getFormRules();

    return {
      ...rules,
      schedule: [
        { required: true, message: 'schedule不能为空', trigger: 'blur' },
      ],
    };
  }
}

