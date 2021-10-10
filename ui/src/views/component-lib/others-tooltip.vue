<template>
  <div class="data-tooltip">
    文字提示：
    <jm-tooltip content="Top center" placement="top">
      <jm-button>Dark</jm-button>
    </jm-tooltip>
    <jm-tooltip content="Right" placement="right" effect="light">
      <jm-button>Light</jm-button>
    </jm-tooltip>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({});
</script>

<style scoped lang="less">
.data-tooltip {

}
</style>