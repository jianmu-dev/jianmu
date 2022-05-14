export interface IParamReference {
  ref: string;
  nodeId: string;
  inner: boolean;
  // 格式：${xxx.[inner.]xxx}
  raw: string;
}

export interface IParam extends IParamReference {
  name: string;
  nodeName: string;
}

export interface ISelectableParam {
  readonly value: string;
  readonly label: string;
  readonly children?: ISelectableParam[];
}