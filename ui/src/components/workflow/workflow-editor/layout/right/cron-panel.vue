<template>
  <div class="jm-workflow-editor-cron-panel">
    <div>This is cron panel</div>
    <div>{{ modelValue.getName() }}</div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { Cron } from '../../model/data/node/cron';

export default defineComponent({
  props: {
    modelValue: {
      type: Object as PropType<Cron>,
      required: true,
    },
  },
  emits: ['update:model-value'],
  setup(props, context) {

  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-cron-panel {

}
</style>