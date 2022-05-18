import { CellView } from '@antv/x6';
import { attrs, BaseTaskRunning, durations, RunningShape } from '../base-task-running';

const toolItem = {
  name: 'boundary',
  args: {
    padding: -attrs.shape.default.lineWidth / 2,
    attrs: {
      rx: attrs.shape.default.radius,
      ry: attrs.shape.default.radius,
      // 线色
      stroke: attrs.shape.default.stroke,
      // 禁用虚线
      'stroke-dasharray': 'none',
      // 线宽
      'stroke-width': attrs.shape.default.lineWidth,
      opacity: attrs.shape.default.opacity,
    },
  },
};

/**
 * X6任务执行中动画
 */
export default class X6TaskRunning extends BaseTaskRunning {
  private readonly view: CellView;
  private readonly keyShape: HTMLElement;
  private keyShapeInterval: any;
  private readonly iconShape: HTMLElement;
  private iconShapeInterval: any;

  constructor(view: CellView) {
    view.cell.addTools(toolItem);
    view.cell.addTools(toolItem);
    super(Array.from(view.graph.container.querySelectorAll('.x6-cell-tool.x6-node-tool.x6-cell-tool-boundary'))
      .filter(el => (el.getAttribute('data-cell-id') === view.cell.id)) as SVGElement[]);

    this.view = view;

    const vueShape = Array.from(this.view.graph.container.querySelectorAll('.jm-workflow-x6-vue-shape'))
      .filter(el => (el.getAttribute('data-x6-node-id') === this.view.cell.id))[0];
    this.keyShape = vueShape.querySelector('.icon') as HTMLElement;
    this.keyShape.style.transition =
      `background-color ${Math.round(durations.keyShape.first / 1000)}s linear`;
    this.iconShape = vueShape.querySelector('.img')! as HTMLElement;
    this.iconShape.style.transition =
      `opacity ${Math.round(durations.iconShape.first / 1000)}s linear`;
  }

  start(): void {
    setTimeout(() => {
      // 保证渲染完成
      Object.keys(attrs.shape.first)
        .flatMap(key => (key === 'radius') ? ['rx', 'ry'] : key)
        .forEach(key => this.animateShape(super.getShapes()[0], key));
    }, 1000);
  }

  stop(): void {
    this.view.cell.removeTool('boundary');

    if (this.keyShapeInterval) {
      clearInterval(this.keyShapeInterval);
      delete this.keyShapeInterval;
    }
    this.keyShape.style.transition = '';
    this.keyShape.style.backgroundColor = '';

    if (this.iconShapeInterval) {
      clearInterval(this.iconShapeInterval);
      delete this.iconShapeInterval;
    }
    this.iconShape.style.transition = '';
    this.iconShape.style.opacity;
  }

  private animateShape(_shape: RunningShape, key: string) {
    const shape = _shape as SVGElement;

    // 初始化
    // shape.stopAnimate(false);
    // shape.attr(attrs.shape.default);

    // 第一步
    this.buildAnimations(shape, key, undefined, attrs.shape.first, durations.shape.first, () => {
      // 第二步
      if (this.iconShape) {
        this.animateIconShape();
      }
      this.animateKeyShape();

      this.buildAnimations(shape, key, attrs.shape.first, attrs.shape.second, durations.shape.second, () => {
        // 第三步
        this.buildAnimations(shape, key, attrs.shape.second, attrs.shape.third, durations.shape.third);

        // 重复
        this.animateShape(super.next(_shape), key);
      });
    });
  }

  private buildAnimations(shape: SVGElement, key: string,
    previousAttrs: any, attrs: any, duration: number, callback?: () => void): void {
    const tempKey = ['rx', 'ry'].includes(key) ? 'radius' : key;
    const value = attrs[tempKey];

    let from;
    let to;
    switch (key) {
      case 'x': {
        const shapeX = parseFloat(shape.getAttribute(key)!);
        const shapeWidth = parseFloat(shape.getAttribute('width')!);
        const previousDeltaW = previousAttrs ? (shapeWidth - previousAttrs.width) / 2 : 0;
        const deltaW = (shapeWidth - attrs.width) / 2;
        from = shapeX + previousDeltaW;
        to = shapeX + deltaW;
        break;
      }
      case 'y': {
        const shapeY = parseFloat(shape.getAttribute(key)!);
        const shapeHeight = parseFloat(shape.getAttribute('height')!);
        const previousDeltaH = previousAttrs ? (shapeHeight - previousAttrs.height) / 2 : 0;
        const deltaH = (shapeHeight - attrs.height) / 2;
        from = shapeY + previousDeltaH;
        to = shapeY + deltaH;
        break;
      }
      default:
        from = previousAttrs ? previousAttrs[tempKey] : shape.getAttribute(key);
        to = value;
        break;
    }

    // x6会触发两次complete，属于bug
    // 通过中间变量绕过，保证触发一次回调
    let previous: Event | undefined = undefined;
    this.view.animate(shape, {
      attributeType: 'XML',
      attributeName: key,
      from,
      to,
      dur: `${duration / 1000}s`,
      fill: 'freeze',
      repeatCount: 0,
      complete: e => {
        if (previous) {
          return;
        }
        previous = e;

        if (!callback) {
          return;
        }
        callback();
      },
    });
  }

  private animateKeyShape() {
    // 初始化
    this.keyShape.style.backgroundColor = attrs.keyShape.default.stroke;

    let index = 0;
    this.keyShapeInterval = setInterval(() => {
      this.keyShape.style.backgroundColor = `${index++ % 2 === 0 ?
        attrs.keyShape.first.stroke : attrs.keyShape.second.stroke}`;
    }, durations.keyShape.first);
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