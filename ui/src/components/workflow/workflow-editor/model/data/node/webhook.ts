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

    const webhookFields: Record<string, CustomRule> = {};
    this.params.forEach((_, index) => {
      webhookFields[index] = {
        type: 'object',
        required: true,
        fields: {
          name: [{ required: true, message: '请输入参数名称', trigger: 'blur' }],
          type: [{ required: true, message: '请选择参数类型', trigger: 'change' }],
          exp: [{ required: true, message: '请输入参数表达式', trigger: 'blur' }],
          required: [{ required: true }],
          default: [],
        } as Record<string, CustomRule>,
      };
    });

    return {
      ...rules,
      params: {
        type: 'array',
        required: false,
        len: this.params.length,
        fields: webhookFields,
      },
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

