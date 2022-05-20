<template>
  <jm-workflow-expression-editor :selectable-params="selectableParams" @change="handleChange"/>
</template>

<script lang='ts'>
import { defineComponent, inject, nextTick, onMounted, ref } from 'vue';
import { ElFormItemContext, elFormItemKey } from 'element-plus/es/el-form';
import { Graph, Node } from '@antv/x6';
import { CustomX6NodeProxy } from '../../../model/data/custom-x6-node-proxy';
import { ISelectableParam } from '../../../../workflow-expression-editor/model/data';

export default defineComponent({
  props: {
    nodeId: {
      type: String,
      required: true,
    },
  },
  emits: ['editor-created'],
  setup(props, { emit }) {
    const elFormItem = inject(elFormItemKey, {} as ElFormItemContext);
    // 获取此时进行编辑的节点信息
    const getGraph = inject('getGraph') as () => Graph;
    const graph = getGraph();
    const graphNode = graph.getCellById(props.nodeId) as Node;
    const proxy = new CustomX6NodeProxy(graphNode);
    // 级联选择器选项
    const selectableParams = ref<ISelectableParam[]>(proxy.getSelectableParams(graph));
    onMounted(() => {
      emit('editor-created', (params: ISelectableParam[]) => {
        selectableParams.value = params;
      });
    });
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
