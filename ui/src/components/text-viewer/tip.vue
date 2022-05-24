<template>
  <jm-tooltip @update:modelValue="()=>{}" :model-value="v" :manual="true">
    <template #content>
      <div v-html="contentText" class="wrapper"></div>
    </template>
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
    contentText: {
      type: String,
      required: true,
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
<style lang="less">
.wrapper {
  max-width: 60vw;
  // 英文单词换行
  word-wrap: break-word;
  // 中文换行
  white-space: pre-wrap;
  // 单词内换行
  word-break: break-all;
}
</style>
