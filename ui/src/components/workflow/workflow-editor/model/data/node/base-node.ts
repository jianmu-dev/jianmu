import Schema from 'async-validator';
import { IWorkflowNode } from '../common';
import { NodeTypeEnum } from '../enumeration';
import { Value } from 'async-validator/dist-types/interface';

export abstract class BaseNode implements IWorkflowNode {
  private readonly ref: string;
  name: string;
  private readonly type: NodeTypeEnum;
  private readonly icon: string;
  private readonly docUrl: string

  protected constructor(ref: string, name: string,
    type: NodeTypeEnum, icon: string, docUrl: string) {
    this.ref = ref;
    this.name = name;
    this.type = type;
    this.icon = icon;
    this.docUrl = docUrl;
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

  getDocUrl(): string {
    return this.docUrl;
  }

  getFormRules(): any {
    return {
      name: [
        { required: true, message: '节点名称不能为空', trigger: 'blur' },
      ],
    };
  }

  async validate(): Promise<void> {
    const validator = new Schema(this.getFormRules());

    const source: Record<string, Value> = {};
    Object.keys(this).forEach(key => (source[key] = (this as any)[key]));

    await validator.validate(source);
  }

  toDsl(): object {
    return {};
  }
}