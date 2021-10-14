<template>
  <div class="navigation-steps">
    步骤条：
    <jm-steps :active="active" finish-status="success">
      <jm-step title="步骤 1"></jm-step>
      <jm-step title="步骤 2"></jm-step>
      <jm-step title="步骤 3"></jm-step>
      <jm-step title="步骤 4"></jm-step>
    </jm-steps>

    <jm-button style="margin-top: 12px;" @click="next">下一步</jm-button>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';

export default defineComponent({
  setup() {
    const active = ref<number>(0);

    return {
      active,
      next() {
        if (active.value++ > 3) {
          active.value = 0;
        }
      },
    };
  },
});
</script>

<style scoped lang="less">
.navigation-steps {

}
</style>