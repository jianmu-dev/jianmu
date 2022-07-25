import Schema, { Value } from 'async-validator';
import { CustomRule, IWorkflowNode } from '../common';
import { NodeTypeEnum } from '../enumeration';
import { ISelectableParam } from '../../../../workflow-expression-editor/model/data';
import { INNER_PARAM_LABEL, INNER_PARAM_TAG } from '../../../../workflow-expression-editor/model/const';
import { INodeOutputDefinitionVo } from '@/api/dto/node-definitions';
import { getNodeOutputDefinitions } from '@/api/node-library';

export abstract class BaseNode implements IWorkflowNode {
  ref: string;
  name: string;
  private readonly type: NodeTypeEnum;
  private readonly icon: string;
  private readonly docUrl: string;

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

  getDisplayName(): string {
    return this.name || this.ref;
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

  async buildSelectableParam(nodeId: string): Promise<ISelectableParam | undefined> {
    return undefined;
  }

  getFormRules(): Record<string, CustomRule> {
    return {
      ref: [
        {
          required: true,
          message: '节点唯一标识不能为空',
          trigger: 'blur',
        },
      ],
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

  toDsl(): object {
    return {};
  }
}

let nodeOutputDefinitions: INodeOutputDefinitionVo[];

export async function buildSelectableInnerOutputParam(): Promise<ISelectableParam> {
  if (!nodeOutputDefinitions) {
    nodeOutputDefinitions = await getNodeOutputDefinitions();
  }
  return {
    // 文档：https://docs.jianmu.dev/guide/custom-node.html#_4-%E5%86%85%E7%BD%AE%E8%BE%93%E5%87%BA%E5%8F%82%E6%95%B0
    value: INNER_PARAM_TAG,
    label: INNER_PARAM_LABEL,
    children: nodeOutputDefinitions.map(({ ref, type, name }) => {
      return {
        value: ref,
        type: type,
        label: name || ref,
      };
    }),
  };
}