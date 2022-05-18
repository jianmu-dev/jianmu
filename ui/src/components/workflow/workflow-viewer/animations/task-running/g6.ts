import { IGroup, IShape, Item } from '@antv/g6';
import { attrs, BaseTaskRunning, durations } from '../base-task-running';

/**
 * G6任务执行中动画
 */
export default class G6TaskRunning extends BaseTaskRunning {
  private readonly group: IGroup;
  private readonly keyShape: IShape;
  private readonly iconShape: IShape | undefined;
  private readonly shapes: IShape[];

  constructor(item: Item) {
    super();
    const group = item._cfg?.group as IGroup;
    const children = [...group.getChildren()];
    // children[1].setZIndex(-2);
    // children[2].setZIndex(-2);

    this.group = group;
    this.iconShape = children.find(child => child.cfg.name === 'async_task_icon') as IShape | undefined;
    this.keyShape = item.get('keyShape');
    this.shapes = [
      group.addShape('rect', {
        zIndex: -1,
        attrs: attrs.shape.default,
        name: 'animate_shape_1',
      }),
      group.addShape('rect', {
        zIndex: -2,
        attrs: attrs.shape.default,
        name: 'animate_shape_2',
      }),
    ];

    group.sort();
  }

  start(): void {
    this.keyShape.attr(attrs.keyShape.first);
    this.keyShape.animate(attrs.keyShape.second, {
      duration: durations.keyShape.second,
    });
    this.iconShape?.attr(attrs.iconShape.first);
    this.iconShape?.animate(attrs.iconShape.second, {
      duration: durations.iconShape.second,
    });
    this.animateShape(this.shapes[0]);
  }

  stop(): void {
    this.shapes.forEach(shape => {
      shape.stopAnimate(false);
      this.group.removeChild(shape);
    });

    this.keyShape.stopAnimate(false);
    this.keyShape.attr(attrs.keyShape.default);
    this.iconShape?.stopAnimate(false);
    this.iconShape?.attr(attrs.iconShape.default);
  }

  private next(shape: IShape): IShape {
    return this.shapes[0] === shape ? this.shapes[1] : this.shapes[0];
  }

  private animateShape(shape: IShape) {
    // 第一步
    // 初始化
    shape.stopAnimate(false);
    shape.attr(attrs.shape.default);

    shape.animate(attrs.shape.first, {
      duration: durations.shape.first,
      callback: () => {
        // 第二步
        if (this.iconShape) {
          this.animateIconShape();
        }
        this.animateKeyShape();

        shape.animate(attrs.shape.second, {
          duration: durations.shape.second,
          callback: () => {
            // 第三步
            shape.animate(attrs.shape.third, {
              duration: durations.shape.third,
            });

            // 重复
            this.animateShape(this.next(shape));
          },
        });
      },
    });
  }

  private animateKeyShape() {
    // 初始化
    this.keyShape.stopAnimate(false);
    this.keyShape.attr(attrs.keyShape.default);

    // 第一步
    this.keyShape.animate(attrs.keyShape.first, {
      duration: durations.keyShape.first,
      callback: () => {
        // 第二步
        this.keyShape.animate(attrs.keyShape.second, {
          duration: durations.keyShape.second,
        });
      },
    });
  }

  private animateIconShape() {
    if (!this.iconShape) {
      return;
    }

    // 初始化
    this.iconShape.stopAnimate(false);
    this.iconShape.attr(attrs.iconShape.default);

    // 第一步
    this.iconShape.animate(attrs.iconShape.first, {
      duration: durations.iconShape.first,
      callback: () => {
        // 第二步
        this.iconShape?.animate(attrs.iconShape.second, {
          duration: durations.iconShape.second,
        });
      },
    });
  }
}