<template>
  <div class="data-state">
    状态：
    <jm-state>默认</jm-state>
    <jm-state type="info">待启动</jm-state>
    <jm-state custom-signal-color="#FF862B">排队中</jm-state>
    <jm-state custom-signal-color="#11C2C2">执行中</jm-state>
    <jm-state type="success">执行成功</jm-state>
    <jm-state custom-signal-color="#9013FE">调度失败</jm-state>
    <jm-state type="warning">已跳过</jm-state>
    <jm-state type="error">执行失败</jm-state>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({});
</script>

<style scoped lang="less">
.data-state {
  > :nth-child(n) {
    margin: 0 10px;
  }
}
</style>