<template>
  <div class="jm-workflow-editor-async-task-panel">
    <div>This is async task panel</div>
    <div>{{ nodeData.getName() }}</div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { AsyncTask } from '../../model/data/node/async-task';

export default defineComponent({
  props: {
    nodeData: {
      type: Object as PropType<AsyncTask>,
      required: true,
    },
  },
  setup(props, context) {

  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-async-task-panel {

}
</style>