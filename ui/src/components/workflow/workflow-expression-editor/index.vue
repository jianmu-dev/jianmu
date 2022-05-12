<template>
  <div class="jm-workflow-expression-editor" ref="editorRef" contenteditable="true"
       @cut="handleCut" @copy="handleCopy" @paste="handlePaste" @blur="handleBlur"/>
</template>

<script lang="ts">
import { defineComponent, onMounted, onUnmounted, PropType, ref } from 'vue';
import { ExpressionEditor, GetParamFn } from './model';

export default defineComponent({
  name: 'jm-workflow-expression-editor',
  props: {
    modelValue: {
      type: String,
      default: '',
    },
    getParam: {
      type: Function as PropType<GetParamFn>,
      required: true,
    },
  },
  emits: ['update:model-value', 'editor-created'],
  setup(props, { emit }) {
    const editorRef = ref<HTMLDivElement>();
    let expressionEditor: ExpressionEditor;

    onMounted(() => {
      expressionEditor = new ExpressionEditor(editorRef.value!, props.modelValue, props.getParam);

      emit('editor-created', expressionEditor);
    });

    onUnmounted(() => {
      expressionEditor.destroy();
    });

    return {
      editorRef,
      handleCut: (e: ClipboardEvent) => expressionEditor.cut(e),
      handleCopy: (e: ClipboardEvent) => expressionEditor.copy(e),
      handlePaste: (e: ClipboardEvent) => expressionEditor.paste(e),
      handleBlur: () => {
        const el = editorRef.value!.cloneNode(true) as HTMLDivElement;
        emit('update:model-value', expressionEditor.getRaw(el));
      },
    };
  },
});
</script>

<style lang="less">
.jm-workflow-expression-editor {
  width: 100%;
  height: 100%;
  outline: none;

  input {
    padding: 2px 5px;
    margin: 2px;

    &:first-child, &:-moz-first-node {
      margin-left: 0;
    }

    &:-moz-last-node, &:last-child {
      margin-right: 0;
    }

    // 必须继承，否则，在Chrome粘贴时附带样式
    color: inherit;
    background-color: #E8E8E8;
    border-width: 0;
    font-size: inherit;
  }

  .input-hover {
    background-color: #EFF7FF;
  }
}
</style>