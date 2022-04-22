<template>
  <div class="jm-workflow-editor" ref="container">
    <div class="stencil" ref="stencilContainer"></div>
    <div class="graph-container" ref="graphContainer"></div>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, PropType, ref } from 'vue';
import { WorkflowLayout } from './layout/model/workflow-layout';
import { IWorkflowData } from './layout/model/data';
import { KeyValue } from '@antv/x6/es/types';
import { HistoryManager } from '@antv/x6/es/graph/history';
import Command = HistoryManager.Command;

// 注册自定义x6元素
Object.values(import.meta.globEager('./shapes/**')).forEach(({ default: register }) => {
  if (typeof register !== 'function') {
    return;
  }
  register();
});

export default defineComponent({
  name: 'jm-workflow-editor',
  props: {
    modelValue: Object as PropType<IWorkflowData>,
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const data = ref<IWorkflowData>(props.modelValue);
    const container = ref<HTMLElement>();
    const stencilContainer = ref<HTMLElement>();
    const graphContainer = ref<HTMLElement>();

    onMounted(() => {
      const layout = new WorkflowLayout(graphContainer.value!, stencilContainer.value!, data.value);

      layout.x6Graph.history.on('change', (args: {
        cmds: Command[] | null
        options: KeyValue
      }) => {
        // code here
        console.log(11111, data.value, layout.x6Graph.toJSON());
        emit('update:modelValue', layout.x6Graph.toJSON());
        console.log(222222, data.value);
      });
    });

    return {
      container,
      stencilContainer,
      graphContainer,
    };
  },
});
</script>

<style lang="less" src="./index.less"/>