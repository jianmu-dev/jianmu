<template>
  <div class="jm-workflow-editor-toolbar">
    <div class="left">
      <button class="jm-icon-workflow-back"></button>
      <div class="title">未命名项目</div>
      <button class="jm-icon-workflow-edit"></button>
    </div>
    <div class="right">
      <div class="tools">
        <button class="jm-icon-workflow-zoom-out"></button>
        <div class="ratio">100%</div>
        <button class="jm-icon-workflow-zoom-in"></button>

        <button :class="['jm-icon-workflow-zoom-normal']"></button>
      </div>
      <div class="configs">
        <div>并发执行</div>
        <div>
          <jm-switch></jm-switch>
        </div>
      </div>
      <div class="operations">
        <jm-button>保存并返回</jm-button>
        <jm-button type="primary">保存</jm-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  emits: ['back', 'save'],
  setup(props, context) {

  },
});
</script>

<style scoped lang="less">
@import '../../vars';

.jm-workflow-editor-toolbar {
  height: @tool-bar-height;
  background: #FFFFFF;
  z-index: 1;
  font-size: 14px;
  color: #042749;
  padding: 0 30px;

  display: flex;
  justify-content: space-between;
  align-items: center;

  button[class^="jm-icon-workflow-"] {
    border-radius: 2px;
    border-width: 0;
    background-color: transparent;
    color: #526579;
    cursor: pointer;
    text-align: center;
    width: 24px;
    height: 24px;
    font-size: 18px;

    &::before {
      font-weight: bold;
    }

    &:hover {
      background-color: #EFF7FF;
      color: #096DD9;
    }
  }

  .left {
    display: flex;
    align-items: center;

    .title {
      margin-left: 20px;
      margin-right: 10px;
    }
  }

  .right {
    display: flex;
    justify-content: right;

    .tools {
      display: flex;
      align-items: center;

      .ratio {
        width: 40px;
        margin: 0 10px;
        text-align: center;
      }

      > :last-child {
        margin-left: 10px;
      }
    }

    .configs {
      display: flex;
      align-items: center;
      margin: 0 50px;

      > div + div {
        margin-left: 10px;
      }
    }

    .operations {
      display: flex;
      align-items: center;

      .el-button {
        padding: 8px 24px;
        min-height: 36px;

        & + .el-button {
          margin-left: 20px;
        }
      }
    }
  }
}
</style>