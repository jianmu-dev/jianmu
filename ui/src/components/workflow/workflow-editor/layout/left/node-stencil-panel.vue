<template>
  <div class="jm-workflow-editor-node-stencil-panel stencil" ref="container">
  </div>
</template>

<script lang="ts">
import { defineComponent, inject, onMounted, ref } from 'vue';
import { Graph } from '@antv/x6';
import WorkflowStencil from '@/components/workflow/workflow-editor/model/workflow-stencil';

export default defineComponent({
  setup(props, context) {
    const container = ref<HTMLElement>();

    onMounted(() => {
      const getX6Graph = inject('getX6Graph') as () => Graph;

      // 初始化 stencil
      const workflowStencil = new WorkflowStencil(getX6Graph(), container.value!);
    });

    return {
      container,
    };
  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-node-stencil-panel {
  width: 300px;
  position: relative;
}
</style>