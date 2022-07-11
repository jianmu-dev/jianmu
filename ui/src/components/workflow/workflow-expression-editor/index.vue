<template>
  <div class="jm-workflow-exp-editor">
    <param-button :selectableParams="selectableParams" @inserted="handleInserted"/>
    <textarea ref="textareaRef"></textarea>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, PropType, ref } from 'vue';
import { ExpressionEditor } from './model/expression-editor';
import { ISelectableParam } from './model/data';
import ParamButton from './param-button.vue';

export default defineComponent({
  name: 'jm-workflow-expression-editor',
  components: { ParamButton },
  props: {
    modelValue: {
      type: String,
      default: '',
    },
    placeholder: {
      type: String,
      default: '',
    },
    selectableParams: {
      type: Array as PropType<ISelectableParam[]>,
      required: true,
    },
  },
  emits: ['update:model-value', 'focus', 'blur', 'change'],
  setup(props, { emit }) {
    const textareaRef = ref<HTMLTextAreaElement>();
    let expressionEditor: ExpressionEditor;

    onMounted(() => {
      const textareaEl = textareaRef.value!;
      textareaEl.value = props.modelValue;

      expressionEditor = new ExpressionEditor(textareaEl, props.placeholder,
        e => emit('focus', e),
        e => {
          const val = textareaEl.value;
          emit('update:model-value', val);
          emit('blur', e);
          emit('change', val);
        });
    });

    return {
      textareaRef,
      handleInserted: (arr: string[]) => expressionEditor.insertParam(arr),
    };
  },
});
</script>

<style scoped lang="less">
@import "../workflow-editor/vars";

.jm-workflow-exp-editor {
  position: relative;

  .param-button {
    position: absolute;
    top: -25px;
    right: 0;
  }

  ::v-deep(.CodeMirror) {
    height: inherit;

    * {
      // 字体设为等宽
      font-family: Menlo, Monaco, 'Courier New', monospace;
    }

    .CodeMirror-lines {
      padding: 0;
    }

    pre.CodeMirror-line, pre.CodeMirror-line-like {
      padding: 0;
    }

    &.CodeMirror-focused {
      box-sizing: border-box;
      border-color: @primary-color;
    }

    .CodeMirror-placeholder {
      color: #c0c4cc;
    }

    line-height: 26px;
    min-height: 36px;
    background-color: #FFFFFF;

    &:hover {
      border-color: @primary-color;
    }

    transition: all .1s ease-in-out;

    color: #333333;
    border-radius: 2px;
    border: 1px solid #D0E0ED;
    padding: 4px 15px;
    box-sizing: border-box;
  }
}
</style>