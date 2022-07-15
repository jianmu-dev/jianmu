import { BaseNode } from './base-node';
import { NodeRefEnum, NodeTypeEnum } from '../enumeration';
import icon from '../../../svgs/shape/start.svg';

export class Start extends BaseNode {
  constructor() {
    super(NodeRefEnum.START, '开始', NodeTypeEnum.START, icon);
  }

  static build(): Start {
    return new Start();
  }
}