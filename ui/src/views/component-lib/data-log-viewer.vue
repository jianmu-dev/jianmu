<template>
  <div class="data-log-viewer">
    日志查看器：
    <jm-log-viewer :value="log"/>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  setup() {
    let log = '';

    for (let i = 1; i <= 10; i++) {
      log += `第${i}行日志\n`;
    }

    return {
      log,
    };
  },
});
</script>

<style scoped lang="less">
.data-log-viewer {

}
</style>