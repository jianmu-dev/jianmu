<template>
  <div class="jm-workflow-editor-graph-panel">
    <node-toolbar ref="nodeToolbar"/>
    <div ref="container" @wheel.prevent="wheelScrollGraph"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, onMounted, onUnmounted, PropType, provide, ref } from 'vue';
import { WorkflowGraph } from '../../model/workflow-graph';
import { IWorkflow } from '../../model/data/common';
import NodeToolbar from './node-toolbar.vue';
import { WorkflowNodeToolbar } from '../../model/workflow-node-toolbar';

export default defineComponent({
  components: { NodeToolbar },
  props: {
    workflowData: {
      type: Object as PropType<IWorkflow>,
      required: true,
    },
  },
  emits: ['graph-created', 'node-selected'],
  setup(props, { emit }) {
    const { proxy } = getCurrentInstance() as any;
    const nodeToolbar = ref();
    const container = ref<HTMLElement>();
    let workflowGraph: WorkflowGraph;

    provide('getWorkflowNodeToolbar', (): WorkflowNodeToolbar => workflowGraph!.workflowNodeToolbar);

    onMounted(() => {
      // 初始化画布
      workflowGraph = new WorkflowGraph(proxy, container.value!,
        (nodeId: string) => emit('node-selected', nodeId));

      emit('graph-created', workflowGraph.x6Graph);
      // 渲染画布，回显
      workflowGraph.render(props.workflowData.data);
    });

    // 销毁画布
    onUnmounted(() => workflowGraph.destroy());

    return {
      nodeToolbar,
      container,
      wheelScrollGraph: (e: WheelEvent) => {
        if (!workflowGraph) {
          return;
        }

        workflowGraph.wheelScroll(e);
      },
    };
  },
});
</script>

<style scoped lang="less">
@import '../../vars';

.jm-workflow-editor-graph-panel {
  height: 100%;
}
</style>