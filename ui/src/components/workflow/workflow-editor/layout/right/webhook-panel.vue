<template>
  <div class="jm-workflow-editor-webhook-panel">
    <div>This is webhook panel</div>
    <div>{{ nodeData.getName() }}</div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { Webhook } from '../../model/data/node/webhook';

export default defineComponent({
  props: {
    nodeData: {
      type: Object as PropType<Webhook>,
      required: true,
    },
  },
  setup(props, context) {

  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-webhook-panel {

}
</style>