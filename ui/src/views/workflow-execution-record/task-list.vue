<template>
  <div class="task-list" v-if="tasks.length > 1">
    <jm-dropdown placement="bottom-start" trigger="hover" class="task-dropdown" max-height="186px"
                 :append-to-body="false">
          <span class="el-dropdown-link">
            #{{ currentDropdown }}<i class="el-icon-arrow-down el-icon--right"></i>
          </span>
      <template #dropdown>
        <jm-dropdown-menu>
          <jm-dropdown-item v-for="item in tasks" :key="item.instanceId" @click="getParams(item.instanceId)"
                            style="padding:0 20px;">
            <div style="display: flex;color:#606266;">
              <div style="width:26px;">#{{ tasksList.indexOf(item.instanceId) + 1 }}</div>
              <jm-task-state :value="item.status"/>
              <div style="text-align:right; white-space: nowrap;"
                   v-if="!(item.status === TaskStatusEnum.RUNNING || item.status === TaskStatusEnum.INIT || item.status === TaskStatusEnum.WAITING)">
                {{ executionTimeFormatter(item.startTime, item.endTime) }}
              </div>
            </div>
          </jm-dropdown-item>
        </jm-dropdown-menu>
      </template>
    </jm-dropdown>
  </div>
</template>

<script lang="ts">
import { defineComponent, onUpdated, ref } from 'vue';
import { ITaskExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { TaskStatusEnum } from '@/api/dto/enumeration';
import { executionTimeFormatter } from '@/utils/formatter';

export default defineComponent({
  props: {
    taskParams: {
      type: Array,
      required: true,
    },
  },
  emits: ['change', 'status-params'],
  setup(props, { emit }) {
    const tasksList = ref<string[]>([]);

    // 当前选中
    const currentDropdown = ref<number>(0);
    // 最新节点id
    const tasks = ref<ITaskExecutionRecordVo[]>(props.taskParams);
    const status = ref<string>('');
    // 初始化列表
    const initList = () => {
      tasks.value.forEach(item => {
        tasksList.value.push(item.instanceId);
      });
      // 初始化执行次数
      currentDropdown.value = tasksList.value.length;
      // 更新到最新id获取日志参数
      emit('change', tasksList.value[0]);
      tasksList.value = tasksList.value.reverse();
    };
    initList();

    // 监听变化更新列表
    onUpdated(() => {
      // 判断是更改id-不初始化
      if (status.value === 'changeId') {
        return;
      }
      // 触发项目动态更新task列表
      if (props.taskParams.length !== tasks.value.length) {
        tasks.value = props.taskParams as ITaskExecutionRecordVo[];
        tasksList.value = [];
        // 初始化
        initList();
        return;
      }
    });

    // 点击item时传递id到父组件
    const getParams = (id: string) => {
      status.value = 'changeId';
      // 获取当前选中
      tasksList.value.forEach(item => {
        if (item !== id) {
          return;
        }
        currentDropdown.value = tasksList.value.indexOf(item) + 1;
        emit('change', id);
      });
    };
    return {
      tasks,
      tasksList,
      TaskStatusEnum,
      executionTimeFormatter,
      currentDropdown,
      getParams,
    };
  },
});
</script>
<style lang="less" scoped>
.jm-task-state {
  margin: 0 35px 0 30px;
}
</style>