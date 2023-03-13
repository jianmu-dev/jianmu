import { Graph, Node } from '@antv/x6';
import { IWorkflow, IWorkflowNode } from './common';
import { NodeTypeEnum } from './enumeration';
import { Cron } from './node/cron';
import { Webhook } from './node/webhook';
import { Shell } from './node/shell';
import { AsyncTask } from './node/async-task';
import { ISelectableParam } from '../../../workflow-expression-editor/model/data';
import { extractReferences, getParam } from '../../../workflow-expression-editor/model/util';
import { NodeError, ParamError } from '../../../workflow-expression-editor/model/error';

export class CustomX6NodeProxy {
  private readonly node: Node;

  constructor(node: Node) {
    this.node = node;
  }

  isTrigger(): boolean {
    const { type } = JSON.parse(this.node.getData<string>());
    return [NodeTypeEnum.CRON, NodeTypeEnum.WEBHOOK].includes(type);
  }

  getData(graph?: Graph, workflowData?: IWorkflow): IWorkflowNode {
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
        nodeData = Shell.build(
          obj,
          graph ? (value: string) => this.validateParam(graph, value) : undefined,
          workflowData ? (name: string) => this.validateCache(workflowData, name) : undefined,
        );
        break;
      case NodeTypeEnum.ASYNC_TASK:
        nodeData = AsyncTask.build(
          obj,
          graph ? (value: string) => this.validateParam(graph, value) : undefined,
          workflowData ? (name: string) => this.validateCache(workflowData, name) : undefined,
        );
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

  private validateParam(graph: Graph, value: string) {
    const references = extractReferences(value);
    if (references.length === 0) {
      return;
    }

    const selectableParams = this.getSelectableParams(graph);
    for (const reference of references) {
      try {
        // 检查参数引用对应的节点或参数是否存在
        getParam(reference, selectableParams);
      } catch (err) {
        if (err instanceof NodeError) {
          const cell = graph.getCellById(reference.nodeId);
          if (cell) {
            const workflowNode = new CustomX6NodeProxy(cell as Node).getData();
            const nodeName = workflowNode.getName();
            throw new Error(`${reference.raw}参数不可用，${nodeName}节点参数不可引用`);
          }
          throw err;
        }

        if (err instanceof ParamError) {
          const cell = graph.getCellById(reference.nodeId);
          if (cell) {
            const workflowNode = new CustomX6NodeProxy(cell as Node).getData();
            const nodeName = workflowNode.getName();
            throw new Error(`${reference.raw}参数不可用，${nodeName}节点不存在此参数`);
          }
          throw err;
        }

        throw err;
      }
    }
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
