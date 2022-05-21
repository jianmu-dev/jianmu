import { CellView } from '@antv/x6';
import { attrs, BaseTaskRunning, durations } from '../base-task-running';
import { NODE } from '@/components/workflow/workflow-editor/shape/gengral-config';

const { icon: { width, height } } = NODE;

const toolItem = {
  name: 'boundary',
  args: {
    padding: 4,
    attrs: {
      rx: width * 0.315,
      ry: height * 0.315,
      // 线色
      stroke: attrs.shape.default.stroke,
      // 禁用虚线
      'stroke-dasharray': 'none',
      // 线宽
      'stroke-width': attrs.shape.default.lineWidth,
      opacity: 0,
    },
  },
};

const { userAgent: ua } = navigator;
const isSafari = !ua.includes('Chrome') && ua.includes('Safari');

/**
 * X6任务执行中动画
 */
export default class X6TaskRunning extends BaseTaskRunning {
  private readonly view: CellView;
  // private readonly keyShape: HTMLElement;
  // private keyShapeInterval: any;
  private readonly iconShape: HTMLElement;
  private shapeInterval: any;
  private iconShapeInterval: any;

  constructor(view: CellView) {
    view.cell.addTools(toolItem);
    const graph = view.graph;
    super(Array.from(graph.container.querySelectorAll('.x6-cell-tool.x6-node-tool.x6-cell-tool-boundary'))
      .filter(el => (el.getAttribute('data-cell-id') === view.cell.id)) as SVGElement[]);

    this.view = view;

    const vueShape = Array.from(graph.container.querySelectorAll('.jm-workflow-x6-vue-shape'))
      .filter(el => (el.getAttribute('data-x6-node-id') === this.view.cell.id))[0];
    this.iconShape = vueShape.querySelector('.img')! as HTMLElement;
    this.iconShape.style.transition =
      `opacity ${Math.round(durations.iconShape.first / 1000)}s linear`;

    if (isSafari) {
      // 兼容safari
      graph.container.querySelectorAll('.x6-cell-tool-boundary').forEach(el => {
        const delta = 15;
        const factor = graph.zoom();
        const x = Math.round(parseFloat(el.getAttribute('x')!));
        const width = Math.round(parseFloat(el.getAttribute('width')!));

        el.setAttribute('x', `${x + delta * factor}`);
        el.setAttribute('width', `${width - delta * factor}`);
      });
    }
  }

  start(): void {
    const shape = super.getShapes()[0] as SVGElement;
    let reverse = false;
    this.shapeInterval = setInterval(() => {
      this.animateShape(shape, reverse);
      reverse = !reverse;
    }, durations.iconShape.first);

    if (!isSafari) {
      this.animateIconShape();
    }
  }

  stop(): void {
    if (this.shapeInterval) {
      clearInterval(this.shapeInterval);
      delete this.shapeInterval;
    }
    this.view.cell.removeTool('boundary');

    if (this.iconShapeInterval) {
      clearInterval(this.iconShapeInterval);
      delete this.iconShapeInterval;
    }
    this.iconShape.style.transition = '';
    this.iconShape.style.opacity = '';
  }

  private animateShape(shape: SVGElement, reverse: boolean) {
    this.view.animate(shape, {
      attributeType: 'XML',
      attributeName: 'opacity',
      from: reverse ? 1 : 0,
      to: reverse ? 0 : 1,
      dur: `${Math.round(durations.iconShape.first / 1000)}s`,
      fill: 'freeze',
      repeatCount: 1,
    });
  }

  private animateIconShape() {
    // 初始化
    this.iconShape.style.opacity = `${attrs.iconShape.default}`;

    let index = 0;
    this.iconShapeInterval = setInterval(() => {
      this.iconShape.style.opacity = `${index++ % 2 === 0 ?
        attrs.iconShape.first.opacity : attrs.iconShape.second.opacity}`;
    }, durations.iconShape.first);
  }
}