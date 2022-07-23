import { Cell, CellView, Graph, JQuery, Node, Point } from '@antv/x6';
import { CustomX6NodeProxy } from './data/custom-x6-node-proxy';
import nodeWarningIcon from '../svgs/node-warning.svg';
import { IWorkflow } from './data/common';
import { checkDuplicate } from './util/reference';
import { NodeGroupEnum, NodeTypeEnum, RefTypeEnum } from './data/enumeration';
import { AsyncTask } from './data/node/async-task';
import {
  getLocalNodeParams,
  getLocalVersionList,
  getOfficialNodeParams,
  getOfficialVersionList,
} from '@/api/node-library';
import { pushParams } from './workflow-node';

export type ClickNodeWarningCallbackFnType = (nodeId: string) => void;

function isWarning(node: Node): boolean {
  return node.hasTool('button');
}

export class WorkflowValidator {
  private readonly workflowData: IWorkflow;
  private readonly graph: Graph;
  private readonly proxy: any;

  constructor(workflowData: IWorkflow, graph: Graph, proxy: any) {
    this.workflowData = workflowData;
    this.graph = graph;
    this.proxy = proxy;
  }

  addWarning(node: Node, clickNodeWarningCallback: ClickNodeWarningCallbackFnType): void {
    if (isWarning(node)) {
      return;
    }

    node.addTools({
      name: 'button',
      args: {
        markup: [
          {
            tagName: 'image',
            attrs: {
              width: 24,
              height: 24,
              'xlink:href': nodeWarningIcon,
              cursor: 'pointer',
            },
          },
        ],
        x: '100%',
        y: 0,
        offset: { x: -13, y: -11 },
        onClick: ({ cell: { id } }: { e: JQuery.MouseDownEvent, cell: Cell, view: CellView }) =>
          clickNodeWarningCallback(id),
      },
    });
  }

  removeWarning(node: Node): void {
    if (!isWarning(node)) {
      return;
    }

    node.removeTool('button');
  }

  async checkInitializingNode(node: Node, clickNodeWarningCallback: ClickNodeWarningCallbackFnType): Promise<void> {
    const proxy = new CustomX6NodeProxy(node);
    const _data = proxy.getData();
    if (_data.getType() !== NodeTypeEnum.ASYNC_TASK) {
      _data
        .validate()
        // 校验节点有误时，加警告
        .catch(() => this.addWarning(node, clickNodeWarningCallback));
      return;
    }
    const data = _data as AsyncTask;
    const isOwnerRef = data.ownerRef === NodeGroupEnum.LOCAL;
    const res = await (isOwnerRef ? getLocalVersionList : getOfficialVersionList)(data.nodeRef, data.ownerRef);
    data.version = res.versions.length > 0 ? res.versions[0] : '';
    if (isOwnerRef) {
      const {
        inputParameters: inputs,
        outputParameters: outputs,
        description: versionDescription,
      } = await getLocalNodeParams(data.nodeRef, data.ownerRef, data.version);
      pushParams(data, inputs, outputs, versionDescription);
    } else {
      const {
        inputParams: inputs,
        outputParams: outputs,
        description: versionDescription,
      } = await getOfficialNodeParams(data.nodeRef, data.ownerRef, data.version);
      pushParams(data, inputs, outputs, versionDescription);
    }
    // fix: #I5DXPM
    proxy.setData(data);
    data
      .validate()
      // 校验节点有误时，加警告
      .catch(() => this.addWarning(node, clickNodeWarningCallback));
  }

  async check(): Promise<void> {
    await this.checkGlobalParams();
    await this.checkNodes();
  }

  /**
   * 校验全局参数
   * @private
   */
  private async checkGlobalParams(): Promise<void> {
    const refs: string[] = [];

    // 验证参数
    for (const globalParam of this.workflowData.global.params) {
      refs.push(globalParam.ref);

      try {
        await globalParam.validate();
      } catch ({ errors }) {
        throw new Error(`全局参数${globalParam.name || globalParam.ref}：${errors[0].message}`);
      }
    }
    // 验证ref是否重复
    checkDuplicate(refs, RefTypeEnum.GLOBAL_PARAM);
  }

  /**
   * 校验所有节点
   * @throws 尚未通过校验时，抛异常
   */
  private async checkNodes(): Promise<void> {
    const proxies = this.graph.getNodes().map(node => new CustomX6NodeProxy(node));

    if (!proxies.find(proxy => proxy.isTask())) {
      throw new Error('至少有一个shell或任务节点');
    }

    if (!proxies.find(proxy => proxy.isStart())) {
      throw new Error('必须有一个开始节点');
    }

    if (!proxies.find(proxy => proxy.isEnd())) {
      throw new Error('必须有一个结束节点');
    }

    const refs: string[] = [];

    for (const proxy of proxies) {
      const data = proxy.getData(this.graph);
      refs.push(data.getRef());

      if (proxy.isTrigger()) {
        // 触发器：只能连到开始
        if (!this.graph.getOutgoingEdges(proxy.node)) {
          throw new Error(`${data.getName()}节点：必须连接到开始节点`);
        }

        try {
          await data.validate();
        } catch ({ errors }) {
          throw new Error(`${data.getName()}节点：${errors[0].message}`);
        }

        continue;
      }

      if (proxy.isTask()) {
        // 任务节点：可连接任何任务节点和结束
        if (!this.graph.getIncomingEdges(proxy.node)) {
          throw new Error(`${data.getName()}节点：不存在上游节点`);
        }

        if (!this.graph.getOutgoingEdges(proxy.node)) {
          throw new Error(`${data.getName()}节点：不存在下游节点`);
        }

        try {
          await data.validate();
        } catch ({ errors }) {
          throw new Error(`${data.getName()}节点：${errors[0].message}`);
        }

        continue;
      }
    }

    checkDuplicate(refs, RefTypeEnum.NODE);
  }

  checkDroppingNode(node: Node, mousePosition: Point.PointLike, nodePanelRect: DOMRect): boolean {
    if (!this.checkDroppingPosition(mousePosition, nodePanelRect)) {
      return false;
    }

    return this.checkSingle(node);
  }

  private checkDroppingPosition(mousePosition: Point.PointLike, nodePanelRect: DOMRect): boolean {
    const { x: mousePosX, y: mousePosY } = mousePosition;
    const { x, y, width, height } = nodePanelRect;
    const maxX = x + width;
    const maxY = y + height;

    if (mousePosX >= x && mousePosX <= maxX &&
      mousePosY >= y && mousePosY <= maxY) {
      // 在节点面板中拖放时，失败
      return false;
    }

    return true;
  }

  private checkSingle(droppingNode: Node): boolean {
    const droppingNodeProxy = new CustomX6NodeProxy(droppingNode);
    if (!droppingNodeProxy.isSingle()) {
      // 非必须单个节点时，忽略
      return true;
    }
    // 表示当前拖放的节点为trigger或单个节点
    const isTrigger = droppingNodeProxy.isTrigger();
    const currentSingleNodeProxy = this.graph.getNodes()
      .map(node => new CustomX6NodeProxy(node))
      .find(proxy => isTrigger ? proxy.isTrigger() : (droppingNodeProxy.getData().getType() === proxy.getData().getType()));

    if (currentSingleNodeProxy) {
      const nodeName = isTrigger ? '触发器' : currentSingleNodeProxy.getData().getName();
      this.proxy.$warning(`只能有一个${nodeName}节点`);
      return false;
    }

    return true;
  }
}