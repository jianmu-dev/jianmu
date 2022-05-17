<template>
  <jm-workflow-expression-editor :selectable-params="selectableParams" @change="handleChange"/>
</template>

<script lang='ts'>
import { defineComponent, inject, nextTick } from 'vue';
import { ElFormItemContext, elFormItemKey } from 'element-plus/es/el-form';
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
    const elFormItem = inject(elFormItemKey, {} as ElFormItemContext);
    // 获取此时进行编辑的节点信息
    const getGraph = inject('getGraph') as () => Graph;
    const graph = getGraph();
    const graphNode = graph.getCellById(props.nodeId) as Node;
    const proxy = new CustomX6NodeProxy(graphNode);
    // 级联选择器选项
    const selectableParams = proxy.getSelectableParams(graph);
    return {
      selectableParams,
      handleChange: async (val: string) => {
        // 必须nextTick，否则外部的change晚于当前，数据更新有延迟
        await nextTick();
        elFormItem.formItemMitt?.emit('el.form.blur', val);
        elFormItem.formItemMitt?.emit('el.form.change', val);
      },
    };
  },
});
</script>
