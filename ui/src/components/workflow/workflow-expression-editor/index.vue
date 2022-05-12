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
  margin: 50px;
  border: 1px solid red;
  width: 800px;
  height: 500px;
  outline: none;

  line-height: 20px;
  font-size: 14px;

  input {
    // 必须继承，否则，在Chrome粘贴时附带样式
    color: inherit;
    background-color: #ccc;
    border-width: 0;

    margin: 0 2px;
    height: 20px;
    line-height: 20px;
    font-size: 14px;
    width: 110px;
  }

  .input-hover {
    background-color: #9EB1C5;
  }
}
</style>