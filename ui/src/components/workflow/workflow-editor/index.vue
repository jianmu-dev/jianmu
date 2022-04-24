<template>
  <div class="jm-workflow-editor">
    <template v-if="graph">
      <toolbar/>
      <node-config-panel
        v-if="selectedNodeData"
        v-model="nodeConfigPanelVisible"
        :node-data="selectedNodeData"
        @closed="handleNodeConfigPanelClosed"/>
    </template>
    <div class="main">
      <node-panel v-if="graph"/>
      <graph-panel :model-value="modelValue"
                   @update:model-value="handleModelValueUpdated"
                   @graph-created="handleGraphCreated"
                   @node-selected="handleNodeSelected"/>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType, provide, ref } from 'vue';
import Toolbar from './layout/top/toolbar.vue';
import NodePanel from './layout/left/node-panel.vue';
import NodeConfigPanel from './layout/right/node-config-panel.vue';
import GraphPanel from './layout/main/graph-panel.vue';
import { INodeData, IWorkflowData } from './model/data';
import { Graph } from '@antv/x6';
import registerCustomVueShape from './shape/custom-vue-shape';

// 注册自定义x6元素
registerCustomVueShape();

export default defineComponent({
  name: 'jm-workflow-editor',
  components: { Toolbar, NodePanel, NodeConfigPanel, GraphPanel },
  props: {
    modelValue: Object as PropType<IWorkflowData>,
  },
  emits: ['update:model-value'],
  setup(props, { emit }) {
    const graph = ref<Graph>();
    const nodeConfigPanelVisible = ref<boolean>(false);
    const selectedNodeData = ref<INodeData>();

    provide('getGraph', (): Graph => graph.value);

    return {
      graph,
      nodeConfigPanelVisible,
      selectedNodeData,
      handleModelValueUpdated: (newVal: IWorkflowData) => {
        emit('update:model-value', newVal);
      },
      handleGraphCreated: (g: Graph) => {
        graph.value = g;
      },
      handleNodeSelected: (data: INodeData) => {
        nodeConfigPanelVisible.value = true;
        selectedNodeData.value = data;
      },
      handleNodeConfigPanelClosed: () => {
        selectedNodeData.value = undefined;
      },
    };
  },
});
</script>

<style lang="less">
@import './vars';

.jm-workflow-editor {
  width: 100%;
  height: 100%;
  border: 10px solid #dfe3e8;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;

  .main {
    display: flex;
    // 铺满剩余高度
    flex-grow: 1;
  }
}
</style>