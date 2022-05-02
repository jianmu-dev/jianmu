<template>
  <div class="jm-workflow-editor-async-task-panel">
    <div>This is async task panel</div>
    <div>{{ modelValue.getName() }}</div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { AsyncTask } from '../../model/data/node/async-task';

export default defineComponent({
  props: {
    modelValue: {
      type: Object as PropType<AsyncTask>,
      required: true,
    },
  },
  emits: ['update:model-value'],
  setup(props, context) {

  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-async-task-panel {

}
</style>