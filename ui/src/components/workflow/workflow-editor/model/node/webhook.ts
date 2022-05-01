import { INodeData } from '../data';
import { NodeTypeEnum } from '../enumeration';
import icon from '../../svgs/shape/webhook.svg';

export class Webhook implements INodeData {
  name: string;
  readonly ref: string;

  constructor(name: string = 'webhook') {
    this.name = name;
    this.ref = 'webhook';
  }

  static build({ name }: any): Webhook {
    return new Webhook(name);
  }

  getName(): string {
    return this.name;
  }

  getRef(): string {
    return this.ref;
  }

  getType(): NodeTypeEnum {
    return NodeTypeEnum.WEBHOOK;
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

