<template>
  <div class="jm-workflow-editor">
    <template v-if="graph">
      <toolbar
        :workflow-data="workflowData"
        @back="handleBack" @save="handleSave"
        @open-global-drawer="handleGlobalParamPanelClosed"/>
      <node-config-panel
        v-if="selectedNodeId"
        v-model="nodeConfigPanelVisible"
        :node-id="selectedNodeId"
        :node-waring-clicked="nodeWaringClicked"
        modal-class="node-config-panel-overlay"
        @closed="handleNodeConfigPanelClosed"/>
      <global-param-panel
        v-if="globalDrawerVisible"
        v-model="globalDrawerVisible"
        :workflow-data="workflowData"
        modal-class="node-config-panel-overlay"
        @closed="handleGlobalParamPanelClosed"/>
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
import { defineComponent, getCurrentInstance, PropType, provide, ref } from 'vue';
import { cloneDeep } from 'lodash';
import Toolbar from './layout/top/toolbar.vue';
import NodePanel from './layout/left/node-panel.vue';
import NodeConfigPanel from './layout/right/node-config-panel.vue';
import GraphPanel from './layout/main/graph-panel.vue';
import GlobalParamPanel from './layout/right/global-param-panel.vue';
import { IWorkflow } from './model/data/common';
import { Graph, Node } from '@antv/x6';
import registerCustomVueShape from './shape/custom-vue-shape';
import { WorkflowValidator } from './model/workflow-validator';
import { ISelectableParam } from '../workflow-expression-editor/model/data';
import { buildSelectableExtParam, buildSelectableGlobalParam } from './model/data/global-param';

// 注册自定义x6元素
registerCustomVueShape();

export default defineComponent({
  name: 'jm-workflow-editor',
  components: { Toolbar, NodePanel, NodeConfigPanel, GraphPanel, GlobalParamPanel },
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
    const globalDrawerVisible = ref<boolean>(false);
    let workflowValidator: WorkflowValidator;
    let checkGlobalParams: () => Promise<void>;

    provide('getGraph', (): Graph => graph.value!);
    provide('getWorkflowValidator', (): WorkflowValidator => workflowValidator!);
    provide('buildSelectableGlobalParam', (): ISelectableParam | undefined => buildSelectableGlobalParam(workflowData.value.global.params));
    provide('buildSelectableExtParam', (): Promise<ISelectableParam | undefined> => buildSelectableExtParam());
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
      globalDrawerVisible,
      handleBack: () => {
        emit('back');
      },
      handleSave: async (back: boolean, dsl: string) => {
        // 必须克隆后发事件，否则外部的数据绑定会受影响
        emit('update:model-value', cloneDeep(workflowData.value));

        emit('save', back, dsl);
      },
      handleGraphCreated: (g: Graph) => {
        workflowValidator = new WorkflowValidator(workflowData.value, g, proxy);
        graph.value = g;
      },
      handleNodeSelected,
      handleNodeConfigPanelClosed: (valid: boolean) => {
        const selectedNode = graph.value!.getCellById(selectedNodeId.value) as Node;
        if (valid) {
          workflowValidator.removeWarning(selectedNode);
        } else {
          workflowValidator.addWarning(selectedNode, nodeId => {
            handleNodeSelected(nodeId, true);
          });
        }
        // 取消选中
        graph.value!.unselect(selectedNodeId.value);
        selectedNodeId.value = '';
      },
      // 全局参数显隐
      handleGlobalParamPanelClosed: async (visible: boolean, _checkGlobalParams: () => Promise<void>) => {
        globalDrawerVisible.value = visible;
        // 是否是关闭
        if (!visible) {
          await checkGlobalParams();
          return;
        }
        checkGlobalParams = _checkGlobalParams;
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
