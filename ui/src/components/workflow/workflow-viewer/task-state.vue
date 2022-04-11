<template>
  <div class="task-state">
    <div class="signal" :style="{ backgroundColor: signal }"></div>
    <div class="label">{{ label }}{{ amount === 0 ? '' : ` ${amount}` }}</div>
  </div>
</template>

<script lang="ts">
import { defineComponent, onUpdated, ref, SetupContext } from 'vue';
import { TaskStatusEnum } from '@/api/dto/enumeration';

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
    status: {
      type: String,
      required: true,
    },
    count: {
      type: Number,
      required: true,
    },
  },
  emit: ['change'],
  setup(props, { emit }: SetupContext) {
    const state = states[props.status];
    const { signal, label } = state;

    const amount = ref<number>(props.count);
    onUpdated(() => {
      if (amount.value === props.count) {
        return;
      }
      amount.value = props.count;
      emit('change');
    });

    return {
      amount,
      signal,
      label,
    };
  },
});
</script>

<style scoped lang="less">
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

  .label {

  }
}
</style>