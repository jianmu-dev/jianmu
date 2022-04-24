<template>
  <div class="jm-workflow-editor-shell-panel">
    <div>This is shell panel</div>
    <div>{{ modelValue.nodeRef }}</div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { IShellData } from '../../model/data';

export default defineComponent({
  props: {
    modelValue: {
      type: Object as PropType<IShellData>,
      required: true,
    },
  },
  emits: ['update:model-value'],
  setup(props, context) {

  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-shell-panel {

}
</style>