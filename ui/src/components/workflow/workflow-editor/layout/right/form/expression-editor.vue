<template>
  <jm-workflow-expression-editor :selectable-params="selectableParams" :param-type="paramType" @change="handleChange" />
</template>

<script lang="ts">
import { defineComponent, inject, nextTick, onMounted, PropType, ref } from 'vue';
import { ElFormItemContext, elFormItemKey } from 'element-plus/es/el-form';
// eslint-disable-next-line no-redeclare
import { Graph, Node } from '@antv/x6';
import { CustomX6NodeProxy } from '../../../model/data/custom-x6-node-proxy';
import { ISelectableParam } from '../../../../workflow-expression-editor/model/data';
import { ExpressionTypeEnum, NodeTypeEnum, ParamTypeEnum } from '../../../model/data/enumeration';

export default defineComponent({
  props: {
    type: {
      type: String as PropType<ExpressionTypeEnum>,
      required: true,
    },
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
    const buildSelectableExtParam = inject('buildSelectableExtParam') as () => Promise<ISelectableParam | undefined>;
    const buildSelectableGlobalParam = inject('buildSelectableGlobalParam') as () => ISelectableParam | undefined;
    const buildSelectableOption = inject('buildSelectableOption') as (() => ISelectableParam | undefined) | undefined;
    const graph = getGraph();
    let extParam: ISelectableParam | undefined;
    const selectableParams = ref<ISelectableParam[]>([]);

    onMounted(async () => {
      // 外部参数
      extParam = await buildSelectableExtParam();
      if (extParam && extParam.children && extParam.children.length > 0) {
        selectableParams.value.push(extParam);
      }

      if (buildSelectableOption) {
        const option = buildSelectableOption();
        if (option) {
          selectableParams.value.push(option);
        }
      }

      let proxy: CustomX6NodeProxy | undefined;
      if (props.type === ExpressionTypeEnum.GLOBAL_PARAM) {
        proxy = graph
          .getNodes()
          .map(node => new CustomX6NodeProxy(node))
          .find(proxy => proxy.getData().getType() === NodeTypeEnum.WEBHOOK);
      } else {
        proxy = new CustomX6NodeProxy(graph.getCellById(props.nodeId) as Node);
      }

      if (!proxy) {
        return;
      }
      // 级联选择器选项
      selectableParams.value.push(...(await proxy.getSelectableParams(graph, props.type, buildSelectableGlobalParam)));
    });
    emit('editor-created', (params: ISelectableParam[]) => {
      selectableParams.value = [];
      // 外部参数
      if (extParam && extParam.children && extParam.children.length > 0) {
        selectableParams.value.push(extParam);
      }
      selectableParams.value.push(...params);
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
