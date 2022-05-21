<template>
  <div class="param-toolbar">
    <div class="text" @mouseenter="changeParam"/>
    <jm-cascader v-if="selectedVal" v-model="selectedVal"
                 :options="selectableParams" :offset="0"
                 :class="{ opened }" @focus="opened = true" @change="handleChange"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject, nextTick, PropType, ref } from 'vue';
import { ISelectableParam } from './model/data';
import { INNER_PARAM_TAG } from './model/const';
import { ExpressionEditor } from './model/expression-editor';
import { fromArray } from './model/util';

export default defineComponent({
  props: {
    selectableParams: {
      type: Array as PropType<ISelectableParam[]>,
      required: true,
    },
  },
  emits: ['change'],
  setup(_, { emit }) {
    const opened = ref<boolean>(false);
    const selectedVal = ref<string[]>();
    const getExpressionEditor = inject('getExpressionEditor') as () => ExpressionEditor;

    return {
      opened,
      selectedVal,
      changeParam: () => {
        const { nodeId, ref, inner } = getExpressionEditor().toolbar.getCurrentParam(() => {
          selectedVal.value = undefined;
          opened.value = false;
        })!;
        selectedVal.value = [];
        selectedVal.value.push(nodeId);
        if (inner) {
          selectedVal.value.push(INNER_PARAM_TAG);
        }
        selectedVal.value.push(ref);
      },
      handleChange: async (arr: string[]) => {
        const { toolbar } = getExpressionEditor();
        const param = toolbar.getParam(fromArray(arr));

        toolbar.updateParam(param);

        await nextTick();

        // 更新参数后，需隐藏工具栏，否则有视觉延迟
        getExpressionEditor().toolbar.hide();

        emit('change');
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
    line-height: normal;
    cursor: pointer;

    &.single-line {
      // 适配某些场景下，强制不换行
      white-space: nowrap;
    }
  }

  ::v-deep(.el-cascader) {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    opacity: 0;
    line-height: 0;

    &.opened {
      height: calc(100% + 10px);
    }

    .el-input {
      height: 100%;
      line-height: 0;

      .el-input__inner {
        height: 100%;
      }

      .el-input__icon {
        display: none;
      }
    }
  }
}
</style>