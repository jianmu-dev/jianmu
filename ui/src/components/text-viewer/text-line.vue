<template>
  <div v-html="valueCom" class="text-line"/>
</template>

<script lang='ts'>
import { computed, defineComponent } from 'vue';

export default defineComponent({
  props: {
    value: {
      type: String,
      required: true,
    },
  },
  setup(props) {
    const valueCom = computed<string>(() => props.value.replace(/ /g, '&nbsp;'));
    return {
      valueCom,
    };
  },
});
</script>

<style scoped lang='less'>
.text-line-last {
  .text-line {
    text-align: left;

    &::after {
      width: 0;
    }
  }
}

.text-line {
  &:last-child {
    text-align: left;

    &::after {
      width: 0;
    }
  }

  text-align: justify;
  // 兼容IE & 火狐
  text-justify: distribute;
  // 解决文本内容两端对齐
  &::after {
    content: "";
    display: inline-block;
    width: 100%;
  }
}
</style>
