import { IWorkflowNode } from '../common';
import { FailureModeEnum, NodeTypeEnum, ParamTypeEnum } from '../enumeration';
import defaultIcon from '../../../svgs/shape/async-task.svg';

export type ParamValueType = string | number | boolean;

export interface IAsyncTaskParam {
  ref: string;
  name: string;
  type: ParamTypeEnum;
  required: boolean;
  value: ParamValueType;
  description?: string;
}

export class AsyncTask implements IWorkflowNode {
  readonly ref: string;
  name: string;
  readonly icon: string;
  version: string;
  readonly inputs: IAsyncTaskParam[];
  readonly outputs: IAsyncTaskParam[];
  failureMode: FailureModeEnum;

  constructor(ref: string, name: string, icon: string = '', version: string = '',
    inputs: IAsyncTaskParam[] = [], outputs: IAsyncTaskParam[] = [],
    failureMode: FailureModeEnum = FailureModeEnum.SUSPEND) {
    this.ref = ref;
    this.name = name;
    this.icon = icon;
    this.version = version;
    this.inputs = inputs;
    this.outputs = outputs;
    this.failureMode = failureMode;
  }

  static build({ ref, name, icon, version, inputs, outputs, failureMode }: any): AsyncTask {
    return new AsyncTask(ref, name, icon, version, inputs, outputs, failureMode);
  }

  getName(): string {
    return this.name;
  }

  getType(): NodeTypeEnum {
    return NodeTypeEnum.ASYNC_TASK;
  }

  getIcon(): string {
    return this.icon || defaultIcon;
  }

  validate(): void {
    if (!this.name) {
      throw new Error('名称不能为空');
    }

    // TODO 待完善校验规则
  }
}

