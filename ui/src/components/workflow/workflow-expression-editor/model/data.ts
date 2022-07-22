/**
 * 可选参数
 */
import { ParamTypeEnum } from '../../workflow-editor/model/data/enumeration';

export interface ISelectableParam {
  readonly value: string;
  readonly type?: ParamTypeEnum
  readonly label: string;
  readonly children?: ISelectableParam[];
}