<template>
  <jm-drawer
    title="节点配置面板"
    :size="400"
    direction="rtl"
    destroy-on-close
  >
    <div class="jm-workflow-editor-node-config-panel">This is node config panel</div>
  </jm-drawer>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  setup(props, context) {

  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-node-config-panel {

}
</style>