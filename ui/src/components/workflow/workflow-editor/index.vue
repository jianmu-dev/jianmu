<template>
  <div class="jm-workflow-editor">
    <template v-if="graph">
      <toolbar :workflow-data="workflowData"/>
      <node-config-panel
        v-if="selectedNodeData"
        v-model="nodeConfigPanelVisible"
        :node-data="selectedNodeData"
        @closed="handleNodeConfigPanelClosed"/>
    </template>
    <div class="main">
      <node-panel v-if="graph" @node-selected="handleNodeSelected"/>
      <graph-panel :workflow-data="workflowData"
                   @graph-created="handleGraphCreated"
                   @node-selected="handleNodeSelected"/>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType, provide, ref } from 'vue';
import { cloneDeep } from 'lodash';
import Toolbar from './layout/top/toolbar.vue';
import NodePanel from './layout/left/node-panel.vue';
import NodeConfigPanel from './layout/right/node-config-panel.vue';
import GraphPanel from './layout/main/graph-panel.vue';
import { IWorkflow, IWorkflowNode } from './model/data/common';
import { Graph } from '@antv/x6';
import registerCustomVueShape from './shape/custom-vue-shape';
import { WorkflowValidator } from './model/workflow-validator';

// 注册自定义x6元素
registerCustomVueShape();

export default defineComponent({
  name: 'jm-workflow-editor',
  components: { Toolbar, NodePanel, NodeConfigPanel, GraphPanel },
  props: {
    modelValue: Object as PropType<IWorkflow>,
  },
  emits: ['update:model-value'],
  setup(props, { emit }) {
    const workflowData = ref<IWorkflow>(props.modelValue ? cloneDeep(props.modelValue) : {
      name: '未命名项目',
      groupId: '',
      global: {
        concurrent: false,
      },
      data: '',
    });
    const graph = ref<Graph>();
    const nodeConfigPanelVisible = ref<boolean>(false);
    const selectedNodeData = ref<IWorkflowNode>();
    let workflowValidator: WorkflowValidator;

    provide('getGraph', (): Graph => graph.value!);
    provide('getWorkflowValidator', (): WorkflowValidator => workflowValidator!);

    return {
      workflowData,
      graph,
      nodeConfigPanelVisible,
      selectedNodeData,
      handleGraphCreated: (g: Graph) => {
        workflowValidator = new WorkflowValidator(g);
        graph.value = g;
      },
      handleNodeSelected: (data: IWorkflowNode) => {
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
  display: flex;
  flex-direction: column;
  background-color: #F0F2F5;
  user-select: none;

  .main {
    position: relative;
    height: calc(100% - @tool-bar-height);
  }
}
</style>