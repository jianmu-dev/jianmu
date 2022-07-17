import { GlobalParam } from './global-param';

export class Global {
  concurrent: boolean;
  readonly params: GlobalParam[];

  constructor(concurrent: boolean = false, params: GlobalParam[] = []) {
    this.concurrent = concurrent;
    this.params = params;
  }

  toDsl(): object {
    const params: Record<string, object> = {};
    this.params.forEach(({ ref, type, value }) =>
      // TODO 适配新版dsl
      (params[ref] = { type, value }));

    return {
      concurrent: this.concurrent,
      params: this.params.length === 0 ? undefined : params,
    };
  }
}