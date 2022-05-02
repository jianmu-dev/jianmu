import { IWorkflowNode } from '../common';
import { NodeTypeEnum } from '../enumeration';

export abstract class BaseNode implements IWorkflowNode {
  private readonly ref: string;
  name: string;
  private readonly type: NodeTypeEnum;
  private readonly icon: string;

  protected constructor(ref: string, name: string, type: NodeTypeEnum, icon: string) {
    this.ref = ref;
    this.name = name;
    this.type = type;
    this.icon = icon;
  }

  getRef(): string {
    return this.ref;
  }

  getName(): string {
    return this.name;
  }

  getType(): NodeTypeEnum {
    return this.type;
  }

  getIcon(): string {
    return this.icon;
  }

  validate(): void {
    if (!this.name) {
      throw new Error('名称不能为空');
    }
  }
}