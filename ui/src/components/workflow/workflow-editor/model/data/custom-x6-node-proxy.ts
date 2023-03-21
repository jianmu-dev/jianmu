import { Graph, Node } from '@antv/x6';
import { IWorkflowNode, IWorkflow } from './common';
import { ExpressionTypeEnum, NodeTypeEnum } from './enumeration';
import { Cron } from './node/cron';
import { Webhook } from './node/webhook';
import { Shell } from './node/shell';
import { AsyncTask } from './node/async-task';
import { ISelectableParam } from '../../../workflow-expression-editor/model/data';
import { Start } from './node/start';
import { End } from './node/end';
import { CustomWebhook } from './node/custom-webhook';

export class CustomX6NodeProxy {
  readonly node: Node;

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

  isTask(): boolean {
    const { type } = JSON.parse(this.node.getData<string>());
    return [NodeTypeEnum.ASYNC_TASK, NodeTypeEnum.SHELL].includes(type);
  }

  getData(graph?: Graph, workflowData?: IWorkflow): IWorkflowNode {
    const obj = JSON.parse(this.node.getData<string>());
    let nodeData: IWorkflowNode;

    switch (obj.type) {
      case NodeTypeEnum.CRON:
        nodeData = Cron.build(obj);
        break;
      case NodeTypeEnum.WEBHOOK:
        nodeData = obj.events ? CustomWebhook.build(obj) : Webhook.build(obj);
        break;
      case NodeTypeEnum.SHELL:
        nodeData = Shell.build(
          obj,
          undefined,
          workflowData ? (name: string) => this.validateCache(workflowData, name) : undefined,
        );
        break;
      case NodeTypeEnum.ASYNC_TASK:
        nodeData = AsyncTask.build(
          obj,
          undefined,
          workflowData ? (name: string) => this.validateCache(workflowData, name) : undefined,
        );
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

  async getSelectableParams(
    graph: Graph,
    expType: ExpressionTypeEnum,
    buildSelectableGlobalParam: () => ISelectableParam | undefined,
  ): Promise<ISelectableParam[]> {
    const graphNode = this.node;
    const workflowNode = new CustomX6NodeProxy(graphNode).getData();
    const params: ISelectableParam[] = [];
    if (workflowNode.getType() === NodeTypeEnum.CRON) {
      return params;
    }
    if (workflowNode.getType() === NodeTypeEnum.WEBHOOK) {
      if (expType === ExpressionTypeEnum.WEBHOOK_PARAM) {
        // WEBHOOK_PARAM
        return params;
      }
      // GLOBAL_PARAM、WEBHOOK_TOKEN、WEBHOOK_ONLY
      // 触发器参数
      const param = await workflowNode.buildSelectableParam(graphNode.id);
      if (!param || !param.children || param.children.length === 0) {
        return params;
      }
      params.push(param);
      return params;
    }

    // NODE_INPUT、SHELL_ENV
    // 全局参数
    const globalParam = buildSelectableGlobalParam();
    if (globalParam && globalParam.children && globalParam.children.length > 0) {
      params.push(globalParam);
    }
    // 任务节点参数
    await this.buildSelectableParams(graph, graphNode, params);
    return params;
  }

  private async buildSelectableParams(graph: Graph, node: Node, params: ISelectableParam[]): Promise<void> {
    const edges = graph.getIncomingEdges(node);
    if (!edges) {
      // 标识根节点
      return;
    }

    for (const edge of edges) {
      const sourceNode = edge.getSourceNode()!;
      const param = await new CustomX6NodeProxy(sourceNode).getData().buildSelectableParam(sourceNode.id);

      if (
        param &&
        param.children &&
        param.children.length > 0 &&
        // 去重上游可选重复节点参数
        !params.find(({ value }) => value === param.value)
      ) {
        params.push(param);
      }
      await this.buildSelectableParams(graph, edge.getSourceNode()!, params);
    }
  }

  toDsl(graph: Graph): object {
    if (this.isTrigger()) {
      return this.getData().toDsl();
    }

    const needs: string[] = [];
    graph.getIncomingEdges(this.node)?.forEach(edge => {
      const sourceNode = edge.getSourceNode();
      if (!sourceNode) {
        return;
      }
      const sourceNodeProxy = new CustomX6NodeProxy(sourceNode);
      if (sourceNodeProxy.isTrigger()) {
        return;
      }

      needs.push(sourceNodeProxy.getData().getRef());
    });

    return {
      ...this.getData().toDsl(),
      needs: needs.length > 0 ? needs : undefined,
    };
  }

  private validateCache({ global: { caches } }: IWorkflow, name: string) {
    if (caches && typeof caches === 'string' && caches === name) {
      return;
    }
    if (caches?.map(item => (item.ref ? item.ref : item)).includes(name)) {
      return;
    }
    // throw new Error(`cache不存在${name}`);
    throw new Error('缓存不存在');
  }
}
