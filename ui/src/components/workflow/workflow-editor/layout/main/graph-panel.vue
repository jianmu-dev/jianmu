<template>
  <div class="jm-workflow-editor-graph-panel" @wheel.prevent="wheelScroll">
    <div class="container" ref="container"></div>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, PropType, ref } from 'vue';
import { KeyValue } from '@antv/x6/es/types';
import { HistoryManager } from '@antv/x6/es/graph/history';
import WorkflowGraph from '../../model/workflow-graph';
import { IWorkflowData } from '../../model/data';
import Command = HistoryManager.Command;

export default defineComponent({
  props: {
    modelValue: Object as PropType<IWorkflowData>,
  },
  emits: ['update:model-value', 'graph-created', 'node-selected'],
  setup(props, { emit }) {
    const container = ref<HTMLElement>();
    let workflowGraph: WorkflowGraph;

    onMounted(() => {
      // 初始化画布
      workflowGraph = new WorkflowGraph(container.value!);
      const graph = workflowGraph.x6Graph;

      // 单击节点事件
      graph.on('node:click', ({ e, x, y, node, view }) => {
        emit('node-selected', node.getData());
      });

      // 画布变化事件
      graph.history.on('change', (args: {
        cmds: Command[] | null
        options: KeyValue
      }) => {
        // code here
        emit('update:model-value', graph.toJSON());
      });

      emit('graph-created', graph);
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
  position: relative;
  overflow: hidden;

  .container {
    position: absolute;
  }

  ::v-deep(.x6-graph-background) {
    // 遮挡折叠后的节点列表
    background-color: #FFFFFF;
  }
}
</style>