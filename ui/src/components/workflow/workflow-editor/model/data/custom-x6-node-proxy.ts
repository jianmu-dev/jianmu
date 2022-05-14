import { Graph, Node } from '@antv/x6';
import { IWorkflowNode } from './common';
import { NodeTypeEnum } from './enumeration';
import { Cron } from './node/cron';
import { Webhook } from './node/webhook';
import { Shell } from './node/shell';
import { AsyncTask } from './node/async-task';
import { ISelectableParam } from '../../../workflow-expression-editor/model/data';

export class CustomX6NodeProxy {
  private readonly node: Node;

  constructor(node: Node) {
    this.node = node;
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
        nodeData = AsyncTask.build(obj, graph ? () => this.getSelectableParams(graph) : undefined);
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

  private getSelectableParams(graph: Graph): ISelectableParam[] {
    // TODO 完善获取上游节点列表
    const nodes: Node[] = graph.getNodes();

    const params: ISelectableParam[] = [];
    for (const node of nodes) {
      const workflowNode = new CustomX6NodeProxy(node).getData();

      const param = workflowNode.buildSelectableParam();
      if (!param) {
        continue;
      }

      params.push(param);
    }

    return params;
  }
}