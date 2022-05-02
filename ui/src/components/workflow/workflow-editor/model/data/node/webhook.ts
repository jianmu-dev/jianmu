import { BaseNode } from './base-node';
import { ISecretKey } from '../common';
import { NodeTypeEnum, ParamTypeEnum } from '../enumeration';
import icon from '../../../svgs/shape/webhook.svg';

export interface IWebhookParam {
  name: string;
  type: ParamTypeEnum;
  exp: string;
}

export interface IWebhookAuth {
  token: string;
  value: ISecretKey;
}

export class Webhook extends BaseNode {
  readonly params: IWebhookParam[];
  auth: IWebhookAuth | undefined;
  only: string;

  constructor(name: string = 'webhook', params: IWebhookParam[] = [],
    auth: IWebhookAuth | undefined = undefined, only: string = '') {
    super('webhook', name, NodeTypeEnum.WEBHOOK, icon);
    this.params = params;
    this.auth = auth;
    this.only = only;
  }

  static build({ name, params, auth, only }: any): Webhook {
    return new Webhook(name, params, auth, only);
  }

  validate(): void {
    super.validate();

    // TODO 待完善校验规则
  }
}

