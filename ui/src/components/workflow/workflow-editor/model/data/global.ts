import { GlobalParam } from './global-param';

export class Global {
  concurrent: boolean;
  readonly params: GlobalParam[];

  constructor(concurrent: boolean = false, params: GlobalParam[] = []) {
    this.concurrent = concurrent;
    this.params = params.map(param => GlobalParam.build(param));
  }

  toDsl(): object {
    const { concurrent, params } = this;
    return {
      concurrent,
      param: params.length === 0 ? undefined : params,
    };
  }
}