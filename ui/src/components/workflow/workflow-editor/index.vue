<template>
  <div class="jm-workflow-editor">
    <template v-if="graph">
      <toolbar :workflow-data="workflowData" @back="handleBack" @save="handleSave"/>
      <node-config-panel
        v-if="selectedNodeId"
        v-model="nodeConfigPanelVisible"
        :node-id="selectedNodeId"
        :node-waring-clicked="nodeWaringClicked"
        modal-class="node-config-panel-overlay"
        @closed="handleNodeConfigPanelClosed"/>
    </template>
    <div class="main">
      <node-panel v-if="graph" @node-selected="nodeId => handleNodeSelected(nodeId, true)"/>
      <graph-panel :workflow-data="workflowData"
                   @graph-created="handleGraphCreated"
                   @node-selected="nodeId => handleNodeSelected(nodeId, false)"/>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, nextTick, PropType, provide, ref } from 'vue';
import { cloneDeep } from 'lodash';
import Toolbar from './layout/top/toolbar.vue';
import NodePanel from './layout/left/node-panel.vue';
import NodeConfigPanel from './layout/right/node-config-panel.vue';
import GraphPanel from './layout/main/graph-panel.vue';
import { IWorkflow } from './model/data/common';
import { Graph } from '@antv/x6';
import registerCustomVueShape from './shape/custom-vue-shape';
import { WorkflowValidator } from './model/workflow-validator';

// 注册自定义x6元素
registerCustomVueShape();

export default defineComponent({
  name: 'jm-workflow-editor',
  components: { Toolbar, NodePanel, NodeConfigPanel, GraphPanel },
  props: {
    modelValue: {
      type: Object as PropType<IWorkflow>,
      required: true,
    },
  },
  emits: ['update:model-value', 'back', 'save'],
  setup(props, { emit }) {
    const { proxy } = getCurrentInstance() as any;
    const workflowData = ref<IWorkflow>(cloneDeep(props.modelValue));
    const graph = ref<Graph>();
    const nodeConfigPanelVisible = ref<boolean>(false);
    const selectedNodeId = ref<string>('');
    const nodeWaringClicked = ref<boolean>(false);
    let workflowValidator: WorkflowValidator;

    provide('getGraph', (): Graph => graph.value!);
    provide('getWorkflowValidator', (): WorkflowValidator => workflowValidator!);
    const handleNodeSelected = async (nodeId: string, waringClicked: boolean) => {
      nodeConfigPanelVisible.value = true;
      selectedNodeId.value = nodeId;
      nodeWaringClicked.value = waringClicked;
    };
    return {
      workflowData,
      graph,
      nodeConfigPanelVisible,
      selectedNodeId,
      nodeWaringClicked,
      handleBack: () => {
        emit('back');
      },
      handleSave: async (back: boolean, dsl: string) => {
        // 必须克隆后发事件，否则外部的数据绑定会受影响
        emit('update:model-value', cloneDeep(workflowData.value));

        emit('save', back, dsl);
      },
      handleGraphCreated: (g: Graph) => {
        workflowValidator = new WorkflowValidator(g, proxy);
        graph.value = g;
      },
      handleNodeSelected,
      handleNodeConfigPanelClosed: (valid: boolean) => {
        if (valid) {
          workflowValidator.removeWarning(selectedNodeId.value);
        } else {
          workflowValidator.addWarning(selectedNodeId.value, nodeId => {
            handleNodeSelected(nodeId, true);
          });
        }
        // 取消选中
        graph.value!.unselect(selectedNodeId.value);
        selectedNodeId.value = '';
      },
    };
  },
});
</script>

<style lang="less">
@import './vars';

.jm-workflow-editor {
  @import './theme/x6';
  @import './theme/el';

  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #F0F2F5;
  user-select: none;
  -moz-user-select: none;
  -webkit-user-select: none;
  -ms-user-select: none;

  .node-config-panel-overlay {
    background-color: transparent;
    //cursor: not-allowed;
  }

  .main {
    position: relative;
    z-index: 1;
    height: calc(100% - @tool-bar-height);
  }
}
</style>
