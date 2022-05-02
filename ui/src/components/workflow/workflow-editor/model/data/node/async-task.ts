import { BaseNode } from './base-node';
import { FailureModeEnum, NodeTypeEnum, ParamTypeEnum } from '../enumeration';

export type ParamValueType = string | number | boolean;

export interface IAsyncTaskParam {
  ref: string;
  name: string;
  type: ParamTypeEnum;
  required: boolean;
  value: ParamValueType;
  description?: string;
}

export class AsyncTask extends BaseNode {
  version: string;
  readonly inputs: IAsyncTaskParam[];
  readonly outputs: IAsyncTaskParam[];
  failureMode: FailureModeEnum;

  constructor(ref: string, name: string, icon: string = '', version: string = '',
    inputs: IAsyncTaskParam[] = [], outputs: IAsyncTaskParam[] = [],
    failureMode: FailureModeEnum = FailureModeEnum.SUSPEND) {
    super(ref, name, NodeTypeEnum.ASYNC_TASK, icon);
    this.version = version;
    this.inputs = inputs;
    this.outputs = outputs;
    this.failureMode = failureMode;
  }

  static build({ ref, name, icon, version, inputs, outputs, failureMode }: any): AsyncTask {
    return new AsyncTask(ref, name, icon, version, inputs, outputs, failureMode);
  }

  validate(): void {
    super.validate();

    // TODO 待完善校验规则
  }
}

