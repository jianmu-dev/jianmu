/**
 * 可选参数
 */
export interface ISelectableParam {
  readonly value: string;
  readonly label: string;
  readonly children?: ISelectableParam[];
}