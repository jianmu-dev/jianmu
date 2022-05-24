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
      default: 'bottom-end',
    },
    // 控制tooltip是否被放置到body元素上
    tipAppendToBody: {
      type: Boolean,
      default: true,
    },
  },
  setup(props, { emit }) {
    const transitCalculator = ref<HTMLElement>();
    // 临时字符串内容
    const temporaryContent = ref<string>('');
    const text = ref<string>(props.value ? props.value : '');
    const { appContext } = getCurrentInstance() as any;
    const temporaryContentComputed = computed<string>(() => {
      return temporaryContent.value.replace(/ /g, '&nbsp;');
    });
    onMounted(async () => {
      const textViewer = new TextViewer(
        text,
        props.tipPlacement,
        props.tipAppendToBody,
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
      if (!props.value || text.value === props.value) {
        return;
      }
      text.value = props.value;
      const textViewer = new TextViewer(
        text,
        props.tipPlacement,
        props.tipAppendToBody,
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
  height: inherit;

  .transit-calculator {
    opacity: 0;
    white-space: nowrap;
    display: inline;
  }

  .content {
    height: 100%;

    .text-line-last {
      // 解决最后一行会被换行显示
      word-break: normal;
      display: flex;
    }
  }
}
</style>
