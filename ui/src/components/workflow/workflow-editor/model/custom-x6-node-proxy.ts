import { Node } from '@antv/x6';
import { INodeData } from './data';
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

  static plainObject(data: INodeData): any {
    const obj = JSON.parse(JSON.stringify(data));
    obj.type = data.getType();

    return obj;
  }

  getData(): INodeData {
    const plainObj = this.node.getData<any>();
    let nodeData: INodeData;

    switch (plainObj.type) {
      case NodeTypeEnum.CRON:
        nodeData = Cron.build(plainObj);
        break;
      case NodeTypeEnum.WEBHOOK:
        nodeData = Webhook.build(plainObj);
        break;
      case NodeTypeEnum.SHELL:
        nodeData = Shell.build(plainObj);
        break;
      case NodeTypeEnum.ASYNC_TASK:
        nodeData = AsyncTask.build(plainObj);
        break;
    }

    return nodeData!;
  }
}