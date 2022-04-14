<template>
  <span :class="{
    'jm-task-state':true,
     [value.toLowerCase()]:true,
  }">{{ text }}</span>
</template>

<script lang="ts">
import { computed, defineComponent, PropType } from 'vue';
import { TaskStatusEnum } from '@/api/dto/enumeration';

export default defineComponent({
  name: 'jm-task-state',
  props: {
    value: {
      type: String as PropType<TaskStatusEnum>,
      default: TaskStatusEnum.INIT,
    },
  },
  setup(props: any) {
    return {
      TaskStatusEnum,
      text: computed<string>(() => {
        let stateText = '';
        switch (props.value) {
          case TaskStatusEnum.INIT:
            stateText = '待启动';
            break;
          case TaskStatusEnum.SUCCEEDED:
            stateText = '成功';
            break;
          case TaskStatusEnum.IGNORED:
            stateText = '已忽略';
            break;
          case TaskStatusEnum.FAILED:
            stateText = '失败';
            break;
          case TaskStatusEnum.SKIPPED:
            stateText = '已跳过';
            break;
          case TaskStatusEnum.SUSPENDED:
            stateText = '已挂起';
            break;
          case TaskStatusEnum.RUNNING:
            stateText = '执行中';
            break;
          case TaskStatusEnum.WAITING:
            stateText = '排队中';
            break;
        }
        return stateText;
      }),
    };
  },
});
</script>

<style lang="less">
.jm-task-state {
  //  待启动
  &.init {
    color: #096DD9;
  }

  //  排队中
  &&.waiting {
    color: #FF862B;
  }

  //  执行中
  &.running {
    color: #11C2C2;
  }

  //  已跳过
  &.skipped {
    color: #979797;
  }

  //  执行失败
  &.failed {
    color: #FF4D4F;
  }

  //  执行成功
  &.succeeded {
    color: #51C41B;
  }

  //  已挂起
  &.suspended {
    color: #7986CB;
  }

  //  已忽略
  &.ignored {
    color: #9847FC;
  }
}
</style>