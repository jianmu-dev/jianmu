<template>
  <div class="jm-workflow-editor-graph-panel" ref="container" @wheel.prevent="wheelScroll"/>
</template>

<script lang="ts">
import { defineComponent, onMounted, PropType, ref } from 'vue';
import WorkflowGraph from '../../model/workflow-graph';
import { IWorkflow, IWorkflowNode } from '../../model/data/common';

export default defineComponent({
  props: {
    modelValue: Object as PropType<IWorkflow>,
  },
  emits: ['update:model-value', 'graph-created', 'node-selected'],
  setup(props, { emit }) {
    const container = ref<HTMLElement>();
    let workflowGraph: WorkflowGraph;

    onMounted(() => {
      // 初始化画布
      workflowGraph = new WorkflowGraph(container.value!,
        (data: IWorkflowNode) => emit('node-selected', data));

      emit('graph-created', workflowGraph.x6Graph);
    });

    return {
      container,
      wheelScroll(e: WheelEvent) {
        if (!workflowGraph) {
          return;
        }

        workflowGraph.wheelScrollContainer(e);
      },
    };
  },
});
</script>

<style scoped lang="less">
@import '../../vars';

.jm-workflow-editor-graph-panel {
  // 铺满剩余宽度
  flex-grow: 1;
}
</style>