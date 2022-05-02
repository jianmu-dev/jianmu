<template>
  <div class="jm-workflow-editor-async-task-panel">
    <div>This is async task panel</div>
    <div>{{ form.getName() }}</div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType, ref } from 'vue';
import { AsyncTask } from '../../model/data/node/async-task';

export default defineComponent({
  props: {
    nodeData: {
      type: Object as PropType<AsyncTask>,
      required: true,
    },
  },
  setup(props) {
    const form = ref<AsyncTask>(props.nodeData);

    return {
      form,
    };
  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-async-task-panel {

}
</style>