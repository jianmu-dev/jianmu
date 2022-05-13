<template>
  <div class="param-toolbar" @mouseleave="hide">
    <div class="text" @mouseenter="changeParam"></div>
    <jm-cascader
      v-if="selectedVal"
      v-model="selectedVal"
      :options="selectableParams"
      :offset="0"
      :append-to-body="false"
      :class="{ opened }"
      @focus="opened = true"
      @change="handleChange"></jm-cascader>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject, PropType, ref } from 'vue';
import { ExpressionEditor, INNER_PARAM_TAG, ISelectableParam } from './model';

export default defineComponent({
  props: {
    selectableParams: {
      type: Array as PropType<ISelectableParam[]>,
      required: true,
    },
  },
  setup() {
    const opened = ref<boolean>(false);
    const selectedVal = ref<string[]>();
    const getExpressionEditor = inject('getExpressionEditor') as () => ExpressionEditor;

    const hide = () => {
      selectedVal.value = undefined;
      opened.value = false;
      getExpressionEditor().toolbar.hide();
    };

    return {
      opened,
      selectedVal,
      changeParam: () => {
        const { nodeId, ref, inner } = getExpressionEditor().toolbar.getCurrentParam()!;
        selectedVal.value = [];
        selectedVal.value.push(nodeId);
        if (inner) {
          selectedVal.value.push(INNER_PARAM_TAG);
        }
        selectedVal.value.push(ref);
      },
      handleChange: (arr: string[]) => {
        const { toolbar } = getExpressionEditor();
        const param = toolbar.getParam(arr);

        toolbar.updateParam(param);

        // 更新参数后，需隐藏工具栏，否则有视觉延迟
        hide();
      },
      hide,
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
    white-space: nowrap;
    cursor: pointer;
  }

  ::v-deep(.el-cascader) {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    background: #3EBB03;
    opacity: 0;
    line-height: 0;

    &.opened {
      height: (100% + 40px);
    }

    .el-input {
      line-height: 0;

      .el-input__inner {
        height: 100%;
        line-height: 0;
      }

      .el-input__icon {
        line-height: 0;
      }
    }
  }
}
</style>