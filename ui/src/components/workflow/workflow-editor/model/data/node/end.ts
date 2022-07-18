import { BaseNode } from './base-node';
import { NodeRefEnum, NodeTypeEnum } from '../enumeration';
import icon from '../../../svgs/shape/end.svg';

export class End extends BaseNode {
  constructor() {
    super(NodeRefEnum.END, '结束', NodeTypeEnum.END, icon, '');
  }

  static build(): End {
    return new End();
  }

  toDsl(): object {
    return {
      alias: super.getName(),
      type: super.getType(),
    };
  }
}