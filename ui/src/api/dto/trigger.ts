import { TriggerTypeEnum } from '@/api/dto/enumeration';

/**
 * 事件参数vo
 */
export interface IEventParameterVo {
  parameterId: string;
  name: string;
  type: string;
  value: string;
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
export interface ITriggerWebhookVo extends Readonly<{
  webhook: string;
}> {
}