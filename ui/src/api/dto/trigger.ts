import { TriggerTypeEnum, WebhookRequstStateEnum } from '@/api/dto/enumeration';
import { IPageDto } from '@/api/dto/common';

/**
 * 事件参数vo
 */
export interface IEventParameterVo {
  parameterId: string;
  ref: string;
  name: string;
  type: string;
  value: object;
  required: boolean;
}

/**
 * 触发器事件vo
 */
export interface ITriggerEventVo {
  id: string;
  projectId: string;
  triggerId: string;
  triggerType: TriggerTypeEnum;
  payload: string;
  occurredTime: string;
  parameters: IEventParameterVo[];
}

/**
 * 触发器webhook vo
 */
export interface ITriggerWebhookVo
  extends Readonly<{
    webhook: string;
  }> {
}

/**
 * 分页返回Webhook请求列表 dto
 */
export interface ITriggerViewingDto
  extends Readonly<IPageDto & {
    projectId: string;
  }> {
}

/**
 * WebRequest
 */
export interface IWebRequestVo
  extends Readonly<{
    id: string;
    projectId: string;
    workflowRef: string;
    workflowVersion: string;
    triggerId: string;
    userAgent: string;
    payload: string;
    statusCode: WebhookRequstStateEnum;
    errorMsg?: string;
    requestTime: string;
  }> {
}

/**
 * 获取webhook参数
 */
export interface IWebhookParameterVo
  extends Readonly<{
    ref: string;
    name: string;
    type: string;
    value: object;
    required: boolean;
  }> {
}

/**
 * 获取webhookRule参数
 */
export interface IWebhookRuleVo extends Readonly<{
  ruleStr: string,
  succeed: boolean
}> {
}

/**
 * 获取webhookEvent参数
 */
export interface IWebhookEventVo extends Readonly<{
  name: string,
  ruleset: IWebhookRuleVo[]
}> {
}

export interface IWebhookAuthVo
  extends Readonly<{
    token: string;
    value: string;
  }> {
}

export interface IWebhookParamVo
  extends Readonly<{
    param: IWebhookParameterVo[];
    auth: IWebhookAuthVo;
    only: string;
    webhookEvent?: IWebhookEventVo
  }> {
}

/**
 * 获取payload参数
 */
export interface IWebRequestPayloadVo
  extends Readonly<{
    payload: string;
  }> {
}
