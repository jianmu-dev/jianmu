<template>
  <div class="jm-workflow-expression-editor">
    <param-button :selectableParams="selectableParams" @inserted="handleInserted"/>
    <param-toolbar ref="paramToolbar" :selectable-params="selectableParams" @change="handleBlur"/>
    <div class="container" ref="editorRef" contenteditable="true" :placeholder="placeholder"
         @cut="handleCut" @copy="handleCopy" @paste="handlePaste" @focus="handleFocus" @blur="handleBlur"
         @keyup="refreshLastRange" @mouseup="refreshLastRange"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, onUnmounted, onUpdated, PropType, provide, ref } from 'vue';
import { ISelectableParam } from './model/data';
import { ExpressionEditor } from './model/expression-editor';
import ParamToolbar from './param-toolbar.vue';
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
  emits: ['update:model-value', 'focus', 'blur', 'change'],
  setup(props, { emit }) {
    const paramToolbar = ref();
    const editorRef = ref<HTMLDivElement>();
    let expressionEditor: ExpressionEditor;
    provide('getExpressionEditor', (): ExpressionEditor => expressionEditor);

    onUpdated(() => expressionEditor.toolbar.refreshSelectableParams(props.selectableParams));

    onMounted(() => (expressionEditor = new ExpressionEditor(paramToolbar.value.$el,
      editorRef.value!, props.modelValue, props.selectableParams)));

    onUnmounted(() => expressionEditor.destroy());

    return {
      paramToolbar,
      editorRef,
      handleCut: (e: ClipboardEvent) => expressionEditor.cut(e),
      handleCopy: (e: ClipboardEvent) => expressionEditor.copy(e),
      handlePaste: (e: ClipboardEvent) => expressionEditor.paste(e),
      handleFocus: (e: Event) => {
        emit('focus', e);
      },
      handleBlur: (e: Event) => {
        const el = editorRef.value!.cloneNode(true) as HTMLDivElement;
        if (expressionEditor.checkManualInput(el.innerText)) {
          // 表示手动输入了参数引用
          const plainText = expressionEditor.getPlainText(el);
          expressionEditor.refresh(plainText);

          // 必须刷新，否则有误
          expressionEditor.refreshLastRange();
        }

        const plainText = expressionEditor.getPlainText(el);

        emit('update:model-value', plainText);
        emit('blur', e);
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

  .container .param-ref, .param-toolbar {
    border-left-width: 0.25em;
    border-right-width: 0.25em;
    border-top-width: 2px;
    border-bottom-width: 2px;
    border-style: solid;
    border-color: #FFFFFF;
    padding: 2px 0.5em;
    // 必须继承，否则，在Chrome粘贴时附带样式
    color: inherit;
    font-size: inherit;
    box-sizing: border-box;
    // 英文单词换行
    word-wrap: break-word;
    // 中文换行
    white-space: pre-wrap;
    // 单词内换行
    word-break: break-all;
  }

  .param-button {
    position: absolute;
    top: -25px;
    right: 0;
  }

  .container {
    line-height: 2em;
    min-height: 36px;
    background-color: #FFFFFF;

    // fix: Safari中无法输入
    user-select: auto;
    -moz-user-select: auto;
    -webkit-user-select: auto;
    -ms-user-select: auto;

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
    padding: 4px 15px;
    box-sizing: border-box;
    // 英文单词换行
    word-wrap: break-word;
    // 中文换行
    white-space: pre-wrap;
    // 单词内换行
    word-break: break-all;
    outline: none;

    &:empty::before {
      color: #c0c4cc;
      content: attr(placeholder);
    }

    .param-ref {
      background-color: #E8E8E8;
    }

    textarea.param-ref {
      resize: none;
    }
  }
}
</style>
