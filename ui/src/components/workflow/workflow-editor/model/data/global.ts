import { CustomRule } from './common';
import Schema, { Value } from 'async-validator';
import { ParamTypeEnum, RefTypeEnum } from './enumeration';
import { checkDuplicate } from '../util/reference';
import { ISelectableParam } from '@/components/workflow/workflow-expression-editor/model/data';
import { IExternalParameterVo } from '@/api/dto/ext-param';

export const GLOBAL_PARAM_SCOPE = 'global';
export const EXT_PARAM_SCOPE = 'ext';

interface IGlobalParam {
  ref: string;
  name: string;
  type: ParamTypeEnum;
  required: boolean;
  value: string;
  hidden: boolean;
}

export class Global {
  concurrent: boolean | number;
  readonly params: IGlobalParam[];

  constructor(concurrent: boolean | number = 1, params: IGlobalParam[] = []) {
    this.concurrent = concurrent;
    this.params = params.map(param => {
      if (param.hidden === undefined) {
        param.hidden = false;
      }
      return param;
    });
  }

  /**
   * 表单校验规则
   */
  getFormRules(): Record<string, CustomRule> {
    const globalParamFields: Record<string, CustomRule> = {};
    this.params.forEach((_, index) => {
      globalParamFields[index] = {
        type: 'object',
        required: true,
        fields: {
          ref: [
            { required: true, message: '请输入唯一标识', trigger: 'blur' },
            {
              pattern: /^[a-zA-Z_]([a-zA-Z0-9_]+)?$/,
              message: '以英文字母或下划线开头，支持下划线、数字、英文字母',
              trigger: 'blur',
            },
            {
              validator: (rule: any, value: any, callback: any) => {
                if (!value) {
                  callback();
                  return;
                }
                try {
                  checkDuplicate(
                    this.params.map(({ ref }) => ref),
                    RefTypeEnum.GLOBAL_PARAM,
                  );
                } catch ({ message, ref }) {
                  if (ref === value) {
                    callback(message);
                    return;
                  }
                }
                callback();
              },
              trigger: 'blur',
            },
          ],
          type: [{ required: true, trigger: 'change' }],
          required: [{ required: true, trigger: 'change', type: 'boolean' }],
          value: [{ required: true, message: '请输入值', trigger: 'blur' }],
          hidden: [{ required: true, trigger: 'change', type: 'boolean' }],
        } as Record<string, CustomRule>,
      };
    });

    return {
      params: {
        type: 'array',
        required: false,
        len: this.params.length,
        fields: globalParamFields,
      },
    };
  }

  async validateParams(): Promise<void> {
    const { params } = this.getFormRules();
    const validator = new Schema({ params });
    const source: Record<string, Value> = {
      params: this.params,
    };

    await validator.validate(source, {
      first: true,
    });
  }

  async validate(): Promise<void> {
    const validator = new Schema(this.getFormRules());
    const source: Record<string, Value> = {};
    Object.keys(this).forEach(key => (source[key] = (this as any)[key]));

    await validator.validate(source, {
      first: true,
    });
  }

  toDsl(): object {
    const { concurrent, params } = this;
    return {
      concurrent,
      param:
        params.length === 0
          ? undefined
          : params.map(param => ({
            ...param,
            hidden: param.hidden || undefined,
          })),
    };
  }
}

/**
 * globalParam参数选择/级联选择
 * @param globalParams
 */
export function buildSelectableGlobalParam(globalParams?: IGlobalParam[]): ISelectableParam | undefined {
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
