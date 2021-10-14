<template>
  <div class="jm-workflow-viewer-node-toolbar" ref="toolbar">
    <div class="mask"></div>
    <div v-if="operationVisible" class="operation">
      <jm-tooltip content="日志" placement="left">
        <button class="view-log-btn" @click="handleClick(NodeToolbarTabTypeEnum.LOG)"></button>
      </jm-tooltip>
      <jm-tooltip content="业务参数" placement="right">
        <button class="view-params-btn" @click="handleClick(NodeToolbarTabTypeEnum.PARAMS)"></button>
      </jm-tooltip>
    </div>
    <jm-tooltip v-if="tips" placement="bottom">
      <template #content>
        <div v-html="tips"/>
      </template>
      <div class="tooltip-section"></div>
    </jm-tooltip>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, PropType, ref, SetupContext } from 'vue';
import { INodeMouseoverEvent } from './utils/model';
import { MAX_LABEL_LENGTH } from './utils/dsl';
import { NodeToolbarTabTypeEnum, NodeTypeEnum } from './utils/enumeration';

export default defineComponent({
  props: {
    taskInstanceId: String,
    nodeEvent: {
      type: Object as PropType<INodeMouseoverEvent>,
      required: true,
    },
    zoom: {
      type: Number,
      required: true,
    },
  },
  emits: ['node-click'],
  setup(props: any, { emit }: SetupContext) {
    const { proxy } = getCurrentInstance() as any;
    const toolbar = ref();

    proxy.$nextTick(() => {
      const z = props.zoom / 100;
      const w = props.nodeEvent.width + 10;
      const h = props.nodeEvent.height + 10;

      toolbar.value.style.left = (props.nodeEvent.x - w / 2) + 'px';
      toolbar.value.style.top = (props.nodeEvent.y - h / 2) + 'px';
      toolbar.value.style.width = w + 'px';
      toolbar.value.style.height = (h + 23 * z) + 'px';
    });

    return {
      NodeToolbarTabTypeEnum,
      tips: computed<string>(() => {
        let str = '';

        switch (props.nodeEvent.type) {
          case NodeTypeEnum.WEBHOOK:
          case NodeTypeEnum.ASYNC_TASK:
          case NodeTypeEnum.END:
          case NodeTypeEnum.START:
            str = props.nodeEvent.description.length > MAX_LABEL_LENGTH ? props.nodeEvent.description : undefined;
            break;
          case NodeTypeEnum.CONDITION:
          case NodeTypeEnum.CRON:
            str = props.nodeEvent.description;
        }

        return str;
      }),
      operationVisible: computed<boolean>(() => {
        if (props.nodeEvent.type === NodeTypeEnum.WEBHOOK) {
          return true;
        }
        return !!(props.nodeEvent.type === NodeTypeEnum.ASYNC_TASK && props.taskInstanceId);
      }),
      toolbar,
      handleClick: (tabType: NodeToolbarTabTypeEnum) => {
        if (props.nodeEvent.type === NodeTypeEnum.ASYNC_TASK) {
          emit('node-click', props.taskInstanceId, props.nodeEvent.type, tabType);
          return;
        }
        emit('node-click', props.nodeEvent.id, props.nodeEvent.type, tabType);
      },
    };
  },
});
</script>

<style scoped lang="less">
.jm-workflow-viewer-node-toolbar {
  position: fixed;
  z-index: 1;

  .mask {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
  }

  .operation {
    position: absolute;
    top: -29px;
    left: 0;
    width: 100%;
    text-align: center;
    white-space: nowrap;

    button {
      width: 28px;
      height: 28px;
      background-color: transparent;
      border: 0;
      background-position: center center;
      background-repeat: no-repeat;
      cursor: pointer;

      & + & {
        margin-left: 8px;
      }

      &.view-log-btn {
        background-image: url('./svgs/task-tool/view-log.svg');

        &:hover {
          background-image: url('./svgs/task-tool/view-log-hover.svg');
        }
      }

      &.view-params-btn {
        background-image: url('./svgs/task-tool/view-params.svg');

        &:hover {
          background-image: url('./svgs/task-tool/view-params-hover.svg');
        }
      }
    }
  }

  .tooltip-section {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 97%;
  }
}
</style>