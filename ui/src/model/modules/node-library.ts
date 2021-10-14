import { INodeCreatingDto, INodeVo } from '@/api/dto/node-library';
import { Mutable } from '@/utils/lib';

export interface ICreateNodeForm extends Mutable<INodeCreatingDto> {
}

export interface INode extends INodeVo {
  isDirectionDown?: boolean;
  isSync?: boolean;
  isDel?: boolean;
}
