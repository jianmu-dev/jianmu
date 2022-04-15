<template>
  <div class="task-list" v-if="tasks.length > 1">
    <jm-dropdown placement="bottom-start" trigger="hover" max-height="186px"
                 :append-to-body="false">
      <span>#{{ currentSelect === 0 ? tasks.length : currentSelect }}<i
        class="el-icon-arrow-down el-icon--right"></i></span>
      <template #dropdown>
        <jm-dropdown-menu>
          <jm-dropdown-item v-for="item in tasks" :key="item.instanceId" style="padding:0 20px;"
                            @click="getCurrentId(item)">
            <div style="display: flex;color:#606266;">
              <div style="width:26px;">#{{ tasks.length - tasks.indexOf(item) }}</div>
              <jm-task-state :value="item.status"/>
              <div style="text-align:right; white-space: nowrap;">{{
                  executionTimeFormatter(
                    item.startTime,
                    item.endTime,
                    item.status === TaskStatusEnum.RUNNING ||
                    item.status === TaskStatusEnum.INIT ||
                    item.status === TaskStatusEnum.WAITING ||
                    item.status === TaskStatusEnum.SUSPENDED)
                }}
              </div>
            </div>
          </jm-dropdown-item>
        </jm-dropdown-menu>
      </template>
    </jm-dropdown>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, PropType, ref } from 'vue';
import { ITaskExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { TaskStatusEnum } from '@/api/dto/enumeration';
import { executionTimeFormatter } from '@/utils/formatter';

export default defineComponent({
  props: {
    taskParams: {
      type: Array as PropType<ITaskExecutionRecordVo[]>,
      required: true,
    },
  },
  emits: ['change'],
  setup(props, { emit }) {
    const tasks = computed<ITaskExecutionRecordVo[]>(() => props.taskParams);
    // 当前选择
    const currentSelect = ref<number>(tasks.value.length);
    return {
      tasks,
      TaskStatusEnum,
      executionTimeFormatter,
      currentSelect,
      getCurrentId: (current: ITaskExecutionRecordVo) => {
        currentSelect.value = tasks.value.length - tasks.value.indexOf(current);
        emit('change', tasks.value[tasks.value.indexOf(current)].instanceId);
      },
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