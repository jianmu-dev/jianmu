<template>
  <div class="jm-text-viewer">
     <span ref="transitCalculator" class="transit-calculator">
        {{ temporaryContent }}
     </span>
  </div>
</template>
<script lang="ts">
import { defineComponent, onMounted, ref, getCurrentInstance, onUpdated } from 'vue';
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
    };
  },
});
</script>
<style lang="less">
.jm-text-viewer {
  .transit-calculator {
    opacity: 0;
    white-space: nowrap;
    display: inline-block;
  }

  .content {
    .text-line {
      &:last-child {
        text-align: left;
      }

      text-align: justify;
      // 兼容IE & 火狐
      word-break: break-all;
      text-justify: distribute;
      // 解决文本内容两端对齐
      &::after {
        content: "";
        display: inline-block;
        width: 100%;
      }

      &.last {
        text-align: justify;
      }
    }
  }
}
</style>

