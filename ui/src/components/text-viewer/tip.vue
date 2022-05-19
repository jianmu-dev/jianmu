<template>
  <jm-tooltip @update:modelValue="()=>{}" :model-value="v" :manual="true">
    <span style="display: inline">...</span>
  </jm-tooltip>
</template>
<script lang="ts">
import { computed, defineComponent } from 'vue';
import JmTooltip from '@/components/others/tooltip';

export default defineComponent({
  components: { JmTooltip },
  props: {
    visible: {
      type: Object,
      default: () => ({}),
    },
  },
  setup(props) {
    // 控制tooltip的显示与隐藏
    const v = computed<boolean>(() => props.visible.value);
    return {
      v,
    };
  },
});
</script>
