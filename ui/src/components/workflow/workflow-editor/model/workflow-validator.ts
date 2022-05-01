import { Graph, Node, Point } from '@antv/x6';
import { INodeData } from './data';
import { NodeTypeEnum } from './enumeration';

export class WorkflowValidator {
  private readonly graph: Graph;

  constructor(graph: Graph) {
    this.graph = graph;
  }

  checkNode(node: Node): boolean {
    const { inputs } = node.getData<INodeData>();

    if (inputs.length === 0) {
      // 无参数时，忽略
      return true;
    }

    // TODO 检查必填项
    return false;
  }

  checkDroppingNode(node: Node, mousePosition: Point.PointLike, nodePanelRect: DOMRect): boolean {
    if (!this.checkDroppingPosition(mousePosition, nodePanelRect)) {
      return false;
    }

    if (!this.checkTrigger(node)) {
      return false;
    }

    return true;
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

  private checkTrigger(droppingNode: Node): boolean {
    const { nodeType } = droppingNode.getData<INodeData>();

    if (![NodeTypeEnum.CRON, NodeTypeEnum.WEBHOOK].includes(nodeType)) {
      // 非trigger时，忽略
      return true;
    }

    // 表示当前拖放的节点为trigger
    const currentTrigger = this.graph.getNodes().find(node =>
      [NodeTypeEnum.CRON, NodeTypeEnum.WEBHOOK].includes(node.getData<INodeData>().nodeType));

    if (currentTrigger) {
      // TODO 需加提示
      console.log('只能有一个触发器节点');
      return false;
    }

    return true;
  }
}