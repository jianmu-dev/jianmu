<template>
  <div class="param-toolbar" @click="changeParam" @mouseleave="hide">
    <div class="text" @click="changeParam"></div>
    <div class="jm-icon-button-cancel" @click="remove"></div>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject } from 'vue';
import { ExpressionEditor } from './model';

export default defineComponent({
  setup(props, context) {
    const getExpressionEditor = inject('getExpressionEditor') as () => ExpressionEditor;

    return {
      changeParam: () => {
        // TODO 待完善
      },
      hide: () => {
        getExpressionEditor().toolbar.hide();
      },
      remove: () => {
        getExpressionEditor().toolbar.removeParam();
      },
    };
  },
});
</script>

<style scoped lang="less">
.param-toolbar {
  position: fixed;
  left: -1000px;
  top: -1000px;
  background-color: #EFF7FF;
  box-sizing: border-box;
  display: flex;
  align-items: center;

  .text {
    cursor: pointer;

    user-select: none;
    -moz-user-select: none;
    -webkit-user-select: none;
    -ms-user-select: none;
  }

  .jm-icon-button-cancel {
    position: absolute;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    width: 0.75em;
    height: 0.75em;
    overflow: hidden;
    border-radius: 50%;
    background: #6B7B8D;
    color: #FFFFFF;

    &::before {
      margin: 0;
    }
  }
}
</style>