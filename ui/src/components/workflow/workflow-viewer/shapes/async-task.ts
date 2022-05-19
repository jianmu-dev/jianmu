import _G6, { IGroup, IShape, Item, ShapeStyle } from '@antv/g6';
import INIT from '../svgs/shape/async-task/INIT.svg';
import WAITING from '../svgs/shape/async-task/WAITING.svg';
import RUNNING from '../svgs/shape/async-task/RUNNING.svg';
import SKIPPED from '../svgs/shape/async-task/SKIPPED.svg';
import FAILED from '../svgs/shape/async-task/FAILED.svg';
import SUCCEEDED from '../svgs/shape/async-task/SUCCEEDED.svg';
import SUSPENDED from '../svgs/shape/async-task/SUSPENDED.svg';
import IGNORED from '../svgs/shape/async-task/IGNORED.svg';
import { NodeTypeEnum } from '../model/data/enumeration';
import { attrs } from '../animations/base-task-running';
import G6TaskRunning from '../animations/task-running/g6';
import { IItemBaseConfig } from '@antv/g6-core/lib/interface/item';
import { TaskStatusEnum } from '@/api/dto/enumeration';
import { SHELL_NODE_TYPE } from '../model/data/common';
import { NODE } from '@/components/workflow/workflow-editor/shape/gengral-config';

const { icon: { width: iconW, height: iconH } } = NODE;

export const imgs: {
  [key: string]: any;
} = {
  [TaskStatusEnum.INIT]: INIT,
  [TaskStatusEnum.WAITING]: WAITING,
  [TaskStatusEnum.RUNNING]: RUNNING,
  [TaskStatusEnum.SKIPPED]: SKIPPED,
  [TaskStatusEnum.FAILED]: FAILED,
  [TaskStatusEnum.SUCCEEDED]: SUCCEEDED,
  [TaskStatusEnum.SUSPENDED]: SUSPENDED,
  [TaskStatusEnum.IGNORED]: IGNORED,
};

export const states: {
  [key: string]: {
    img: any;
    highlightStyle: ShapeStyle;
    indicatorStyle: ShapeStyle;
  };
} = {
  [TaskStatusEnum.INIT]: {
    img: INIT,
    highlightStyle: {
      shadowColor: '#096DD9',
    },
    indicatorStyle: {
      fill: 'transparent',
    },
  },
  [TaskStatusEnum.WAITING]: {
    img: WAITING,
    highlightStyle: {
      shadowColor: '#FF862B',
    },
    indicatorStyle: {
      fill: '#FF862B',
    },
  },
  [TaskStatusEnum.RUNNING]: {
    img: RUNNING,
    highlightStyle: {
      shadowColor: '#11C2C2',
    },
    indicatorStyle: {
      fill: 'transparent',
    },
  },
  [TaskStatusEnum.SKIPPED]: {
    img: SKIPPED,
    highlightStyle: {
      shadowColor: '#979797',
    },
    indicatorStyle: {
      fill: '#979797',
    },
  },
  [TaskStatusEnum.FAILED]: {
    img: FAILED,
    highlightStyle: {
      shadowColor: '#FF4D4F',
    },
    indicatorStyle: {
      fill: '#FF4D4F',
    },
  },
  [TaskStatusEnum.SUCCEEDED]: {
    img: SUCCEEDED,
    highlightStyle: {
      shadowColor: '#51C41B',
    },
    indicatorStyle: {
      fill: '#51C41B',
    },
  },
  [TaskStatusEnum.SUSPENDED]: {
    img: SUSPENDED,
    highlightStyle: {
      shadowColor: '#7986CB',
    },
    indicatorStyle: {
      fill: '#7986CB',
    },
  },
  [TaskStatusEnum.IGNORED]: {
    img: IGNORED,
    highlightStyle: {
      shadowColor: '#9847FC',
    },
    indicatorStyle: {
      fill: '#9847FC',
    },
  },
};

export default function (G6: typeof _G6) {
  G6.registerNode(
    NodeTypeEnum.ASYNC_TASK,
    {
      options: {
        size: [iconW, iconH],
        anchorPoints: [
          [0.5, 0],
          [0.5, 1],
          [0, 0.5],
          [1, 0.5],
        ],
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
        style: attrs.keyShape.default,
      },
      setState(name, value, item) {
        if (name === 'highlight') {
          const keyShape = item?.get('keyShape') as IShape;

          const status = (item?.getStates().find(state => state.startsWith('status:')) as string).substring(7);
          // 更新样式
          keyShape.attr({
            shadowColor: value ? states[status].highlightStyle.shadowColor : attrs.keyShape.default.shadowColor,
            shadowBlur: value ? 35 : attrs.keyShape.default.shadowBlur,
          });
          return;
        }

        if (name === 'status') {
          const status = value as string;
          const cfg = item?._cfg as IItemBaseConfig;
          const group = item?._cfg?.group as IGroup;
          const defaultIcon = group
            .getChildren()
            .find(child => child.cfg.name === 'async_task_default_icon');
          if (defaultIcon) {
            // 更新默认icon
            defaultIcon.attr('img', imgs[status]);
          }
          const stateIndicator = group
            .getChildren()
            .find(child => child.cfg.name === 'async_task_state_indicator');
          if (stateIndicator) {
            // 更新状态指示灯样式
            stateIndicator.attr(states[status].indicatorStyle);
          }

          if (status === TaskStatusEnum.RUNNING) {
            if (!cfg.runningAnimation) {
              cfg.runningAnimation = new G6TaskRunning(item as Item);
            }

            cfg.runningAnimation.start();
          } else {
            if (cfg.runningAnimation) {
              cfg.runningAnimation.stop();
              delete cfg.runningAnimation;
            }
          }

          return;
        }
      },
      afterDraw(cfg, group) {
        const width = iconW;
        const height = iconH;
        const { iconUrl, uniqueKey } = group?.cfg.item.getModel();

        if (!iconUrl) {
          group?.addShape('image', {
            attrs: {
              x: -width / 2,
              y: -height / 2,
              width,
              height,
              // 不能设置初始值，否则流程节点图标状态可能不刷新
              // img: imgs[TaskStatusEnum.RUNNING],
            },
            name: 'async_task_default_icon',
          });
        } else {
          group?.addShape('image', {
            attrs: {
              x: -width / 2,
              y: -height / 2,
              width,
              height,
              img: `${iconUrl}${uniqueKey === SHELL_NODE_TYPE ? '' : '?roundPic/radius/!25.5p'}`,
            },
            name: 'async_task_icon',
          });
        }

        const indicatorW = 8;
        const indicatorH = 8;
        group?.addShape('rect', {
          attrs: {
            x: width / 2,
            y: -height / 2,
            width: indicatorW,
            height: indicatorH,
            fill: 'transparent',
            radius: 4,
          },
          // must be assigned in G6 3.3 and later versions. it can be any value you want
          name: 'async_task_state_indicator',
        });
      },
    },
    'rect',
  );
}
