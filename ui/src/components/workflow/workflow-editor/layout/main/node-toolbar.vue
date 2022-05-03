<template>
  <div class="jm-workflow-editor-node-toolbar">

  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  setup(props, context) {
  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-node-toolbar {
  position: absolute;
  left: -1000px;
  top: -1000px;
  z-index: 1;

  width: 100px;
  height: 30px;
  background: #6b7b8d;
  opacity: 0.5;
}
</style>