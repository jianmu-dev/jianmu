import Schema, { Value } from 'async-validator';
import { CustomRule, ICache, IGlobal } from './common';
import { checkDuplicate } from '../util/reference';
import { RefTypeEnum } from './enumeration';

export class Global {
  concurrent: boolean | number;
  caches: ICache[];

  constructor(global: IGlobal) {
    this.concurrent = global.concurrent;
    this.caches = global.caches || [];
  }

  /**
   * 表单校验规则
   */
  getCacheFormRules(): Record<string, CustomRule> {
    const cachesFields: Record<string, CustomRule> = {};
    this.caches.forEach((_, index) => {
      cachesFields[index] = {
        type: 'object',
        required: true,
        fields: {
          ref: [
            { required: true, message: '请输入缓存唯一标识', trigger: 'blur' },
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
                    this.caches.map(({ ref }) => ref),
                    RefTypeEnum.CACHE,
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
        } as Record<string, CustomRule>,
      };
    });

    return {
      caches: {
        type: 'array',
        required: false,
        len: this.caches.length,
        fields: cachesFields,
      },
    };
  }

  /**
   * cache校验顶部图标
   */
  async validateCache(): Promise<void> {
    const { caches } = this.getCacheFormRules();
    const validator = new Schema({ caches });
    // 区分数据结构构建数据，避免状态错误
    if (!this.caches[0].ref) {
      const arr: any = [];
      this.caches.forEach(item => {
        arr.push({ ref: item });
      });
      this.caches = arr;
    }
    const source: Record<string, Value> = {
      caches: this.caches,
    };

    await validator.validate(source, {
      first: true,
    });
  }
}
