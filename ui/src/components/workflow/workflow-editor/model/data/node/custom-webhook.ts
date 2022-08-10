import { BaseNode } from './base-node';
import { NodeRefEnum, NodeTypeEnum } from '../enumeration';
import { ISelectableParam } from '@/components/workflow/workflow-expression-editor/model/data';
import { CustomRule } from '../common';
import { IWebhookParam } from './webhook';

const OFFICIAL_NODE_OWNER_REF = '_';

export const CUSTOM_PARAM_SCOPE = 'custom_trigger';

export interface ICustomWebhookRule {
  key?: string;
  paramRef: string;
  operator: string;
  matchingValue: string | number | boolean;
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

  constructor(ref: string, nodeRef: string, name: string, icon: string, ownerRef: string, version: string = '',
    events: ICustomWebhookEvent[] = [], eventInstances: ICustomWebhookEventInstance[] = []) {
    super(NodeRefEnum.WEBHOOK, name, NodeTypeEnum.WEBHOOK, icon, '');
    this.ownerRef = ownerRef;
    this.nodeRef = nodeRef;
    this.events = events;
    this.version = version;
    this.eventInstances = eventInstances;
  }

  static build({ ref, nodeRef, name, icon, ownerRef, version, events, eventInstances }: any): CustomWebhook {
    return new CustomWebhook(ref, nodeRef, name, icon, ownerRef, version, events, eventInstances);
  }

  async buildSelectableParam(nodeId: string): Promise<ISelectableParam | undefined> {
    if (this.events.length === 0) {
      return undefined;
    }

    const arr: any[] = [];
    const refArr: string[] = [];
    // 整合参数 & 参数去重
    this.events.forEach(item => {
      item.availableParams.forEach(_item => {
        if (!refArr.includes(_item.ref)) {
          arr.push(_item);
          refArr.push(_item.ref);
        }
      });
    });

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
    // TODO 待完善
    return {
      ...rules,
    };
  }

  toDsl(): object {
    const { ownerRef, nodeRef, version, eventInstances } = this;
    return {
      webhook: `${ownerRef === OFFICIAL_NODE_OWNER_REF ? '' : `${ownerRef}/`}${nodeRef}@${version}`,
      event: eventInstances.length === 0 ? undefined : eventInstances.map(param => {
        const customEvent: any = {
          ref: param.ref,
          ruleset: param.ruleset.length === 0 ? undefined : param.ruleset.map(item => {
            const rule: any = {
              'param-ref': item.paramRef,
              operator: item.operator.toLowerCase(),
              value: item.matchingValue,
            };
            return rule;
          }),
          'ruleset-operator': param.rulesetOperator.toLowerCase(),
        };
        return customEvent;
      }),
    };
  }
}