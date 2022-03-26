<template>
  <span class="jm-text-copy">
    <jm-tooltip placement="top" :append-to-body="false">
      <template #content>
        <div class="tip-content" v-html="tipContent"/>
      </template>
      <span class="jm-icon-button-copy" @click="copy"></span>
    </jm-tooltip>
  </span>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, PropType, ref } from 'vue';
import useClipboard from 'vue-clipboard3';

export default defineComponent({
  name: 'jm-text-copy',
  props: {
    value: {
      type: String,
      default: '',
    },
    asyncValue: {
      type: Function as PropType<() => Promise<string>>,
      default: null,
    },
  },
  setup(props) {
    const { proxy } = getCurrentInstance() as any;
    const { toClipboard } = useClipboard();
    const tipContent = ref<string>('点击复制');
    const copying = ref<boolean>(false);

    const reset = (msg: string) => {
      tipContent.value = msg;

      setTimeout(() => {
        tipContent.value = '点击复制';
        copying.value = false;
      }, 2000);
    };

    return {
      tipContent,
      copying,
      copy: async () => {
        if (copying.value) {
          return;
        }
        copying.value = true;

        try {
          let value = props.value;
          if (props.asyncValue) {
            value = await props.asyncValue();
          }

          if (!value) {
            throw new Error('值为空');
          }

          await toClipboard(value, proxy.$el);
          reset('复制成功');
        } catch (err) {
          reset(`复制失败<br/>原因：${err.message}`);
          console.error(err);
        }
      },
    };
  },
});
</script>

<style scoped lang="less">
.jm-text-copy {
  display: inline;

  ::v-deep(.el-popper) {
    padding: 0;

    .tip-content {
      text-align: left;
      padding: 10px;
      white-space: nowrap;
    }
  }

  .jm-icon-button-copy {
    display: inline;
    color: #6B7B8D;
    cursor: pointer;
    opacity: 0.5;

    &:hover {
      opacity: 1;

      &:active {
        opacity: 0.8;
      }
    }

    &::before {
      margin: 0;
    }
  }
}
</style>