import { GlobalParam } from './global-param';

export class Global {
  concurrent: boolean;
  readonly params: GlobalParam[];

  constructor(concurrent: boolean = false, params: GlobalParam[] = []) {
    this.concurrent = concurrent;
    this.params = params;
  }
}