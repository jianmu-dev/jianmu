import { IParamReference } from './data';

/**
 * 节点错误
 */
export class NodeError extends Error {
  readonly reference: IParamReference;

  constructor(reference: IParamReference) {
    super(`${reference.raw}参数不可引用或对应的节点不存在`);
    this.name = 'NodeError';
    this.reference = reference;
  }
}

/**
 * 参数错误
 */
export class ParamError extends Error {
  readonly reference: IParamReference;

  constructor(reference: IParamReference) {
    super(`${reference.raw}参数不可引用或不存在`);
    this.name = 'ParamError';
    this.reference = reference;
  }
}