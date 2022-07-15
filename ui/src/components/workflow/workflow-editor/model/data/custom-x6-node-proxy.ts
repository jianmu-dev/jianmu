import { Graph, Node } from '@antv/x6';
import { IWorkflowNode } from './common';
import { NodeTypeEnum } from './enumeration';
import { Cron } from './node/cron';
import { Webhook } from './node/webhook';
import { Shell } from './node/shell';
import { AsyncTask } from './node/async-task';
import { ISelectableParam } from '../../../workflow-expression-editor/model/data';
import { Start } from './node/start';
import { End } from './node/end';

export class CustomX6NodeProxy {
  private readonly node: Node;

  constructor(node: Node) {
    this.node = node;
  }

  isSingle(): boolean {
    return this.isStart() || this.isEnd() || this.isTrigger();
  }

  isStart(): boolean {
    const { type } = JSON.parse(this.node.getData<string>());
    return [NodeTypeEnum.START].includes(type);
  }

  isEnd(): boolean {
    const { type } = JSON.parse(this.node.getData<string>());
    return [NodeTypeEnum.END].includes(type);
  }

  isTrigger(): boolean {
    const { type } = JSON.parse(this.node.getData<string>());
    return [NodeTypeEnum.CRON, NodeTypeEnum.WEBHOOK].includes(type);
  }

  getData(graph?: Graph): IWorkflowNode {
    const obj = JSON.parse(this.node.getData<string>());
    let nodeData: IWorkflowNode;

    switch (obj.type) {
      case NodeTypeEnum.CRON:
        nodeData = Cron.build(obj);
        break;
      case NodeTypeEnum.WEBHOOK:
        nodeData = Webhook.build(obj);
        break;
      case NodeTypeEnum.SHELL:
        nodeData = Shell.build(obj);
        break;
      case NodeTypeEnum.ASYNC_TASK:
        nodeData = AsyncTask.build(obj);
        break;
      case NodeTypeEnum.START:
        nodeData = Start.build();
        break;
      case NodeTypeEnum.END:
        nodeData = End.build();
        break;
    }

    return nodeData!;
  }

  setData(data: IWorkflowNode): void {
    this.node.setData(JSON.stringify(data), {
      // 必须覆盖，否则出错
      overwrite: true,
    });

    // TODO 校验节点，同步节点警告状态
  }

  getSelectableParams(graph: Graph): ISelectableParam[] {
    let graphNode = this.node;
    let workflowNode = new CustomX6NodeProxy(graphNode).getData();
    const params: ISelectableParam[] = [];
    if (workflowNode.getType() === NodeTypeEnum.CRON) {
      return params;
    }
    if (workflowNode.getType() === NodeTypeEnum.WEBHOOK) {
      const param = workflowNode.buildSelectableParam(graphNode.id);
      if (!param || !param.children || param.children.length === 0) {
        return params;
      }
      params.push(param);
      return params;
    }

    // eslint-disable-next-line no-constant-condition
    while (true) {
      const edges = graph.getIncomingEdges(graphNode);
      if (!edges) {
        break;
      }
      graphNode = edges[0].getSourceNode()!;
      workflowNode = new CustomX6NodeProxy(graphNode).getData();
      const param = workflowNode.buildSelectableParam(graphNode.id);
      if (!param || !param.children || param.children.length === 0) {
        continue;
      }
      params.push(param);
    }
    return params;
  }
}
