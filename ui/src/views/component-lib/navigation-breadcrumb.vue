<template>
  <div class="navigation-breadcrumb">
    <div>面包屑：</div>
    <jm-breadcrumb>
      <jm-breadcrumb-item to="/">首页</jm-breadcrumb-item>
      <jm-breadcrumb-item to="/1">任务定义</jm-breadcrumb-item>
      <jm-breadcrumb-item>详情</jm-breadcrumb-item>
    </jm-breadcrumb>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({});
</script>

<style scoped lang="less">
.navigation-breadcrumb {
  display: flex;
  align-items: center;
}
</style>