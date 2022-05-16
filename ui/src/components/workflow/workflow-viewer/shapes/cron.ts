import _G6 from '@antv/g6';
import img from '../svgs/shape/cron.svg';
import { NodeTypeEnum } from '../model/data/enumeration';

export default function (G6: typeof _G6) {
  G6.registerNode(NodeTypeEnum.CRON, {
    options: {
      size: 80,
      anchorPoints: [[0.5, 0], [0.5, 1], [0, 0.5], [1, 0.5]],
      labelCfg: {
        position: 'bottom',
        style: {
          fontSize: 18,
          fontWeight: 500,
          fill: '#3F536E',

          // label背景
          background: {
            // fill: '#FFFFFF',
            padding: [0, 0, 10, 0],
          },
        },
      },
      style: {
        fill: '#FFFFFF',
        lineWidth: 1,
        stroke: '#2962FF',
        radius: 80 * 0.242,
        shadowOffsetX: 0,
        shadowOffsetY: 0,
        shadowColor: '#C5D9FF',
        shadowBlur: 15,
      },
      stateStyles: {
        hover: {},
        selected: {},
      },
    },
    afterDraw(cfg, group) {
      const width = 44;
      const height = 44;
      group?.addShape('image', {
        attrs: {
          x: -width / 2,
          y: -height / 2,
          width,
          height,
          img,
        },
      });
    },
  }, 'rect');
}