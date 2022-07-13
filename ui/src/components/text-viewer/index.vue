<template>
  <div class="jm-text-viewer" ref="wrapperElement"/>
</template>
<script lang="ts">
import { defineComponent, onMounted, ref, getCurrentInstance, onUpdated, onBeforeUnmount } from 'vue';
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
    // 默认组件在外层元素大小在指定时间内重新计算（截流阈值）
    threshold: {
      type: Number,
      default: 800,
    },
  },
  setup(props, { emit }) {
    const wrapperElement = ref<HTMLElement>();
    const text = ref<string>(props.value ? props.value : '');
    const { appContext } = getCurrentInstance() as any;
    let textViewer: TextViewer;
    const init = async () => {
      if (textViewer) {
        textViewer.destroy();
      }
      textViewer = new TextViewer(
        text.value,
        props.tipPlacement,
        props.tipAppendToBody,
        wrapperElement.value!,
        appContext,
        (event: ICallbackEvent) => {
          emit('loaded', event);
        },
        props.threshold,
      );
    };
    onMounted(init);
    onUpdated(async () => {
      if (!props.value || text.value === props.value) {
        return;
      }
      text.value = props.value;
      await init();
    });
    onBeforeUnmount(() => {
      if (textViewer) {
        textViewer.destroy();
      }
    });
    return {
      wrapperElement,
    };
  },
});
</script>
<style lang="less">
.jm-text-viewer {
  height: inherit;

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
