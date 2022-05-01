<template>
  <div class="jm-workflow-editor-webhook-panel">
    <div>This is webhook panel</div>
    <div>{{ modelValue.ref }}</div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { IWebhookData } from '../../model/data';

export default defineComponent({
  props: {
    modelValue: {
      type: Object as PropType<IWebhookData>,
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