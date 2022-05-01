<template>
  <div class="jm-workflow-editor-webhook-panel">
    <div>This is webhook panel</div>
    <div>{{ modelValue.getName() }}</div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { Webhook } from '../../model/node/webhook';

export default defineComponent({
  props: {
    modelValue: {
      type: Object as PropType<Webhook>,
      required: true,
    },
  },
  emits: ['update:model-value'],
  setup(props, context) {

  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-webhook-panel {

}
</style>