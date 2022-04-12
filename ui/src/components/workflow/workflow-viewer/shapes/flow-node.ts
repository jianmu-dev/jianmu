import _G6 from '@antv/g6';
import { NodeTypeEnum } from '../utils/enumeration';

export default function (G6: typeof _G6) {
  G6.registerNode(NodeTypeEnum.FLOW_NODE, {
    options: {
      size: 25,
      // anchorPoints: [[0.5, 0], [0.5, 1], [0, 0.5], [1, 0.5]],
      anchorPoints: [[0.5, 0.5]],
      style: {
        // fill: '#C7CFE3',
        lineWidth: 1,
        stroke: '#C7CFE3',
      },
    },
  }, 'circle');
}