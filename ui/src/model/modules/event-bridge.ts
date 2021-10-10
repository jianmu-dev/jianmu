import { Mutable } from '@/utils/lib';
import { IEventBridgeQueryingDto, IEventBridgeSavingDto, IEventBridgeTargetTransformerVo, IEventBridgeTargetVo } from '@/api/dto/event-bridge';

/**
 * 查询表单
 */
export interface IQueryForm extends Mutable<IEventBridgeQueryingDto> {
}

/**
 * 保存桥接器子表单
 */
export interface ISaveBridgeSubForm extends Mutable<IEventBridgeSavingDto['bridge']> {
}

/**
 * 保存来源子表单
 */
export interface ISaveSourceSubForm extends Mutable<IEventBridgeSavingDto['source']> {
}

/**
 * 保存目标子表单
 */
export interface ISaveTargetsSubForm {
  targets: (Mutable<Partial<Omit<IEventBridgeTargetVo, 'name' | 'transformers'>>> & {
    name: string;
    transformers: IEventBridgeTargetTransformerVo[];
  })[];
}

/**
 * 子表单枚举
 */
export enum SubFormTypeEnum {
  BRIDGE,
  SOURCE,
  TARGET,
}