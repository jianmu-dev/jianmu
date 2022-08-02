import { isValidCronExpression } from 'cron-expression-validator';
import { BaseNode } from './base-node';
import { NodeRefEnum, NodeTypeEnum } from '../enumeration';
import icon from '../../../svgs/shape/cron.svg';
import { CustomRule } from '../common';

export class Cron extends BaseNode {
  schedule: string;

  constructor(name: string = 'cron', schedule: string = '') {
    super(NodeRefEnum.CRON, name, NodeTypeEnum.CRON, icon, 'https://v2.jianmu.dev/guide/cron.html');
    this.schedule = schedule;
  }

  static build({ name, schedule }: any): Cron {
    return new Cron(name, schedule);
  }

  getFormRules(): Record<string, CustomRule> {
    const rules = super.getFormRules();

    return {
      ...rules,
      schedule: [
        { required: true, message: 'schedule不能为空', trigger: 'blur' },
        {
          validator: (rule: any, value: any, callback: any) => {
            if (!isValidCronExpression(value)) {
              callback(new Error('请输入正确的cron表达式'));
              return;
            }
            callback();
          },
          trigger: 'blur',
        },
      ],
    };
  }

  toDsl(): object {
    const { schedule } = this;

    return {
      type: NodeTypeEnum.CRON,
      schedule,
    };
  }
}

