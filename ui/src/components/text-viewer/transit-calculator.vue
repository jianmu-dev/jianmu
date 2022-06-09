<template>
  <span class="transit-calculator" v-html="tempValue"/>
</template>

<script lang='ts'>
import { computed, defineComponent } from 'vue';

export default defineComponent({
  props: {
    temporaryContent: {
      type: Object,
      default: () => ({}),
    },
  },
  setup(props) {
    const tempValue = computed<string>(() => props.temporaryContent.value.replace(/ /g, '&nbsp;'));
    return {
      tempValue,
    };
  },
});
</script>

<style scoped lang='less'>
.transit-calculator {
  opacity: 0;
  white-space: nowrap;
  display: inline;
}
</style>
