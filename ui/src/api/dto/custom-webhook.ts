import { OwnerTypeEnum, ParamTypeEnum } from '@/api/dto/enumeration';

/**
 * webhook事件Vo
 */
export interface IWebhookEventOperatorVo
  extends Readonly<{
    ref: string;
    name: string;
  }> {
}

/**
 * webhook参数Vo
 */
export interface IWebhookParamOperatorVo
  extends Readonly<{
    type: ParamTypeEnum;
    operators: IWebhookEventOperatorVo[];
  }> {
}

/**
 * webhook规则Vo
 */
export interface IWebhookOperatorVo
  extends Readonly<{
    paramOperators: IWebhookParamOperatorVo[];
    rulesetOperators: IWebhookEventOperatorVo[];
  }> {
}


/**
 * 查询webhook列表Vo
 */
export interface IWebhookDefinitionVo
  extends Readonly<{
    id: string;
    ref: string;
    name: string;
    description: string;
    icon: string;
    ownerRef: string;
    ownerName: string;
    ownerType: OwnerTypeEnum;
    creatorRef: string;
    creatorName: string;
  }> {
}

/**
 * webhook参数列表
 */
export interface IWebhookParameter
  extends Readonly<{
    ref: string;
    name: string;
    type: ParamTypeEnum;
    value: string;
    required: boolean;
    hidden: boolean;
  }> {
}

/**
 * 自定义webhook规则
 */
export interface ICustomWebhookRule
  extends Readonly<{
    paramRef: string;
    operator: string;
    matchingValue: string | boolean | number;
  }> {
}

/**
 * Event
 */
export interface IEventVo
  extends Readonly<{
    ref: string;
    name: string;
    availableParams: IWebhookParameter[];
    ruleset: ICustomWebhookRule[];
  }> {
}

/**
 * 查询webhook version list
 */
export interface INodeDefVersionListVo
  extends Readonly<{
    versions: string[];
  }> {
}

/**
 * 获取webhook version 某个版本具体信息
 */
export interface IWebhookDefinitionVersionVo
  extends Readonly<{
    id: string;
    definitionId: string;
    ref: string;
    ownerRef: string;
    version: string;
    creatorRef: string;
    creatorName: string;
    events: IEventVo[];
    dslText: string;
  }> {
}