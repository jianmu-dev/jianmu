<template>
  <jm-tooltip @update:modelValue="()=>{}" :model-value="visible" :manual="true">
    <span style="display: inline">...</span>
  </jm-tooltip>
</template>
<script lang="ts">
import {
  defineComponent,
  ref,
} from 'vue';

export default defineComponent({
  setup() {
    // 控制tooltip的显示与隐藏
    const visible = ref<boolean>(false);
    const toggle = (flag: boolean) => {
      visible.value = flag;
    };
    return {
      visible,
      toggle,
    };
  },
});
</script>
