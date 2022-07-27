<template>
  <div class="task-list" v-if="tasks.length > 1">
    <jm-dropdown placement="bottom-start" trigger="hover" max-height="186px"
                 :append-to-body="false">
      <span>#{{ tasks.length - selectedIndex }}<i
        class="el-icon-arrow-down el-icon--right"></i></span>
      <template #dropdown>
        <jm-dropdown-menu>
          <jm-dropdown-item v-for="item in tasks" :key="item.instanceId" style="padding:0 20px;"
                            @click="changeTask(item)">
            <div style="display: flex;color:#606266;">
              <div style="width:26px;">#{{ tasks.length - tasks.indexOf(item) }}</div>
              <jm-task-state :value="item.status"/>
              <div style="min-width: 100px">
                <jm-timer :start-time="item.startTime" :end-time="item.endTime" :tip-append-to-body="false"/>
              </div>
            </div>
          </jm-dropdown-item>
        </jm-dropdown-menu>
      </template>
    </jm-dropdown>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onUpdated, PropType, ref } from 'vue';
import { ITaskExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { TaskStatusEnum } from '@/api/dto/enumeration';

export default defineComponent({
  props: {
    tasks: {
      type: Array as PropType<ITaskExecutionRecordVo[]>,
      required: true,
    },
  },
  emits: ['change'],
  setup(props, { emit }) {
    // 当前选择
    const selectedTask = ref<ITaskExecutionRecordVo>();
    const latestTask = computed<ITaskExecutionRecordVo | undefined>(() => {
      if (props.tasks.length === 0) {
        return undefined;
      }
      return props.tasks[0];
    });

    const changeTask = (t: ITaskExecutionRecordVo) => {
      selectedTask.value = { ...t };
      emit('change', selectedTask.value.instanceId);
    };
    onUpdated(() => {
      if (latestTask.value && !selectedTask.value) {
        changeTask(latestTask.value);
      }
    });
    return {
      TaskStatusEnum,
      changeTask,
      selectedIndex: computed<number>(() => {
        return props.tasks.findIndex(({ instanceId }) => instanceId === selectedTask.value?.instanceId);
      }),
    };
  },
});
</script>
<style lang="less" scoped>
.task-list {
  .jm-task-state {
    width: 50px;
    margin: 0 30px;
  }
}
</style>
