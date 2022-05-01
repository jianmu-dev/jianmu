<template>
  <div class="jm-workflow-editor-async-task-panel">
    <div>This is async task panel</div>
    <div>{{ modelValue.ref }}</div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { IAsyncTaskData } from '../../model/data';

export default defineComponent({
  props: {
    modelValue: {
      type: Object as PropType<IAsyncTaskData>,
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