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
                          style="color:#3EBB03;">成功</span>
                <span v-if="item.status === TaskStatusEnum.FAILED"
                      style="color:#CF1322;">失败</span>
                <span v-if="item.status === TaskStatusEnum.SKIPPED"
                      style="color:#979797;">已跳过</span>
              </div>
              <div style="text-align:right;">{{ getTimeDifference(item.startTime, item.endTime) }}</div>
            </div>
          </jm-dropdown-item>
        </jm-dropdown-menu>
      </template>
    </jm-dropdown>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { ITaskExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { TaskStatusEnum } from '@/api/dto/enumeration';

export default defineComponent({
  props: {
    taskParma: {
      type: Array,
      required: true,
    },
  },
  emits: ['current-id', 'total-params', 'success-params', 'fail-params', 'skip-params'],
  setup(props, { emit }) {
    const total = ref<number>(0);
    const successNum = ref<number>(0);
    const failNum = ref<number>(0);
    const skipNum = ref<number>(0);
    const projectIdList = ref<string[]>([]);

    // 当前选中
    const currentDropdown = ref<number>(0);
    const tasks = ref<ITaskExecutionRecordVo[]>(props.taskParma);

    // 循环获取以上参数
    props.taskParma.forEach(item => {
      // 存储所有id
      projectIdList.value.push(item.instanceId);
      // 循环获取执行次数
      if (item.status === TaskStatusEnum.SUCCEEDED) {
        successNum.value++;
      } else if (item.status === TaskStatusEnum.FAILED) {
        failNum.value++;
      } else if (item.status === TaskStatusEnum.SKIPPED) {
        skipNum.value++;
      }
    });
    // 初始化执行次数
    currentDropdown.value = projectIdList.value.length;
    total.value = projectIdList.value.length;
    // 反转数组，最后执行的数据在最前面
    projectIdList.value = projectIdList.value.reverse();
    emit('total-params', total.value);
    emit('success-params', successNum.value);
    emit('fail-params', failNum.value);
    emit('skip-params', skipNum.value);

    // 获取时间差
    const getTimeDifference = (startTime: string, endTime: string): string => {
      let str = '';
      // 两时间相减获取时间差
      const time = new Date(endTime).getTime() - new Date(startTime).getTime();
      // 获得小时
      let s = time / 1000;
      // 分
      let m = s / 60;
      // 时
      const h = m / 60;
      // 判断获取时分秒
      if (h >= 1) {
        str = (parseInt(h) + 'h' + ' ') + (parseInt(h - parseInt(h) * 60) + 'm' + ' ') + (Math.ceil(s) + 's');
      }
      if (m >= 1) {
        str = (parseInt(m) + 'm' + ' ') + Math.ceil((m - parseInt(m)) * 60) + 's';
      }
      if (m < 1) {
        str = Math.ceil(s) + 's';
      }
      return str;
    };

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
    return {
      tasks,
      projectIdList,
      TaskStatusEnum,
      getTimeDifference,
      currentDropdown,
      total,
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