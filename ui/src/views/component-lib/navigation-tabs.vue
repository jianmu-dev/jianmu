<template>
  <div class="navigation-tabs">
    标签页：
    <jm-tabs v-model="activeName" @tab-click="handleClick">
      <jm-tab-pane name="workflow-mode">
        <template #label>
          <span><i class="jm-icon-tab-flowchart"></i> 流程图模式</span>
        </template>
        -- 流程图模式 --
      </jm-tab-pane>
      <jm-tab-pane name="dsl-mode">
        <template #label>
          <span><i class="jm-icon-tab-dsl"></i> DSL模式</span>
        </template>
        -- DSL模式 --
      </jm-tab-pane>
      <jm-tab-pane name="workflow-definition">
        <template #label>
          <span><i class="jm-icon-tab-workflow"></i> 流程定义</span>
        </template>
        -- 流程定义 --
      </jm-tab-pane>
      <jm-tab-pane name="task-execution-record">
        <template #label>
          <span><i class="jm-icon-tab-record"></i> 任务执行记录</span>
        </template>
        -- 任务执行记录 --
      </jm-tab-pane>
      <jm-tab-pane name="executing">
        <template #label>
          <span><i class="jm-icon-tab-executing"></i> 执行中</span>
        </template>
        -- 执行中 --
      </jm-tab-pane>
      <jm-tab-pane name="complete">
        <template #label>
          <span><i class="jm-icon-tab-complete"></i> 执行完成</span>
        </template>
        -- 执行完成 --
      </jm-tab-pane>
    </jm-tabs>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';

export default defineComponent({
  setup() {
    return {
      activeName: ref('workflow-definition'),
      handleClick: function (tab, event) {
        console.log(tab, event);
      },
    };
  },
});
</script>

<style scoped lang="less">
.navigation-tabs {

}
</style>