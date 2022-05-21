/**
 * 参数引用
 */
export interface IParamReference {
  ref: string;
  nodeId: string;
  inner: boolean;
  // 格式：${xxx.[inner.]xxx}
  raw: string;
}

/**
 * 参数
 */
export interface IParam extends IParamReference {
  name: string;
  nodeName: string;
}

/**
 * 可选参数
 */
export interface ISelectableParam {
  readonly value: string;
  readonly label: string;
  readonly children?: ISelectableParam[];
}

/**
 * 显示内容大小
 */
export interface IContentSize {
  width: number;
  height: number;
  multiline: boolean;
}