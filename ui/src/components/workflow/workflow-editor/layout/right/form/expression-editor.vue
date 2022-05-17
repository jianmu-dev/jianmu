<template>
  <jm-workflow-expression-editor :selectable-params="selectableParams"/>
</template>

<script lang='ts'>
import { defineComponent, inject } from 'vue';
import { Graph, Node } from '@antv/x6';
import { CustomX6NodeProxy } from '../../../model/data/custom-x6-node-proxy';


export default defineComponent({
  props: {
    nodeId: {
      type: String,
      required: true,
    },
  },
  setup(props) {
    // 获取此时进行编辑的节点信息
    const getGraph = inject('getGraph') as () => Graph;
    const graph = getGraph();
    const graphNode = graph.getCellById(props.nodeId) as Node;
    const proxy = new CustomX6NodeProxy(graphNode);
    // 级联选择器选项
    const selectableParams = proxy.getSelectableParams(graph);
    return {
      selectableParams,
    };
  },
});
</script>
