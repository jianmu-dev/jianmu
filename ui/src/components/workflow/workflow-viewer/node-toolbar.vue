<template>
  <div class="jm-workflow-viewer-node-toolbar" ref="toolbar">
    <template v-if="!operationVisible">
      <div class="mask">
        <jm-tooltip v-if="tips" placement="bottom" :appendToBody="false">
          <template #content>
            <div style="white-space: nowrap" v-html="tips" />
          </template>
          <div class="tooltip-section"></div>
        </jm-tooltip>
      </div>
    </template>
    <jm-popover
      v-else-if="popoverVisible"
      :append-to-body="false"
      :offset="0"
      trigger="hover"
      width="auto"
      placement="top"
    >
      <template #reference>
        <div class="mask">
          <jm-tooltip v-if="tips" placement="bottom" :appendToBody="false">
            <template #content>
              <div style="white-space: nowrap" v-html="tips" />
            </template>
            <div class="tooltip-section"></div>
          </jm-tooltip>
        </div>
      </template>
      <div class="operation">
        <template v-if="status === TaskStatusEnum.SUSPENDED">
          <jm-popconfirm
            title="确定要重试吗？"
            icon="jm-icon-warning"
            confirmButtonText="确定"
            cancelButtonText="取消"
            confirmButtonIcon="jm-icon-button-confirm"
            cancelButtonIcon="jm-icon-button-cancel"
            @confirm="handleClick(NodeToolbarTabTypeEnum.RETRY)"
            :append-to-body="false"
            :offset="7"
          >
            <template #reference>
              <div class="item">
                <div class="icon retry"></div>
                <div class="txt">重试</div>
              </div>
            </template>
          </jm-popconfirm>
          <div class="separator"></div>
          <jm-popconfirm
            title="确定要忽略吗？"
            icon="jm-icon-warning"
            confirmButtonText="确定"
            cancelButtonText="取消"
            confirmButtonIcon="jm-icon-button-confirm"
            cancelButtonIcon="jm-icon-button-cancel"
            @confirm="handleClick(NodeToolbarTabTypeEnum.IGNORE)"
            :append-to-body="false"
            :offset="7"
          >
            <template #reference>
              <div class="item">
                <div class="icon ignore"></div>
                <div class="txt">忽略</div>
              </div>
            </template>
          </jm-popconfirm>
          <div class="separator"></div>
        </template>
        <div class="item" @click="handleClick(NodeToolbarTabTypeEnum.LOG)">
          <div class="icon view-log" />
          <div class="txt">日志</div>
        </div>
        <div class="separator"></div>
        <div class="item" @click="handleClick(NodeToolbarTabTypeEnum.PARAMS)">
          <div class="icon view-params" />
          <div class="txt">参数</div>
        </div>
        <div class="separator" v-if="taskCaches"></div>
        <div class="item" v-if="taskCaches" @click="handleClick(NodeToolbarTabTypeEnum.CACHE)">
          <div class="icon view-cache" />
          <div class="txt">缓存</div>
        </div>
      </div>
    </jm-popover>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, nextTick, onMounted, onUpdated, PropType, ref, SetupContext } from 'vue';
import { INodeMouseoverEvent } from './model/data/common';
import { MAX_LABEL_LENGTH } from './model/dsl/g6';
import { GraphTypeEnum, NodeToolbarTabTypeEnum, NodeTypeEnum } from './model/data/enumeration';
import { TaskStatusEnum } from '@/api/dto/enumeration';

export default defineComponent({
  props: {
    graphType: {
      type: String as PropType<GraphTypeEnum>,
      required: true,
    },
    readonly: {
      type: Boolean,
      required: true,
    },
    taskCaches: {
      type: Boolean,
      default: false,
    },
    taskBusinessId: String,
    taskStatus: String as PropType<TaskStatusEnum>,
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
    const toolbar = ref<HTMLElement>();
    const popoverVisible = ref<boolean>(true);
    const status = ref<TaskStatusEnum>(props.taskStatus);

    onUpdated(async () => {
      if (status.value === props.taskStatus) {
        return;
      }
      status.value = props.taskStatus;
      popoverVisible.value = false;
      // 保证状态变化时，重新渲染
      await nextTick();
      popoverVisible.value = true;
    });

    onMounted(() => {
      const { x, y, width, height } = props.nodeEvent;
      toolbar.value.style.left = `${x}px`;
      toolbar.value.style.top = `${y}px`;
      toolbar.value.style.width = `${width}px`;
      toolbar.value.style.height = `${height}px`;
    });

    return {
      NodeToolbarTabTypeEnum,
      TaskStatusEnum,
      popoverVisible,
      status,
      tips: computed<string>(() => {
        let str = '';
        if (props.graphType === GraphTypeEnum.X6) {
          return str;
        }

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
        if (props.readonly) {
          return false;
        }
        if (NodeTypeEnum.WEBHOOK === props.nodeEvent.type) {
          return true;
        }
        if (NodeTypeEnum.ASYNC_TASK === props.nodeEvent.type) {
          return props.taskStatus !== TaskStatusEnum.INIT;
        }
        return false;
      }),
      toolbar,
      handleClick: (tabType: NodeToolbarTabTypeEnum) => {
        if (props.nodeEvent.type === NodeTypeEnum.ASYNC_TASK) {
          emit('node-click', props.taskBusinessId, props.nodeEvent.type, tabType);
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

  ::v-deep(.el-popover) {
    &.el-popper {
      padding: 6px 16px;
    }
  }

  .mask {
    position: absolute;
    left: 0;
    top: -10px;
    width: 100%;
    height: calc(100% + 10px);

    .tooltip-section {
      position: absolute;
      left: 0;
      top: 0;
      width: 100%;
      height: 95%;
    }
  }

  .operation {
    display: flex;
    justify-content: center;
    align-items: center;

    .item {
      user-select: none;
      cursor: pointer;

      &:active {
        .icon {
          background-color: #eff7ff;
          border-radius: 2px;
        }
      }

      .icon {
        width: 28px;
        height: 28px;
        background-color: transparent;
        border: 0;
        background-position: center center;
        background-repeat: no-repeat;

        &.retry {
          background-image: url('./svgs/task-tool/retry.svg');
        }

        &.ignore {
          background-image: url('./svgs/task-tool/ignore.svg');
        }

        &.view-log {
          background-image: url('./svgs/task-tool/view-log.svg');
        }

        &.view-params {
          background-image: url('./svgs/task-tool/view-params.svg');
        }
        &.view-cache {
          background-image: url('./svgs/task-tool/view-cache.svg');
        }
      }

      .txt {
        text-align: center;
        font-size: 12px;
        color: #082340;
        line-height: 20px;
      }
    }

    .separator {
      margin: 0 10px 20px 10px;
      width: 1px;
      height: 15px;
      background-color: #d9dee7;
      overflow: hidden;
    }
  }
}
</style>
