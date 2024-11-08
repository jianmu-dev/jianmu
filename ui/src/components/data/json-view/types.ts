export enum JsonDataType {
  ARRAY = 'array',
  VALUE = 'value',
  OBJECT = 'object',
}

interface JsonDataBase {
  key: string;
  type: JsonDataType;
  depth: number;
  path: string;
}

export type JsonData = JsonDataArray | JsonDataObject | JsonDataValue;

export interface JsonDataArray extends JsonDataBase {
  type: JsonDataType.ARRAY;
  length: number;
  children: Array<JsonData>;
}

export interface JsonDataObject extends JsonDataBase {
  type: JsonDataType.OBJECT;
  length: number;
  children: Array<JsonData>;
}

export interface JsonDataValue extends JsonDataBase {
  type: JsonDataType.VALUE;
  value?: any;
}

export interface SelectedJsonData {
  key: string;
  value: string;
  path: string;
}

export enum ColorMode {
  LIGHT = 'light',
  DARK = 'dark',
}
