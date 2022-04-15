import _G6, { INode, IShape } from '@antv/g6';
import { IElement } from '@antv/g-base/lib/interfaces';
import { NodeTypeEnum } from '@/components/workflow/workflow-viewer/utils/enumeration';

// lineDash array
const lineDash = [4, 2, 1, 2];

export default function (G6: typeof _G6) {
  G6.registerEdge('flow', {
    options: {
      labelCfg: {
        // 边上的标签文本根据边的方向旋转
        autoRotate: true,

        style: {
          fontSize: 16,
          fontWeight: 400,
          fill: '#474F68',
          shadowOffsetX: 0,
          shadowOffsetY: 0,
          shadowColor: '#FFFFFF',
          shadowBlur: 15,

          // label背景
          background: {
            fill: '#FFFFFF',
            padding: [0, 0, 0, 0],
          },
        },
      },
      style: {
        stroke: '#C7CFE3',
        lineWidth: 2.5,
      },
    },
    setState(name, value, item) {
      const shape = item?.get('keyShape') as IShape;
      if (name === 'running') {
        if (value) {
          if (!shape.cfg.isAnimating) {
            let index = 0;
            shape.animate(() => {
              index++;
              if (index > 9) {
                index = 0;
              }

              return {
                lineDash,
                lineDashOffset: -index,
              };
            }, {
              repeat: true,
              duration: 1000,
            });

            shape.cfg.isAnimating = true;
          }
        } else {
          shape.stopAnimate(false);
          shape.attr('lineDash', null);

          delete shape.cfg.isAnimating;
        }
      }
    },
    afterDraw(cfg, group) {
      const targetNode = cfg?.targetNode as INode;
      const path = group?.getFirst() as IElement;

      if (targetNode._cfg?.model?.type === NodeTypeEnum.FLOW_NODE) {
        return;
      }

      // 动态画箭头
      path.attr({
        endArrow: {
          path: G6.Arrow.vee(18, 18, 1.4),
          d: 2.6,
          fill: '#C7CFE3',
          stroke: 'transparent',
        },
      });
    },
  }, 'polyline');
}