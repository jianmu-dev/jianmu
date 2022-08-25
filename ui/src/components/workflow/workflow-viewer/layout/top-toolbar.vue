<template>
  <div class="view-mode-container" :class="{readonly, dslMode}">
    <div class="tab" :class="{select: !dslMode}" @click="$emit('change-view-mode', ViewModeEnum.GRAPHIC)">图示</div>
    <div class="tab" :class="{select: dslMode}" @click="$emit('change-view-mode', ViewModeEnum.YAML)">yaml</div>
  </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue';
import { ViewModeEnum } from '@/api/dto/enumeration';
export default defineComponent({
  props: {
    dslMode: {
      type: Boolean,
      required: true,
    },
    readonly: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['change-view-mode'],
  setup() {
    return {
      ViewModeEnum,
    };
  },
});
</script>

<style lang="less">
.view-mode-container {
  position: absolute;
  top: 0;
  left: 30px;
  height: 60px;
  box-sizing: border-box;
  width: calc(100% - 60px);
  border-bottom: 1px solid #E6EBF2;
  background-color: #ffffff;
  // z-index: 3;
  // 预览只读时 底线充满
  &.readonly {
    left: 0;
    padding-left: 30px;
    width: 100%;
  }
  &.dslMode {
    border-bottom: 1px solid transparent;
  }
  .tab {
    position: relative;
    display: inline-block;
    text-align: center;
    width: 40px;
    line-height: 59px;
    font-size: 16px;
    margin-right: 55px;
    color: #082340;
    cursor: pointer;
    z-index: 100;
    &.select {
      color: #096DD9;
      border-bottom: 2px solid #096DD9;
    }
    &:hover {
      color: #096DD9;
    }
  }
}
</style>