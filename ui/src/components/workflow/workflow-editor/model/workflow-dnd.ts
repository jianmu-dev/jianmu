import { Addon, Graph, Node, Point } from '@antv/x6';
// @ts-ignore
import listen from 'good-listener';
import { IWorkflowNode } from './data/common';
import { NODE, PORTS } from '../shape/gengral-config';
import { ClickNodeWarningCallbackFnType, WorkflowValidator } from './workflow-validator';
import { CustomX6NodeProxy } from './data/custom-x6-node-proxy';
import { NodeGroupEnum, NodeTypeEnum } from './data/enumeration';
import {
  getLocalNodeParams,
  getLocalVersionList,
  getOfficialNodeParams,
  getOfficialVersionList,
} from '@/api/node-library';
import { AsyncTask } from './data/node/async-task';
import { pushParams } from './workflow-node';

const { icon: { width, height }, textMaxHeight } = NODE;

interface IDraggingListener {
  mousePosition: Point.PointLike;
  listener?: any;
}

export class WorkflowDnd {
  private readonly graph: Graph;
  private readonly dnd: Addon.Dnd;
  private readonly draggingListener: IDraggingListener = {
    mousePosition: { x: -1, y: -1 },
  }

  constructor(graph: Graph,
    workflowValidator: WorkflowValidator,
    nodeContainer: HTMLElement,
    clickNodeWarningCallback: ClickNodeWarningCallbackFnType) {
    this.graph = graph;
    this.dnd = new Addon.Dnd({
      target: graph,
      animation: true,
      getDragNode: (sourceNode: Node) => {
        const { width, height } = sourceNode.getSize();
        sourceNode.resize(width, height + textMaxHeight);
        // 开始拖拽时初始化的节点，直接使用，无需克隆
        return sourceNode;
      },
      getDropNode: (draggingNode: Node) => {
        const { width, height } = draggingNode.getSize();
        draggingNode.resize(width, height - textMaxHeight);

        // 结束拖拽时，必须克隆拖动的节点，因为拖动的节点和目标节点不在一个画布
        const targetNode = draggingNode.clone();
        // 保证不偏移
        setTimeout(() => {
          const { x, y } = targetNode.getPosition();
          targetNode.setPosition(x, y - textMaxHeight / 2);
        });
        return targetNode;
      },
      validateNode: async (droppingNode: Node) => {
        const { mousePosition } = this.draggingListener;
        // 销毁监听器，必须先获取鼠标位置后销毁
        this.destroyListener();

        const nodePanelRect = nodeContainer.getBoundingClientRect();
        // 验证节点是否有效放置在画布中，true代表成功
        const flag = workflowValidator.checkDroppingNode(droppingNode, mousePosition, nodePanelRect);
        const proxy = new CustomX6NodeProxy(droppingNode);
        const _data = proxy.getData();
        if (!flag) {
          return false;
        }
        if (_data.getType() !== NodeTypeEnum.ASYNC_TASK) {
          _data
            .validate()
            // 校验节点有误时，加警告
            .catch(() => workflowValidator.addWarning(droppingNode, clickNodeWarningCallback));
          return true;
        }
        const data = _data as AsyncTask;
        const isOwnerRef = data.ownerRef === NodeGroupEnum.LOCAL;
        const res = await (isOwnerRef ? getLocalVersionList : getOfficialVersionList)(data.getRef(), data.ownerRef);
        data.version = res.versions.length > 0 ? res.versions[0] : '';
        if (isOwnerRef) {
          const {
            inputParameters: inputs,
            outputParameters: outputs,
            description: versionDescription,
          } = await getLocalNodeParams(data.getRef(), data.ownerRef, data.version);
          pushParams(data, inputs, outputs, versionDescription);
        } else {
          const {
            inputParams: inputs,
            outputParams: outputs,
            description: versionDescription,
          } = await getOfficialNodeParams(data.getRef(), data.ownerRef, data.version);
          pushParams(data, inputs, outputs, versionDescription);
        }
        // fix: #I5DXPM
        proxy.setData(data);
        data
          .validate()
          // 校验节点有误时，加警告
          .catch(() => workflowValidator.addWarning(droppingNode, clickNodeWarningCallback));
        return true;
      },
    });
  }

  drag(data: IWorkflowNode, event: MouseEvent) {
    // 构建监听器
    this.buildListener(event);

    const node = this.graph.createNode({
      shape: 'vue-shape',
      width,
      height,
      component: 'custom-vue-shape',
      ports: { ...PORTS },
    });
    const proxy = new CustomX6NodeProxy(node);
    proxy.setData(data);

    this.dnd.start(node, event);
  }

  private buildListener({ x, y }: MouseEvent) {
    this.draggingListener.mousePosition = { x, y };
    this.draggingListener.listener = listen(document.body, 'mousemove', (e: MouseEvent) => {
      this.draggingListener.mousePosition.x = e.x;
      this.draggingListener.mousePosition.y = e.y;
    });
  }

  private destroyListener() {
    if (this.draggingListener.listener) {
      this.draggingListener.listener.destroy();
    }

    this.draggingListener.mousePosition = { x: -1, y: -1 };
    delete this.draggingListener.listener;
  }
}
