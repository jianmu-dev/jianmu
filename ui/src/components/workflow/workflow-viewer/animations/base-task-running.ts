export const attrs = {
  shape: {
    // 默认
    default: {
      x: -35,
      y: -35,
      width: 70,
      height: 70,
      lineWidth: 8,
      stroke: '#11C2C2',
      opacity: 1,
      radius: 70 * 0.242,
    },
    first: {
      x: -43,
      y: -43,
      width: 86,
      height: 86,
      radius: 86 * 0.242,
    },
    second: {
      // opacity: 0.5,
      x: -49,
      y: -49,
      width: 98,
      height: 98,
      radius: 98 * 0.242,
    },
    third: {
      opacity: 0,
      x: -63,
      y: -63,
      width: 126,
      height: 126,
      radius: 126 * 0.242,
    },
  },
  keyShape: {
    default: {
      fill: '#E5FFFF',
      lineWidth: 1,
      stroke: '#11C2C2',
      radius: 80 * 0.242,
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

  protected constructor() {
  }


  start(): void {
  }

  stop(): void {
  }
}