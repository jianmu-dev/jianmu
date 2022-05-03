import { Graph, Node } from '@antv/x6';
import { CustomX6NodeProxy } from './data/custom-x6-node-proxy';
import { NodeTypeEnum } from './data/enumeration';

export class WorkflowNodeToolbar {
  private readonly proxy: any;
  private readonly el: HTMLElement;
  private readonly graph: Graph;
  private node?: Node;

  constructor(proxy: any, graph: Graph) {
    this.proxy = proxy;
    this.el = this.proxy.nodeToolbar.$el;
    this.graph = graph;
  }

  show(node: Node): void {
    this.node = node;
    this.move();
  }

  hide({ relatedTarget }: MouseEvent): void {
    if (relatedTarget === this.el) {
      return;
    }

    this.deleteNode();
  }

  private deleteNode() {
    delete this.node;
    this.el.style.left = '';
    this.el.style.top = '';
  }

  removeNode(): void {
    if (!this.node) {
      return;
    }

    const node = this.node;
    const nodeProxy = new CustomX6NodeProxy(node);
    const nodeData = nodeProxy.getData();

    let msg = '<div>确定要删除吗?</div>';
    msg += `<div style="margin-top: 5px; font-size: 12px; line-height: normal;">名称：${nodeData.getName()}</div>`;
    let title = '删除';
    switch (nodeData.getType()) {
      case NodeTypeEnum.ASYNC_TASK:
      case NodeTypeEnum.SHELL:
        title += '节点';
        break;
      case NodeTypeEnum.CRON:
      case NodeTypeEnum.WEBHOOK:
        title += '触发器';
        break;
    }

    this.proxy.$confirm(msg, title, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true,
    }).then(async () => {
      this.graph.removeCell(node);

      this.deleteNode();
    }).catch(() => {
    });
  }

  move(): void {
    if (!this.node) {
      return;
    }

    const node = this.node;

    // 保证不偏移
    setTimeout(() => {
      // 画布偏移量
      const { tx, ty } = this.graph.translate();
      // 相对于画布的绝对坐标
      const { x, y } = node.getPosition();
      // 缩放比例
      const zoom = this.graph.zoom();
      // 节点大小
      const { width: nodeW } = node.size();
      // 节点工具栏大小
      const { offsetWidth: toolbarW, offsetHeight: toolbarH } = this.el;

      this.el.style.left = `${x * zoom + tx + (nodeW * zoom - toolbarW) / 2}px`;
      this.el.style.top = `${y * zoom + ty - toolbarH}px`;
    });
  }
}