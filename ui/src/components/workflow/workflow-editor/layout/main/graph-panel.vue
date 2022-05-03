<template>
  <div class="jm-workflow-editor-graph-panel" ref="container" @wheel.prevent="wheelScroll"/>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, onMounted, PropType, ref } from 'vue';
import WorkflowGraph from '../../model/workflow-graph';
import { IWorkflow } from '../../model/data/common';

export default defineComponent({
  props: {
    workflowData: {
      type: Object as PropType<IWorkflow>,
      required: true,
    },
  },
  emits: ['graph-created', 'node-selected'],
  setup(props, { emit }) {
    const { proxy } = getCurrentInstance() as any;
    const container = ref<HTMLElement>();
    let workflowGraph: WorkflowGraph;

    onMounted(() => {
      // 初始化画布
      workflowGraph = new WorkflowGraph(proxy, container.value!,
        (nodeId: string) => emit('node-selected', nodeId));

      emit('graph-created', workflowGraph.x6Graph);
      // 渲染画布，回显
      workflowGraph.render(props.workflowData.data);
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