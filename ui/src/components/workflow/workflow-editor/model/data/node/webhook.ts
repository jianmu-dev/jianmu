import { BaseNode } from './base-node';
import { CustomRule } from '../common';
import { NodeRefEnum, NodeTypeEnum, ParamTypeEnum } from '../enumeration';
import icon from '../../../svgs/shape/webhook.svg';
import { extractReferences, getParam } from '../../../../workflow-expression-editor/model/util';
import { ISelectableParam } from '../../../../workflow-expression-editor/model/data';

export const WEBHOOK_PARAM_SCOPE = 'trigger';

export interface IWebhookParam {
  key: string;
  name: string;
  type: ParamTypeEnum | undefined;
  exp: string;
  required: boolean;
  default?: string;
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
    super(NodeRefEnum.WEBHOOK, name, NodeTypeEnum.WEBHOOK, icon, 'https://v2.jianmu.dev/guide/webhook.html');
    this.params = params;
    this.auth = auth;
    this.only = only;
  }

  static build({ name, params, auth, only }: any): Webhook {
    return new Webhook(name, params, auth, only);
  }

  buildSelectableParam(nodeId: string = ''): ISelectableParam | undefined {
    if (this.params.length === 0) {
      return undefined;
    }

    return {
      value: WEBHOOK_PARAM_SCOPE,
      label: super.getName(),
      children: this.params.filter(({ name }) => name)
        .map(({ name }) => {
          return {
            value: name,
            label: name,
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
          name: [{ required: true, message: '请输入参数名称', trigger: 'blur' }],
          type: [{ required: true, message: '请选择参数类型', trigger: 'change' }],
          exp: [{ required: true, message: '请输入参数表达式', trigger: 'blur' }],
          required: [{ required: true, type: 'boolean' }],
          default: [
            {
              validator: ({ fullField }: any, value: any, callback: any) => {
                const param = this.params[fullField!.split('.')[1]];
                if (param.required || !param.type) {
                  callback();
                  return;
                }

                const defaultVal = param.default!;
                switch (param.type) {
                  case ParamTypeEnum.BOOL:
                    if (!['true', 'false'].includes(defaultVal)) {
                      callback('请输入正确的参数默认值');
                      return;
                    }
                    break;
                  case ParamTypeEnum.NUMBER:
                    if (isNaN(parseFloat(defaultVal))) {
                      callback('请输入正确的参数默认值');
                      return;
                    }
                    break;
                }
                callback();
              },
              trigger: 'blur',
            },
          ],
        } as Record<string, CustomRule>,
      };
    });

    const validator = (rule: any, value: any, callback: any) => {
      if (!value) {
        callback();
        return;
      }

      const references = extractReferences(value);
      if (references.length > 0) {
        const param = this.buildSelectableParam();
        const selectableParams = param ? [param] : [];
        for (const reference of references) {
          try {
            // 检查引用的触发器参数是否存在
            getParam(reference, selectableParams);
          } catch ({ message }) {
            callback(new Error(`${reference.raw}触发器参数不存在`));
            return;
          }
        }
      }

      callback();
    };

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
            { validator, trigger: 'blur' },
          ],
          value: [{ required: true, message: '请选择密钥', trigger: 'change' }],
        } as Record<string, CustomRule>,
      },
      only: [{ validator, trigger: 'blur' }],
    };
  }

  toDsl(): object {
    const { params, auth, only } = this;

    return {
      type: NodeTypeEnum.WEBHOOK,
      param: params.length === 0 ? undefined : params.map(param => {
        const newParam: any = { ...param };
        delete newParam.key;
        if (!param.required) {
          const defaultVal = param.default!;
          switch (param.type) {
            case ParamTypeEnum.BOOL:
              switch (defaultVal) {
                case 'true':
                  newParam.default = true;
                  break;
                case 'false':
                  newParam.default = false;
                  break;
              }
              break;
            case ParamTypeEnum.NUMBER:
              newParam.default = parseFloat(defaultVal);
              break;
          }
        }
        return newParam;
      }),
      auth,
      only: only ? only : undefined,
    };
  }
}

