<template>
  <div class="jm-workflow-editor-cron-panel">
    <div>This is cron panel</div>
    <div>{{ form.getName() }}</div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType, ref } from 'vue';
import { Cron } from '../../model/data/node/cron';

export default defineComponent({
  props: {
    nodeData: {
      type: Object as PropType<Cron>,
      required: true,
    },
  },
  setup(props) {
    const form = ref<Cron>(props.nodeData);

    return {
      form,
    };
  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-cron-panel {

}
</style>