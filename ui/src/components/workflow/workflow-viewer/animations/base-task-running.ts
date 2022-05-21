import { IShape } from '@antv/g6';
import { NODE } from '@/components/workflow/workflow-editor/shape/gengral-config';

const { icon: { width: iconW } } = NODE;

export type RunningShape = IShape | SVGElement;

export const attrs = {
  shape: {
    // 默认
    default: {
      lineWidth: 8,
      stroke: '#11C2C2',

      opacity: 0,
      x: -31,
      y: -31,
      width: 62,
      height: 62,
      radius: 62 * 0.242,
    },
    first: {
      opacity: 1,
      x: -39,
      y: -39,
      width: 78,
      height: 78,
      radius: 78 * 0.242,
    },
    second: {
      opacity: 1,
      // opacity: 0.5,
      x: -45,
      y: -45,
      width: 90,
      height: 90,
      radius: 90 * 0.242,
    },
    third: {
      opacity: 0,
      x: -59,
      y: -59,
      width: 118,
      height: 118,
      radius: 118 * 0.242,
    },
  },
  keyShape: {
    default: {
      fill: '#E5FFFF',
      radius: iconW * 0.255,
      shadowOffsetX: 0,
      shadowOffsetY: 0,
      shadowColor: 'transparent',
      shadowBlur: 15,
    },
    first: {
      stroke: 'transparent',
    },
    second: {
      stroke: '#11C2C2',
    },
  },
  iconShape: {
    default: {
      opacity: 1,
    },
    first: {
      opacity: 0,
    },
    second: {
      opacity: 1,
    },
  },
};

export const durations = {
  shape: {
    first: 1000,
    second: 1000,
    third: 2000,
  },
  keyShape: {
    first: 1000,
    second: 1000,
  },
  iconShape: {
    first: 1000,
    second: 1000,
  },
};

export abstract class BaseTaskRunning {
  private readonly shapes: RunningShape[];

  protected constructor(shapes: RunningShape[]) {
    this.shapes = shapes;
  }


  start(): void {
  }

  stop(): void {
  }

  protected next(shape: RunningShape): RunningShape {
    return this.shapes[0] === shape ? this.shapes[1] : this.shapes[0];
  }

  protected getShapes(): RunningShape[] {
    return this.shapes;
  }
}