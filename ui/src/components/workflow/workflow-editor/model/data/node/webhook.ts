import { BaseNode } from './base-node';
import { CustomRule } from '../common';
import { NodeRefEnum, NodeTypeEnum, ParamTypeEnum, RefTypeEnum } from '../enumeration';
import icon from '../../../svgs/shape/webhook.svg';
import { ISelectableParam } from '../../../../workflow-expression-editor/model/data';
import { checkDuplicate } from '../../util/reference';

export const WEBHOOK_PARAM_SCOPE = 'trigger';

export interface IWebhookParam {
  key: string;
  ref: string;
  name: string;
  type: ParamTypeEnum | undefined;
  value: string;
  required: boolean;
  hidden: boolean;
}

export interface IWebhookAuth {
  token: string;
  value: string;
}

export class Webhook extends BaseNode {
  readonly params: IWebhookParam[];
  auth?: IWebhookAuth;
  only?: string;

  constructor(name: string = 'webhook', params: IWebhookParam[] = [],
    auth: IWebhookAuth | undefined = undefined, only: string | undefined = undefined) {
    super(NodeRefEnum.WEBHOOK, name, NodeTypeEnum.WEBHOOK, icon, 'https://docs.jianmu.dev/guide/webhook.html');
    this.params = params.map(param => {
      if (param.hidden === undefined) {
        param.hidden = false;
      }
      return param;
    });
    this.auth = auth;
    this.only = only;
  }

  static build({ name, params, auth, only }: any): Webhook {
    return new Webhook(name, params, auth, only);
  }

  async buildSelectableParam(nodeId: string = ''): Promise<ISelectableParam | undefined> {
    if (this.params.length === 0) {
      return undefined;
    }

    return {
      value: WEBHOOK_PARAM_SCOPE,
      label: '触发器参数',
      children: this.params
        .filter(({ ref }) => ref)
        .map(({ ref, type, name }) => {
          return {
            value: ref,
            type,
            label: name || ref,
          };
        }),
    };
  }

  getFormRules(): Record<string, CustomRule> {
    const rules = super.getFormRules();

    const webhookParamFields: Record<string, CustomRule> = {};
    this.params.forEach((_, index) => {
      webhookParamFields[index] = {
        type: 'object',
        required: true,
        fields: {
          ref: [
            { required: true, message: '请输入参数唯一标识', trigger: 'blur' },
            {
              validator: (rule: any, value: any, callback: any) => {
                if (!value) {
                  callback();
                  return;
                }
                try {
                  checkDuplicate(this.params.map(({ ref }) => ref), RefTypeEnum.TRIGGER_PARAM);
                } catch ({ message, ref }) {
                  if (ref === value) {
                    callback(message);
                    return;
                  }
                }
                callback();
              },
              trigger: 'blur',
            },
          ],
          name: [{ required: false, message: '请输入参数名称', trigger: 'blur' }],
          type: [{ required: true, message: '请选择参数类型', trigger: 'change' }],
          value: [{ required: true, message: '请输入参数值', trigger: 'blur' }],
          required: [{ required: true, type: 'boolean' }],
          hidden: [{ required: true, type: 'boolean' }],
        } as Record<string, CustomRule>,
      };
    });

    return {
      ...rules,
      params: {
        type: 'array',
        required: false,
        len: this.params.length,
        fields: webhookParamFields,
      },
      auth: {
        type: 'object',
        required: false,
        fields: {
          token: [
            { required: true, message: '请输入token值', trigger: 'blur' },
          ],
          value: [{ required: true, message: '请选择密钥', trigger: 'change' }],
        } as Record<string, CustomRule>,
      },
    };
  }

  toDsl(): object {
    const { params, auth, only } = this;

    return {
      type: NodeTypeEnum.WEBHOOK,
      param: params.length === 0 ? undefined : params.map(param => {
        const newParam: any = {
          ...param,
          hidden: param.hidden || undefined,
        };
        delete newParam.key;
        return newParam;
      }),
      auth,
      only: only ? only : undefined,
    };
  }
}

