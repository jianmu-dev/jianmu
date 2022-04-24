<template>
  <div class="jm-workflow-editor">
    <template v-if="container">
      <toolbar/>
      <node-config-panel/>
    </template>
    <div class="main">
      <node-stencil-panel v-if="container"/>
      <div class="graph-container">
        <div ref="container"></div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, PropType, provide, ref } from 'vue';
import Toolbar from './layout/top/toolbar.vue';
import NodeStencilPanel from './layout/left/node-stencil-panel.vue';
import NodeConfigPanel from './layout/right/node-config-panel.vue';
import WorkflowGraph from './model/workflow-graph';
import { IWorkflowData } from './model/data';
import { KeyValue } from '@antv/x6/es/types';
import { HistoryManager } from '@antv/x6/es/graph/history';
import { Graph } from '@antv/x6';
import registerCustomVueShape from './shape/custom-vue-shape';
import Command = HistoryManager.Command;

// 注册自定义x6元素
registerCustomVueShape();

export default defineComponent({
  name: 'jm-workflow-editor',
  components: { Toolbar, NodeStencilPanel, NodeConfigPanel },
  props: {
    modelValue: Object as PropType<IWorkflowData>,
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const data = ref<IWorkflowData>(props.modelValue);
    const container = ref<HTMLElement>();

    onMounted(() => {
      // 初始化画布
      const workflowGraph = new WorkflowGraph(container.value!);
      provide('getX6Graph', (): Graph => {
        return workflowGraph.x6Graph;
      });

      workflowGraph.x6Graph.history.on('change', (args: {
        cmds: Command[] | null
        options: KeyValue
      }) => {
        // code here
        emit('update:modelValue', workflowGraph.x6Graph.toJSON());
      });
    });

    return {
      container,
    };
  },
});
</script>

<style lang="less">
.jm-workflow-editor {
  width: 100%;
  height: 100%;
  border: 10px solid #dfe3e8;
  position: relative;
  box-sizing: border-box;

  .main {
    // 50px为工具栏高度
    height: calc(100% - 50px);
    display: flex;

    .graph-container {
      // 280px为左侧栏宽度
      width: calc(100% - 300px);
    }
  }
}
</style>