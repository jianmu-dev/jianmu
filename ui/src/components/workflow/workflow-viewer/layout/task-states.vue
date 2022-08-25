<template>
  <div class="task-states">
    <div
      v-for="({status, count}) in taskStates"
      :key="status"
      @mouseenter="$emit('mouse-event', status, true)"
      @mouseleave="$emit('mouse-event', status, false)"
      class="task-state"
    >
      <!-- @mouseenter="workflowGraph.highlightNodeState(status, true)"
      @mouseleave="workflowGraph.highlightNodeState(status, false)" -->
      <div class="signal" :style="{ backgroundColor: states[status].signal }"></div>
      <div class="label">{{ states[status].label }}{{ count === 0 ? '' : ` ${count}` }}</div>
    </div>
  </div>
</template>
<script lang="ts">
import { TaskStatusEnum } from '@/api/dto/enumeration';
import { defineComponent, PropType } from 'vue';

const states: {
  [key: string]: {
    signal: string,
    label: string,
  };
} = {
  [TaskStatusEnum.INIT]: {
    signal: '#096DD9',
    label: '待启动',
  },
  [TaskStatusEnum.WAITING]: {
    signal: '#FF862B',
    label: '排队中',
  },
  [TaskStatusEnum.RUNNING]: {
    signal: '#11C2C2',
    label: '执行中',
  },
  [TaskStatusEnum.SKIPPED]: {
    signal: '#979797',
    label: '已跳过',
  },
  [TaskStatusEnum.FAILED]: {
    signal: '#FF4D4F',
    label: '执行失败',
  },
  [TaskStatusEnum.SUCCEEDED]: {
    signal: '#51C41B',
    label: '执行成功',
  },
  [TaskStatusEnum.SUSPENDED]: {
    signal: '#7986CB',
    label: '已挂起',
  },
  [TaskStatusEnum.IGNORED]: {
    signal: '#9847FC',
    label: '已忽略',
  },
};
export default defineComponent({
  props: {
    // workflowGraph: {
    //   type: Object as PropType<WorkflowGraph>,
    //   required: true,
    // },
    taskStates: {
      type: Array as PropType<{
          status: string;
          count: number;
        }[]>,
      default: ()=>[],
    },
  },
  emit: ['change', 'mouse-event'],
  setup() {
    return {
      states,
    };
  },
});
</script>

<style lang="less">
.task-states {
  position: absolute;
  z-index: 1;
  bottom: 22px;
  left: 44px;
  background-color: rgba(255, 255, 255, 0.6);

  > div + div {
    margin-left: 16px;
  }
  .task-state {
    display: inline-flex;
    align-items: center;
    font-size: 14px;
    color: #595959;
    cursor: default;

    .signal {
      width: 16px;
      height: 16px;
      margin-right: 5px;
      border-radius: 2px;
      overflow: hidden;
    }
  }
}
</style>