<template>
  <div class="jm-text-viewer">
    <span ref="transitCalculator" class="transit-calculator" v-html="temporaryContentComputed"/>
  </div>
</template>
<script lang="ts">
import { defineComponent, onMounted, ref, getCurrentInstance, onUpdated, computed } from 'vue';
import { ICallbackEvent, TextViewer } from './model';

export default defineComponent({
  emits: ['loaded'],
  name: 'jm-text-viewer',
  props: {
    // 文本内容
    value: {
      type: String,
      default: '',
    },
    // tooltip 显示方向
    tipPlacement: {
      type: String,
      default: 'bottom',
    },
  },
  setup(props, { emit }) {
    const transitCalculator = ref<HTMLElement>();
    // 临时字符串内容
    const temporaryContent = ref<string>('');
    const text = ref<string>(props.value ? props.value.toString() : '');
    const { appContext } = getCurrentInstance() as any;
    const temporaryContentComputed = computed<string>(() => {
      return temporaryContent.value.replace(/ /g, '&nbsp;');
    });
    onMounted(async () => {
      const textViewer = new TextViewer(
        text,
        props.tipPlacement,
        temporaryContent,
        transitCalculator,
        appContext,
        (event: ICallbackEvent) => {
          emit('loaded', event);
        },
      );
      await textViewer.reload();
    });
    onUpdated(async () => {
      if (!props.value || text.value === props.value.toString()) {
        return;
      }
      text.value = props.value.toString();
      const textViewer = new TextViewer(
        text,
        props.tipPlacement,
        temporaryContent,
        transitCalculator,
        appContext,
        (event: ICallbackEvent) => {
          emit('loaded', event);
        },
      );
      await textViewer.reload();
    });
    return {
      transitCalculator,
      temporaryContent,
      temporaryContentComputed,
    };
  },
});
</script>
<style lang="less">
.jm-text-viewer {
  .transit-calculator {
    opacity: 0;
    white-space: nowrap;
    display: inline;
  }

  .content {
    .text-line-last {
      // 解决最后一行会被换行显示
      word-break: normal;
      display: flex;
    }
  }
}
</style>
