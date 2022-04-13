<template>
  <jm-state class="workflow-execution-record-task-state" v-if="status === 'INIT'" type="info">
    <template #default>待启动<span v-if="count" class="count">{{ count }}</span></template>
  </jm-state>
  <jm-state class="workflow-execution-record-task-state" v-else-if="status === 'WAITING'" custom-signal-color="#FF862B">
    <template #default>排队中<span v-if="count" class="count">{{ count }}</span></template>
  </jm-state>
  <jm-state class="workflow-execution-record-task-state" v-else-if="status === 'RUNNING'" custom-signal-color="#11C2C2">
    <template #default>执行中<span v-if="count" class="count">{{ count }}</span></template>
  </jm-state>
  <jm-state class="workflow-execution-record-task-state" v-else-if="status === 'SKIPPED'" type="warning">
    <template #default>已跳过<span v-if="count" class="count">{{ count }}</span></template>
  </jm-state>
  <jm-state class="workflow-execution-record-task-state" v-else-if="status === 'FAILED'" type="error">
    <template #default>执行失败<span v-if="count" class="count">{{ count }}</span></template>
  </jm-state>
  <jm-state class="workflow-execution-record-task-state" v-else-if="status === 'SUCCEEDED'" type="success">
    <template #default>执行成功<span v-if="count" class="count">{{ count }}</span></template>
  </jm-state>
  <jm-state class="workflow-execution-record-task-state" v-else-if="status === 'SUSPENDED'"
            custom-signal-color="#7986CB">
    <template #default>已挂起<span v-if="count" class="count">{{ count }}</span></template>
  </jm-state>
  <jm-state class="workflow-execution-record-task-state" v-else-if="status === 'IGNORED'"
            custom-signal-color="#9847FC">
    <template #default>已忽略<span v-if="count" class="count">{{ count }}</span></template>
  </jm-state>
  <!--            <jm-state class="workflow-execution-record-task-state" custom-signal-color="#FF862B">排队中</jm-state>-->
  <!--            <jm-state class="workflow-execution-record-task-state" custom-signal-color="#9013FE">调度失败</jm-state>-->
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  props: {
    status: String,
    count: Number,
  },
});
</script>

<style scoped lang="less">
.workflow-execution-record-task-state {
  width: 70px;

  .count {
    padding-left: 10px;
  }
}
</style>