import { ParamTypeEnum } from './enumeration';
import { CustomRule } from '@/components/workflow/workflow-editor/model/data/common';
import Schema, { Value } from 'async-validator';
import { ISelectableParam } from '@/components/workflow/workflow-expression-editor/model/data';
import { IExternalParameterVo } from '@/api/dto/ext-param';

export const GLOBAL_PARAM_SCOPE = 'global';
export const EXT_PARAM_SCOPE = 'ext';

export class GlobalParam {
  ref: string;
  name: string;
  type: ParamTypeEnum;
  required: boolean;
  value: string;

  constructor(ref: string, name: string, type: ParamTypeEnum, required: boolean, value: string) {
    this.ref = ref;
    this.name = name;
    this.type = type;
    this.required = required;
    this.value = value;
  }

  static build({ ref, name, type, required, value }: any): GlobalParam {
    return new GlobalParam(ref, name, type, required, value);
  }

  /**
   * 表单校验规则
   */
  getFormRules(): Record<string, CustomRule> {
    return {
      ref: [
        { required: true, message: '请输入唯一标识', trigger: 'blur' },
        { pattern: /^[a-zA-Z_]([a-zA-Z0-9_]+)?$/, message: '以英文字母或下划线开头，支持下划线、数字、英文字母', trigger: 'blur' },
      ],
      type: [{ required: true, trigger: 'change' }],
      required: [{ required: true, trigger: 'change', type: 'boolean' }],
      value: [{ required: true, message: '请输入值', trigger: 'blur' }],
    };
  }

  async validate(): Promise<void> {
    const validator = new Schema(this.getFormRules());
    const source: Record<string, Value> = {};
    Object.keys(this).forEach(key => (source[key] = (this as any)[key]));

    await validator.validate(source, {
      first: true,
    });
  }

}

/**
 * globalParam参数选择/级联选择
 * @param globalParams
 */
export function buildSelectableGlobalParam(globalParams?: GlobalParam[]): ISelectableParam | undefined {
  if (!globalParams || globalParams.length === 0) {
    return undefined;
  }
  return {
    value: GLOBAL_PARAM_SCOPE,
    label: '全局参数',
    children: globalParams
      .filter(({ ref }) => ref)
      .map(({ ref, type, name }) => {
        return {
          value: ref,
          type,
          label: name || ref,
        };
      }),
  };
}

export function buildSelectableExtParam(extParams: IExternalParameterVo[]): ISelectableParam | undefined {
  if (!extParams || extParams.length === 0) {
    return undefined;
  }
  return {
    value: EXT_PARAM_SCOPE,
    label: '外部参数',
    children: extParams
      .filter(({ ref }) => ref)
      .map(({ ref, type, name }) => {
        return {
          value: ref,
          type,
          label: name || ref,
        };
      }),
  };
}