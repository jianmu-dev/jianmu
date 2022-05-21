import _G6 from '@antv/g6';
import img from '../svgs/shape/webhook.svg';
import { NodeTypeEnum } from '../model/data/enumeration';
import { NODE } from '@/components/workflow/workflow-editor/shape/gengral-config';

const { icon: { width: iconW, height: iconH } } = NODE;

export default function (G6: typeof _G6) {
  G6.registerNode(NodeTypeEnum.WEBHOOK, {
    options: {
      size: [iconW, iconH],
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
        radius: iconW * 0.255,
        shadowOffsetX: 0,
        shadowOffsetY: 0,
        shadowColor: 'transparent',
        shadowBlur: 15,
      },
      stateStyles: {
        hover: {},
        selected: {},
      },
    },
    afterDraw(cfg, group) {
      const width = iconW;
      const height = iconH;
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