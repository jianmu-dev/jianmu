<template>
  <div class="jm-workflow-editor">
    <template v-if="graph">
      <toolbar :workflow-data="workflowData" @back="handleBack" @save="handleSave" @open-cache-panel="openCachePanel" />
      <node-config-panel
        v-if="selectedNodeId"
        v-model="nodeConfigPanelVisible"
        :node-id="selectedNodeId"
        :node-waring-clicked="nodeWaringClicked"
        :workflow-data="workflowData"
        modal-class="node-config-panel-overlay"
        @closed="handleNodeConfigPanelClosed"
      />
      <cache-panel
        modal-class="node-config-panel-overlay"
        v-model="cachePanelVisible"
        :workflow-data="workflowData"
        @closed="handleCachePanel"
      />
    </template>
    <div class="main">
      <node-panel v-if="graph" @node-selected="nodeId => handleNodeSelected(nodeId, true)" />
      <graph-panel
        :workflow-data="workflowData"
        @graph-created="handleGraphCreated"
        @node-selected="nodeId => handleNodeSelected(nodeId, false)"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, PropType, provide, ref } from 'vue';
import { cloneDeep } from 'lodash';
import Toolbar from './layout/top/toolbar.vue';
import NodePanel from './layout/left/node-panel.vue';
import NodeConfigPanel from './layout/right/node-config-panel.vue';
import CachePanel from './layout/right/cache-panel.vue';
import GraphPanel from './layout/main/graph-panel.vue';
import { IWorkflow } from './model/data/common';
// eslint-disable-next-line no-redeclare
import { Graph, Node } from '@antv/x6';
import registerCustomVueShape from './shape/custom-vue-shape';
import { WorkflowValidator } from './model/workflow-validator';
import { CustomX6NodeProxy } from '@/components/workflow/workflow-editor/model/data/custom-x6-node-proxy';

// 注册自定义x6元素
registerCustomVueShape();

export default defineComponent({
  name: 'jm-workflow-editor',
  components: { Toolbar, NodePanel, NodeConfigPanel, GraphPanel, CachePanel },
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
    let checkCache: () => Promise<void>;
    const nodeWaringClicked = ref<boolean>(false);
    const cachePanelVisible = ref<boolean>(false);
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
        workflowValidator = new WorkflowValidator(g, proxy, workflowData.value);
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
      cachePanelVisible,
      handleCachePanel: async () => {
        await checkCache();
        const nodes = graph.value!.getNodes();
        for (const node of nodes) {
          const workflowNode = new CustomX6NodeProxy(node).getData(graph.value, workflowData.value);
          try {
            await workflowNode.validate();
            workflowValidator.removeWarning(node);
          } catch ({ errors }) {
            workflowValidator.addWarning(node, nodeId => {
              handleNodeSelected(nodeId, true);
            });
          }
        }
      },
      openCachePanel: (_checkCache: () => Promise<void>) => {
        checkCache = _checkCache;
        cachePanelVisible.value = true;
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
  background-color: #f0f2f5;
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
