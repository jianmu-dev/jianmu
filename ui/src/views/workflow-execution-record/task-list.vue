<template>
  <div class="task-list" v-if="tasks.length > 1">
    <jm-dropdown placement="bottom-start" trigger="hover" class="task-dropdown">
          <span class="el-dropdown-link">
            #{{ currentDropdown }}<i class="el-icon-arrow-down el-icon--right"></i>
          </span>
      <template #dropdown>
        <jm-dropdown-menu style="height:186px;">
          <jm-dropdown-item v-for="item in tasks" :key="item.instanceId" @click="getParams(item.instanceId)"
                            style="padding:0 20px;">
            <div style="display: flex;color:#606266;">
              <div style="width:26px;">#{{ projectIdList.indexOf(item.instanceId) + 1 }}</div>
              <div style="margin:0 30px;width:45px;">
                <span v-if="item.status === TaskStatusEnum.SUCCEEDED"
                      style="color:#51C41B;">成功</span>
                <span v-if="item.status === TaskStatusEnum.FAILED"
                      style="color:#FF4D4F;">失败</span>
                <span v-if="item.status === TaskStatusEnum.SKIPPED"
                      style="color:#979797;">已跳过</span>
                <span v-if="item.status === TaskStatusEnum.RUNNING"
                      style="color:#11C2C2;">执行中</span>
                <span v-if="item.status === TaskStatusEnum.INIT"
                      style="color:#096DD9;">待启动</span>
                <span v-if="item.status === TaskStatusEnum.WAITING"
                      style="color:#FF862B">排队中</span>
              </div>
              <div style="text-align:right;"
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
    taskParma: {
      type: Array,
      required: true,
    },
  },
  emits: ['current-id', 'status-params'],
  setup(props, { emit }) {
    const projectIdList = ref<string[]>([]);
    const statusParams = ref<{ total: number; successNum: number; failNum: number; skipNum: number }>({
      total: 0,
      successNum: 0,
      failNum: 0,
      skipNum: 0,
    });

    // 当前选中
    const currentDropdown = ref<number>(0);
    // 最新节点id
    const newId = ref<string>('');
    const tasks = ref<ITaskExecutionRecordVo[]>(props.taskParma);

    // 获取状态信息，返回父组件
    const getTaskInfo = () => {
      // 循环获取以上参数
      tasks.value.forEach(item => {
        // 存储所有id
        projectIdList.value.push(item.instanceId);
        // 循环获取执行次数
        if (item.status === TaskStatusEnum.SUCCEEDED) {
          statusParams.value.successNum++;
        } else if (item.status === TaskStatusEnum.FAILED) {
          statusParams.value.failNum++;
        } else if (item.status === TaskStatusEnum.SKIPPED) {
          statusParams.value.skipNum++;
        }
      });
      // 初始化执行次数
      currentDropdown.value = projectIdList.value.length;
      statusParams.value.total = projectIdList.value.length;
      newId.value = projectIdList.value[0];
      // 反转数组，最后执行的数据在最前面
      projectIdList.value = projectIdList.value.reverse();
      emit('status-params', statusParams.value);
      // 获取最新id
      emit('current-id', newId.value);
    };

    // 页面初始化
    getTaskInfo();

    // 点击item时传递id到父组件
    const getParams = (id: string) => {
      // 获取当前选中
      projectIdList.value.forEach(item => {
        if (item === id) {
          currentDropdown.value = projectIdList.value.indexOf(item) + 1;
        }
      });
      emit('current-id', id);
    };
    onUpdated(() => {
      if (props.taskParma.length) {
        tasks.value = props.taskParma as ITaskExecutionRecordVo[];
        // 初始化
        projectIdList.value = [];
        statusParams.value = { successNum: 0, failNum: 0, skipNum: 0, total: 0 };
        // 重新获取信息
        getTaskInfo();
      }
    });
    return {
      tasks,
      projectIdList,
      TaskStatusEnum,
      executionTimeFormatter,
      currentDropdown,
      getParams,
    };
  },
});
</script>

<style scoped lang="less">
.task-list {
  position: absolute;
  right: 0;
  top: 9px;
  z-index: 2;
}
</style>