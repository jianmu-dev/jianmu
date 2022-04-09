<template>
  <div class="jm-workflow-viewer-node-toolbar" ref="toolbar">
    <template v-if="!operationVisible">
      <div class="mask"></div>
      <jm-tooltip v-if="tips" placement="bottom" :appendToBody="false">
        <template #content>
          <div style="white-space: nowrap" v-html="tips"/>
        </template>
        <div class="tooltip-section"></div>
      </jm-tooltip>
    </template>
    <jm-popover v-else
                :append-to-body="false"
                :offset="0"
                trigger="hover"
                width="auto"
                placement="top">
      <template #reference>
        <div class="mask"></div>
        <jm-tooltip v-if="tips" placement="bottom" :appendToBody="false">
          <template #content>
            <div style="white-space: nowrap" v-html="tips"/>
          </template>
          <div class="tooltip-section"></div>
        </jm-tooltip>
      </template>
      <div class="operation">
        <template v-if="taskStatus === TaskStatusEnum.SUSPENDED">
          <jm-popconfirm
            title="确定要重试吗？"
            icon="jm-icon-warning"
            confirmButtonText="确定"
            cancelButtonText="取消"
            confirmButtonIcon="jm-icon-button-confirm"
            cancelButtonIcon="jm-icon-button-cancel"
            @confirm="handleClick(NodeToolbarTabTypeEnum.RETRY)"
            :append-to-body="false"
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
          <div class="icon view-log"/>
          <div class="txt">日志</div>
        </div>
        <div class="separator"></div>
        <div class="item" @click="handleClick(NodeToolbarTabTypeEnum.PARAMS)">
          <div class="icon view-params"/>
          <div class="txt">参数</div>
        </div>
      </div>
    </jm-popover>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, PropType, ref, SetupContext } from 'vue';
import { INodeMouseoverEvent } from './utils/model';
import { MAX_LABEL_LENGTH } from './utils/dsl';
import { NodeToolbarTabTypeEnum, NodeTypeEnum } from './utils/enumeration';
import { TaskStatusEnum } from '@/api/dto/enumeration';

export default defineComponent({
  props: {
    readonly: {
      type: Boolean,
      required: true,
    },
    taskInstanceId: String,
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

    onMounted(() => {
      const z = props.zoom / 100;
      const w = props.nodeEvent.width + 10;
      const h = props.nodeEvent.height + 10;

      toolbar.value.style.left = props.nodeEvent.x - w / 2 + 'px';
      toolbar.value.style.top = (props.nodeEvent.y - h / 2) + 'px';
      toolbar.value.style.width = w + 'px';
      toolbar.value.style.height = (h + 23 * z) + 'px';
    });

    return {
      NodeToolbarTabTypeEnum,
      TaskStatusEnum,
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
        if (props.readonly) {
          return false;
        }

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
    top: -10px;
    width: 100%;
    height: calc(100% + 10px);
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
          background-color: #EFF7FF;
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
      background-color: #D9DEE7;
      overflow: hidden;
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