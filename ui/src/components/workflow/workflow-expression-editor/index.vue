<template>
  <div class="jm-workflow-expression-editor">
    <param-button :selectableParams="selectableParams" @inserted="handleInserted"/>
    <param-toolbar ref="paramToolbar" :selectable-params="selectableParams"/>
    <div class="container" ref="editorRef" contenteditable="true" :placeholder="placeholder"
         @cut="handleCut" @copy="handleCopy" @paste="handlePaste" @blur="handleBlur"
         @keyup="refreshLastRange" @mouseup="refreshLastRange"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, onUnmounted, PropType, provide, ref } from 'vue';
import { ISelectableParam } from './model/data';
import { ExpressionEditor } from './model/expression-editor';
import ParamToolbar from './param-toolbar.vue';
import { extractReferences } from './model/util';
import ParamButton from './param-button.vue';

export default defineComponent({
  name: 'jm-workflow-expression-editor',
  components: { ParamButton, ParamToolbar },
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
  emits: ['update:model-value', 'change'],
  setup(props, { emit }) {
    const paramToolbar = ref();
    const editorRef = ref<HTMLDivElement>();
    let expressionEditor: ExpressionEditor;
    provide('getExpressionEditor', (): ExpressionEditor => expressionEditor);

    onMounted(() => (expressionEditor = new ExpressionEditor(paramToolbar.value.$el,
      editorRef.value!, props.modelValue, props.selectableParams)));

    onUnmounted(() => expressionEditor.destroy());

    return {
      paramToolbar,
      editorRef,
      handleCut: (e: ClipboardEvent) => expressionEditor.cut(e),
      handleCopy: (e: ClipboardEvent) => expressionEditor.copy(e),
      handlePaste: (e: ClipboardEvent) => expressionEditor.paste(e),
      handleBlur: () => {
        const el = editorRef.value!.cloneNode(true) as HTMLDivElement;
        const references = extractReferences(el.innerText);
        const plainText = expressionEditor.getPlainText(el);

        if (references.length > 0) {
          // 表示手动输入了参数引用
          expressionEditor.refresh(plainText);

          // 必须刷新，否则有误
          expressionEditor.refreshLastRange();
        }

        emit('update:model-value', plainText);
        emit('change', plainText);
      },
      refreshLastRange: () => expressionEditor.refreshLastRange(),
      handleInserted: (arr: string[]) => expressionEditor.insertParam(arr),
    };
  },
});
</script>

<style lang="less">
@import "../workflow-editor/vars";

.jm-workflow-expression-editor {
  position: relative;

  .container input, .param-toolbar {
    padding: 2px 0.5em;
    // 必须继承，否则，在Chrome粘贴时附带样式
    color: inherit;
    font-size: inherit;
  }

  .param-button {
    position: absolute;
    top: -25px;
    right: 0;
  }

  .container {
    line-height: 34px;

    &:hover {
      border-color: @primary-color;
    }

    transition: all .1s ease-in-out;

    &:focus {
      border: 1px solid @primary-color;
    }

    color: #333333;
    border-radius: 2px;
    border: 1px solid #D0E0ED;
    padding: 0 15px;
    box-sizing: border-box;
    // 英文单词换行
    word-wrap: break-word;
    // 中文换行
    white-space: pre-wrap;
    outline: none;

    &:empty::before {
      color: #c0c4cc;
      content: attr(placeholder);
    }

    input {
      margin: 2px 0.25em;
      background-color: #E8E8E8;
      border-width: 0;
    }
  }
}
</style>
