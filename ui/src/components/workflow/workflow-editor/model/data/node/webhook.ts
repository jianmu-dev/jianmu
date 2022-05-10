import { BaseNode } from './base-node';
import { CustomRule, ISecretKey } from '../common';
import { NodeTypeEnum, ParamTypeEnum } from '../enumeration';
import icon from '../../../svgs/shape/webhook.svg';

export interface IWebhookParam {
  name: string;
  type: ParamTypeEnum;
  exp: string;
  required: boolean;
  default?: string;
}

export interface IWebhookAuth {
  token?: string;
  value?: ISecretKey;
}

export class Webhook extends BaseNode {
  readonly params: IWebhookParam[];
  auth: IWebhookAuth;
  only?: string;

  constructor(name: string = 'webhook', params: IWebhookParam[] = [],
    auth: IWebhookAuth = {}, only: string | undefined = undefined) {
    super('webhook', name, NodeTypeEnum.WEBHOOK, icon, 'https://docs.jianmu.dev/guide/webhook.html');
    this.params = params;
    this.auth = auth;
    this.only = only;
  }

  static build({ name, params, auth, only }: any): Webhook {
    return new Webhook(name, params, auth, only);
  }

  getFormRules(): Record<string, CustomRule> {
    const rules = super.getFormRules();

    return {
      ...rules,
      // TODO 待完善校验规则
      param_name: [],
      param_type: [],
      param_exp: [],
      auth_token: [],
      auth_value: [],
      only: [],
    };
  }

  toDsl(): object {
    const { params, auth, only } = this;

    return {
      type: NodeTypeEnum.WEBHOOK,
      param: params.length === 0 ? undefined : params,
      auth,
      only,
    };
  }
}

