<template>
  <jm-workflow-expression-editor :selectable-params="selectableParams" :param-type="paramType" @change="handleChange"/>
</template>

<script lang='ts'>
import { defineComponent, inject, nextTick, onMounted, PropType, ref } from 'vue';
import { ElFormItemContext, elFormItemKey } from 'element-plus/es/el-form';
import { Graph, Node } from '@antv/x6';
import { CustomX6NodeProxy } from '../../../model/data/custom-x6-node-proxy';
import { ISelectableParam } from '../../../../workflow-expression-editor/model/data';
import { NodeTypeEnum, ParamTypeEnum } from '../../../model/data/enumeration';

export default defineComponent({
  props: {
    nodeId: {
      type: String,
      default: '',
    },
    paramType: {
      type: String as PropType<ParamTypeEnum>,
      required: true,
    },
  },
  emits: ['editor-created'],
  setup(props, { emit }) {
    const elFormItem = inject(elFormItemKey, {} as ElFormItemContext);
    // 获取此时进行编辑的节点信息
    const getGraph = inject('getGraph') as () => Graph;
    const buildSelectableGlobalParam = inject('buildSelectableGlobalParam') as () => ISelectableParam | undefined;
    const graph = getGraph();
    const selectableParams = ref<ISelectableParam[]>([]);
    if (props.nodeId) {
      const graphNode = graph.getCellById(props.nodeId) as Node;
      const proxy = new CustomX6NodeProxy(graphNode);
      // 级联选择器选项
      selectableParams.value.push(...proxy.getSelectableParams(graph));
      if (proxy.getData().getType() !== NodeTypeEnum.WEBHOOK) {
        const buildGlobalParam = buildSelectableGlobalParam();
        if (buildGlobalParam) {
          selectableParams.value.push(buildGlobalParam);
        }
      }
    } else {
      const webhookNode = graph.getNodes().find(node =>
        new CustomX6NodeProxy(node).getData().getType() === NodeTypeEnum.WEBHOOK);
      if (webhookNode) {
        selectableParams.value.push(...new CustomX6NodeProxy(webhookNode).getSelectableParams(graph));
      }
    }
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
