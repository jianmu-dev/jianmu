import { BaseVo, IPageDto } from '@/api/dto/common';
import { EventBridgeSourceTypeEnum, EventBridgeTargetTransformerTypeEnum } from '@/api/dto/enumeration';

/**
 * 查询事件桥接器dto
 */
export interface IEventBridgeQueryingDto extends Readonly<IPageDto> {
}

/**
 *  事件桥接器vo
 */
export interface IEventBridgeVo extends Readonly<BaseVo & {
  id: string;
  name: string;
}> {
}

/**
 *  事件桥接器来源vo
 */
export interface IEventBridgeSourceVo extends Readonly<{
  id: string;
  bridgeId: string;
  name: string;
  type: EventBridgeSourceTypeEnum;
}> {
}

/**
 * 事件桥接器目标转换器vo
 */
export interface IEventBridgeTargetTransformerVo extends Readonly<{
  type: EventBridgeTargetTransformerTypeEnum;
  variableName: string;
  variableType: string;
  expression: string;
}> {
}

/**
 * 事件桥接器目标vo
 */
export interface IEventBridgeTargetVo extends Readonly<{
  id: string;
  ref?: string;
  name: string;
  relatedProjectId?: string;
  relatedProjectName?: string;
  transformers: IEventBridgeTargetTransformerVo[];
}> {
}

/**
 *  事件桥接器详情vo
 */
export interface IEventBridgeDetailVo extends Readonly<{
  bridge: IEventBridgeVo;
  source: IEventBridgeSourceVo;
  targets: IEventBridgeTargetVo[]
}> {
}

/**
 * 保存事件桥接器dto
 */
export interface IEventBridgeSavingDto extends Readonly<{
  bridge: Partial<Omit<IEventBridgeVo, 'name'>> & {
    readonly name: string;
  };
  source: Partial<Omit<IEventBridgeSourceVo, 'name' | 'type'>> & {
    readonly name: string;
    readonly type: EventBridgeSourceTypeEnum;
  };
  targets: (Partial<Omit<IEventBridgeTargetVo, 'name' | 'transformers'>> & {
    readonly name: string;
    readonly transformers: IEventBridgeTargetTransformerVo[];
  })[];
}> {
}

/**
 * 目标事件payload vo
 */
export interface ITargetEventPayloadVo {
  query: {
    [key: string]: string[];
  };
  header: {
    [key: string]: string[];
  };
  body: string;
}

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
 * 目标事件vo
 */
export interface ITargetEventVo {
  id: string;
  bridgeId: string;
  sourceId: string;
  sourceEventId: string;
  connectionEventId: string;
  targetId: string;
  targetRef: string;
  destinationId: string;
  payload: ITargetEventPayloadVo;
  eventParameters: IEventParameterVo[];
}