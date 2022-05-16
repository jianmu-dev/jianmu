import _G6 from '@antv/g6';
import { NodeTypeEnum } from '../model/data/enumeration';
import img from '../svgs/shape/flow-node.svg';

export default function (G6: typeof _G6) {
  G6.registerNode(NodeTypeEnum.FLOW_NODE, {
    options: {
      size: 44,
      anchorPoints: [[0.5, 0.5]],
      style: {
        fill: '#FFFFFF',
        lineWidth: 1,
        stroke: '#00A6AB',
      },
    },
    afterDraw(cfg, group) {
      const width = 24;
      const height = 24;
      group?.addShape('image', {
        attrs: {
          x: -width / 2 - 1,
          y: -height / 2,
          width,
          height,
          img,
        },
      });
    },
  }, 'circle');
}