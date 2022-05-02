import { Node } from '@antv/x6';
import { IWorkflowNode } from './common';
import { NodeTypeEnum } from './enumeration';
import { Cron } from './node/cron';
import { Webhook } from './node/webhook';
import { Shell } from './node/shell';
import { AsyncTask } from './node/async-task';

export class CustomX6NodeProxy {
  private readonly node: Node;

  constructor(node: Node) {
    this.node = node;
  }

  getData(): IWorkflowNode {
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
}