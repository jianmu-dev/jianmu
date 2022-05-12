<template>
  <div class="jm-workflow-expression-editor">
    <param-toolbar ref="paramToolbar"/>
    <div class="container" ref="editorRef" contenteditable="true"
         @cut="handleCut" @copy="handleCopy" @paste="handlePaste" @blur="handleBlur"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, onUnmounted, PropType, provide, ref } from 'vue';
import { ExpressionEditor, GetParamFn } from './model';
import ParamToolbar from './param-toolbar.vue';

export default defineComponent({
  name: 'jm-workflow-expression-editor',
  components: { ParamToolbar },
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
    const paramToolbar = ref();
    const editorRef = ref<HTMLDivElement>();
    let expressionEditor: ExpressionEditor;
    provide('getExpressionEditor', (): ExpressionEditor => expressionEditor);

    onMounted(() => {
      expressionEditor = new ExpressionEditor(paramToolbar.value.$el, editorRef.value!, props.modelValue, props.getParam);

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
        emit('update:model-value', expressionEditor.getRaw(el));
      },
    };
  },
});
</script>

<style lang="less">
.jm-workflow-expression-editor {
  width: inherit;
  height: inherit;

  .container input, .param-toolbar {
    padding: 2px 0.5em;
    // 必须继承，否则，在Chrome粘贴时附带样式
    color: inherit;
    font-size: inherit;
  }

  .container {
    width: inherit;
    height: inherit;
    outline: none;

    input {
      margin: 2px 0.25em;

      &:first-child, &:-moz-first-node {
        margin-left: 0;
      }

      &:-moz-last-node, &:last-child {
        margin-right: 0;
      }

      background-color: #E8E8E8;
      border-width: 0;
    }
  }
}
</style>