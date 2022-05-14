<template>
  <div class="jm-workflow-expression-editor">
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

export default defineComponent({
  name: 'jm-workflow-expression-editor',
  components: { ParamToolbar },
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
  emits: ['update:model-value', 'editor-created'],
  setup(props, { emit }) {
    const paramToolbar = ref();
    const editorRef = ref<HTMLDivElement>();
    let expressionEditor: ExpressionEditor;
    provide('getExpressionEditor', (): ExpressionEditor => expressionEditor);

    onMounted(() => {
      expressionEditor = new ExpressionEditor(paramToolbar.value.$el, editorRef.value!, props.modelValue, props.selectableParams);

      emit('editor-created', expressionEditor);
    });

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
      },
      refreshLastRange: () => {
        expressionEditor.refreshLastRange();
      },
    };
  },
});
</script>

<style lang="less">
.jm-workflow-expression-editor {
  .container input, .param-toolbar {
    padding: 2px 0.5em;
    // 必须继承，否则，在Chrome粘贴时附带样式
    color: inherit;
    font-size: inherit;
  }

  .container {
    // 英文单词换行
    word-wrap: break-word;
    // 中文换行
    white-space: pre-wrap;
    outline: none;

    &:empty::before {
      color: #7B8C9C;
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