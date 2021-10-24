<template>
  <div class="jm-editable-text">
    <jm-input v-if="writable"
              v-model="text"
              v-focus
              placeholder="请输入内容"
              @blur="changeWritable(false)"
              @keyup.enter="changeWritable(false)"
              @click.stop/>
    <div v-else class="text-wrapper" @click.stop="changeWritable(true)">
      <span :class="{'default': !text}">{{ text || defaultText || '可点击设置' }}</span>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, SetupContext, watch } from 'vue';

export default defineComponent({
  name: 'jm-editable-text',
  props: {
    modelValue: String,
    defaultText: String,
  },
  emits: ['update:model-value', 'change'],
  directives: {
    // 注册一个局部的自定义指令 v-focus
    focus: {
      mounted(el) {
        // 聚焦元素
        el.querySelector('input').focus();
      },
    },
  },
  setup(props: any, { emit }: SetupContext) {
    const text = ref<string>(props.modelValue);
    const writable = ref<boolean>(false);

    watch(() => props.modelValue, (newVal: string) => (text.value = newVal));

    return {
      text,
      writable,
      changeWritable: (val: boolean) => {
        if (!val) {
          if (props.modelValue !== text.value) {
            emit('update:model-value', text.value);
            emit('change', text.value);
          }
        }
        writable.value = val;
      },
    };
  },
});
</script>

<style scoped lang="less">
.jm-editable-text {
  width: 100%;
  height: inherit;
  line-height: inherit;
  display: inline-block;

  ::v-deep(.el-input) {
    line-height: inherit;
    box-sizing: border-box;
    background-color: #F5F8FB;

    .el-input__inner {
      padding: 0;
      height: inherit;
      line-height: inherit;
      box-sizing: border-box;
      border-width: 0;
      background-color: transparent;
    }
  }

  .text-wrapper {
    max-width: 100%;
    height: inherit;
    line-height: inherit;
    display: inline-block;
    cursor: pointer;
    box-sizing: border-box;

    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;

    padding-right: 1.5em;
    background-image: url('./svgs/edit.svg');
    background-repeat: no-repeat;
    background-position: right center;
    background-size: 1.2em 1.2em;

    &:hover {
      background-color: #EFF7FF;
    }

    .default {
      color: #ccc;
    }
  }
}
</style>