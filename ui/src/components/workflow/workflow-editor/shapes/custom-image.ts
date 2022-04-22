import { Graph } from '@antv/x6';
import ports from './ports';

const width = 80;
const height = 80;

export default function () {
  Graph.registerNode(
    'custom-image',
    {
      inherit: 'rect',
      width,
      height,
      markup: [
        {
          tagName: 'rect',
          selector: 'body',
        },
        {
          tagName: 'image',
        },
        {
          tagName: 'text',
          selector: 'label',
        },
      ],
      attrs: {
        body: {
          stroke: '#5F95FF',
          fill: '#5F95FF',
          rx: width * 25.5 / 100,
        },
        image: {
          width,
          height,
          refX: 0,
          refY: 0,
          rx: width * 25.5 / 100,
          overflow: 'hidden',
        },
        label: {
          refX: 0,
          refY: height + 5,
          textAnchor: 'left',
          textVerticalAnchor: 'top',
          fontSize: 12,
          fill: '#FF0000',
        },
      },
      ports: { ...ports },
    },
    true,
  );
}