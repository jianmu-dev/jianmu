<template>
  <span class="jm-text-copy">
    <jm-tooltip v-if="tip" placement="top" :append-to-body="false">
      <template #content>
        <div class="tip-content" v-html="tipContent"/>
      </template>
      <span :class="{'jm-icon-button-copy': true, 'doing': copying}" @click="copy"></span>
    </jm-tooltip>
  </span>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, nextTick, PropType, ref } from 'vue';
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
    const tip = ref<boolean>(true);
    const tipContent = ref<string>('复制');
    const copying = ref<boolean>(false);

    const reloadTip = async (msg: string) => {
      tip.value = false;
      await nextTick();
      tipContent.value = msg;
      tip.value = true;
    };

    const reset = async (msg: string) => {
      await reloadTip(msg);

      setTimeout(async () => {
        copying.value = false;
        await reloadTip('复制');
      }, 2000);
    };

    return {
      tip,
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
          await reset('已复制');
        } catch (err) {
          await reset(`复制失败<br/>原因：${err.message}`);
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
    user-select: none;

    &:hover {
      opacity: 1;

      &:active {
        opacity: 0.8;
      }
    }

    &::before {
      margin: 0;
    }

    &.doing {
      opacity: 0.35;
      cursor: not-allowed;

      &:hover {
        opacity: 0.35;
      }
    }
  }
}
</style>