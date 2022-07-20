enum ParamType {
  STRING = 'STRING',
  BOOL = 'BOOL',
  NUMBER = 'NUMBER',
}

/**
 * 查询外部参数
 */
export interface IExternalParameterVo {
  id: string;
  ref: string;
  name: string;
  type: ParamType;
  value: string;
  label: string;
  createdTime: string;
  lastModifiedTime: string;
}

/**
 * 创建外部参数
 */
export interface IExternalParameterCreatingDto {
  ref: string;
  name?: string;
  type: ParamType;
  value: string;
  label: string;
}

/**
 * 修改外部参数
 */
export interface IExternalParameterUpdatingDto {
  id: string;
  name?: string;
  type: ParamType;
  value: string;
  label: string;
}

/**
 * 获取外部参数标签列表
 */
export interface IExternalParameterLabelVo {
  id: string;
  value: string;
  createdTime: string;
  lastModifiedTime: string;
}