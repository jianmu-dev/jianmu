import { INodeData, ISecretKey } from '../data';
import { NodeTypeEnum, ParamTypeEnum } from '../enumeration';
import icon from '../../svgs/shape/webhook.svg';

export interface IWebhookParam {
  name: string;
  type: ParamTypeEnum;
  exp: string;
}

export interface IWebhookAuth {
  token: string;
  value: ISecretKey;
}

export class Webhook implements INodeData {
  readonly ref: string = 'webhook';
  name: string;
  readonly params: IWebhookParam[];
  auth: IWebhookAuth | undefined;
  only: string;

  constructor(name: string = 'webhook', params: IWebhookParam[] = [],
    auth: IWebhookAuth | undefined = undefined, only: string = '') {
    this.name = name;
    this.params = params;
    this.auth = auth;
    this.only = only;
  }

  static build({ name, params, auth, only }: any): Webhook {
    return new Webhook(name, params, auth, only);
  }

  getName(): string {
    return this.name;
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

    // TODO 待完善校验规则
  }
}

