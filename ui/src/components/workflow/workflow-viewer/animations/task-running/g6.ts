import { IGroup, IShape, Item } from '@antv/g6';
import { attrs, BaseTaskRunning, durations, RunningShape } from '../base-task-running';

/**
 * G6任务执行中动画
 */
export default class G6TaskRunning extends BaseTaskRunning {
  private readonly group: IGroup;
  // private readonly keyShape: IShape;
  private readonly iconShape?: IShape;

  constructor(item: Item) {
    const group = item._cfg?.group as IGroup;
    super([
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
    ]);
    const children = [...group.getChildren()];
    // children[1].setZIndex(-2);
    // children[2].setZIndex(-2);

    this.group = group;
    this.iconShape = children.find(child =>
      ['async_task_default_icon', 'async_task_icon'].includes(child.cfg.name)) as IShape | undefined;
    // this.keyShape = item.get('keyShape');

    group.sort();
  }

  start(): void {
    // this.keyShape.attr(attrs.keyShape.first);
    // this.keyShape.animate(attrs.keyShape.second, {
    //   duration: durations.keyShape.second,
    // });
    this.iconShape?.attr(attrs.iconShape.first);
    this.iconShape?.animate(attrs.iconShape.second, {
      duration: durations.iconShape.second,
    });
    this.animateShape(super.getShapes()[0]);
  }

  stop(): void {
    super.getShapes().forEach(_shape => {
      const shape = _shape as IShape;
      shape.stopAnimate(false);
      this.group.removeChild(shape);
    });

    // this.keyShape.stopAnimate(false);
    // this.keyShape.attr(attrs.keyShape.default);
    this.iconShape?.stopAnimate(false);
    this.iconShape?.attr(attrs.iconShape.default);
  }

  private animateShape(_shape: RunningShape) {
    const shape = _shape as IShape;

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
        // this.animateKeyShape();

        shape.animate(attrs.shape.second, {
          duration: durations.shape.second,
          callback: () => {
            // 第三步
            shape.animate(attrs.shape.third, {
              duration: durations.shape.third,
            });

            // 重复
            this.animateShape(super.next(_shape));
          },
        });
      },
    });
  }

  // private animateKeyShape() {
  //   // 初始化
  //   this.keyShape.stopAnimate(false);
  //   this.keyShape.attr(attrs.keyShape.default);
  //
  //   // 第一步
  //   this.keyShape.animate(attrs.keyShape.first, {
  //     duration: durations.keyShape.first,
  //     callback: () => {
  //       // 第二步
  //       this.keyShape.animate(attrs.keyShape.second, {
  //         duration: durations.keyShape.second,
  //       });
  //     },
  //   });
  // }

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