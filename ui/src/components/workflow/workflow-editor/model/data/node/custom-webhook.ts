import { BaseNode } from './base-node';
import { NodeRefEnum, NodeTypeEnum } from '../enumeration';
import { ISelectableParam } from '@/components/workflow/workflow-expression-editor/model/data';
import { CustomRule, ParamValueType } from '../common';
import { IWebhookParam } from './webhook';
import { IWebhookOperatorVo } from '@/api/dto/custom-webhook';
import { getWebhookOperators } from '@/api/custom-webhook';

const OFFICIAL_NODE_OWNER_REF = '_';

export const CUSTOM_PARAM_SCOPE = 'trigger';

export interface ICustomWebhookRule {
  key?: string;
  paramRef: string;
  operator: string;
  matchingValue: ParamValueType;
}

export interface ICustomWebhookEvent {
  readonly ref: string;
  readonly name: string;
  readonly availableParams: IWebhookParam[];
  readonly eventRuleset: ICustomWebhookRule[];
}

export interface ICustomWebhookEventInstance {
  ref: string;
  ruleset: ICustomWebhookRule[];
  rulesetOperator: string;
}

export class CustomWebhook extends BaseNode {
  readonly ownerRef: string;
  readonly nodeRef: string;
  readonly events: ICustomWebhookEvent[];
  version: string;
  readonly eventInstances: ICustomWebhookEventInstance[];
  dslText: string;

  constructor(
    ref: string,
    nodeRef: string,
    name: string,
    icon: string,
    ownerRef: string,
    version = '',
    dslText = '',
    events: ICustomWebhookEvent[] = [],
    eventInstances: ICustomWebhookEventInstance[] = [],
  ) {
    super(NodeRefEnum.WEBHOOK, name, NodeTypeEnum.WEBHOOK, icon, '');
    this.ownerRef = ownerRef;
    this.nodeRef = nodeRef;
    this.events = events;
    this.version = version;
    this.eventInstances = eventInstances;
    this.dslText = dslText;
  }

  static build({ ref, nodeRef, name, icon, ownerRef, version, dslText, events, eventInstances }: any): CustomWebhook {
    return new CustomWebhook(ref, nodeRef, name, icon, ownerRef, version, dslText, events, eventInstances);
  }

  async buildSelectableParam(nodeId: string): Promise<ISelectableParam | undefined> {
    if (this.eventInstances.length === 0) {
      return undefined;
    }

    const events = this.eventInstances.map(({ ref }) => this.events.find(event => event.ref === ref)!);
    const arr: IWebhookParam[] = [];
    const refArr: string[] = [];
    // 整合参数 & 参数去重
    events.forEach(({ availableParams }) =>
      availableParams.forEach(item => {
        if (refArr.includes(item.ref)) {
          return;
        }
        arr.push(item);
        refArr.push(item.ref);
      }),
    );

    return {
      value: CUSTOM_PARAM_SCOPE,
      label: `${super.getName()}触发器参数`,
      children: arr
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
    const customWebhookFields: Record<string, CustomRule> = {};
    this.events.forEach((item, index) => {
      const instance = this.eventInstances.find(({ ref }) => ref === item.ref);
      if (!instance) {
        return;
      }
      const fields: Record<string, CustomRule> = {};
      customWebhookFields[index] = {
        type: 'object',
        required: false,
        fields: {
          ruleset: {
            type: 'array',
            required: false,
            len: instance.ruleset.length,
            fields,
          },
          rulesetOperator: [{ required: true, message: '请选择匹配类型', trigger: 'change' }],
        } as Record<string, CustomRule>,
      };
      instance.ruleset.forEach((_item, idx) => {
        fields[idx] = {
          type: 'object',
          required: true,
          fields: {
            paramRef: [{ required: true, message: '请选择参数', trigger: 'change' }],
            matchingValue: [{ required: true, message: '请输入参数值', trigger: 'blur' }],
          } as Record<string, CustomRule>,
        };
      });
    });
    return {
      ...rules,
      version: [
        {
          required: true,
          validator: (rule: any, value: any, callback: any) => {
            if (value) {
              callback();
            }
            if (!this.version) {
              callback('请选择版本');
              return;
            }
            callback();
          },
          trigger: 'change',
        },
      ],
      selectedReference: [
        {
          validator: (rule: any, value: any, callback: any) => {
            if (value) {
              callback();
            }
            if (this.eventInstances.length === 0) {
              callback('请选择一类触发事件');
              return;
            }
            callback();
          },
          trigger: 'change',
        },
      ],
      eventInstances: {
        type: 'array',
        required: false,
        len: this.eventInstances.length,
        fields: customWebhookFields,
      },
    };
  }

  toDsl(): object {
    const { ownerRef, nodeRef, version, eventInstances } = this;
    return {
      webhook: `${ownerRef === OFFICIAL_NODE_OWNER_REF ? '' : `${ownerRef}/`}${nodeRef}@${version}`,
      event:
        eventInstances.length === 0
          ? undefined
          : eventInstances.map(param => {
            const customEvent: any = {
              ref: param.ref,
              ruleset:
                  param.ruleset.length === 0
                    ? undefined
                    : param.ruleset.map(item => {
                      const rule: any = {
                        'param-ref': item.paramRef,
                        operator: item.operator,
                        value: item.matchingValue,
                      };
                      return rule;
                    }),
              'ruleset-operator': param.rulesetOperator,
            };
            return customEvent;
          }),
    };
  }
}

/**
 * 获取规则参数 evens/rule
 */
let webhookOperator: IWebhookOperatorVo;

export async function getWebhookOperator(): Promise<IWebhookOperatorVo> {
  if (!webhookOperator) {
    webhookOperator = await getWebhookOperators();
  }
  return webhookOperator;
}

/**
 * 构造下拉option
 * @param uiEvent
 * @param ref
 */
export function buildSelectableOption(uiEvent: any, ref: string): ISelectableParam | undefined {
  // TODO 判断了是否有uiEvent，但初始情况下可能没有uiEvent

  const options = !uiEvent?.param ? undefined : uiEvent.param[ref]?.option;
  if (!options) {
    return options;
  }

  return {
    value: '',
    label: '动作参数',
    children: options.map(({ value, name }: { value: string | number; name: string }) => {
      return {
        value: value,
        label: name || value,
      };
    }),
  };
}
