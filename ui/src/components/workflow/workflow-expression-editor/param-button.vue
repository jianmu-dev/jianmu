<template>
  <div class="param-button" v-if="!hidden">
    <jm-cascader
      placement="top"
      :options="selectableParams"
      @change="insertParam"
      v-model="selectValue"
      :offset="5"
    />
    <div class="insert-param">
      <i class="jm-icon-link-add"></i>
      <span class="text">参数</span>
    </div>
  </div>
</template>

<script lang='ts'>
import { computed, defineComponent, PropType, ref } from 'vue';
import { ISelectableParam } from './model/data';

export default defineComponent({
  emits: ['inserted'],
  props: {
    selectableParams: {
      type: Array as PropType<ISelectableParam[]>,
      required: true,
    },
  },
  setup(props, { emit }) {
    const selectValue = ref<string[]>([]);
    const hidden = computed<boolean>(() => props.selectableParams.length === 0);
    return {
      insertParam(arr: string[]) {
        emit('inserted', arr);
        selectValue.value = [];
      },
      selectValue,
      hidden,
    };
  },
});
</script>

<style scoped lang='less'>
@import "../workflow-editor/vars";

.param-button {
  color: #6B7B8D;
  position: relative;

  &:hover {
    color: @primary-color;
  }

  .insert-param {
    color: inherit;
    cursor: pointer;
    display: flex;
    align-items: center;
    height: 20px;

    .text {
      font-size: 12px;
      margin-left: 3px;
    }
  }

  ::v-deep(.el-cascader) {
    position: absolute;
    right: 0;
    bottom: 0;
    width: 100%;
    opacity: 0;
    line-height: 0;

    .el-input {
      line-height: 0;
      overflow: hidden;

      .el-input__inner {
        height: 100%;
        line-height: 0;
      }

      .el-input__icon {
        line-height: 0;
      }

      .el-input__suffix {
        display: none;
      }
    }
  }
}
</style>
