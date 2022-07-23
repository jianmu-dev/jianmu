<template>
  <div class="param-button" v-if="!hidden">
    <jm-cascader
      placement="top"
      :options="selectableParams"
      @change="insertParam"
      v-model="selectValue"
      :offset="5"
    >
      <template #default="{ data }">
        <span class="param-label">{{ data.label }}</span>
        <jm-tooltip :content="`参数类型：${getType(data?.type)}`" placement="top" v-if="data?.type">
          <i class="jm-icon-workflow-param-type"></i>
        </jm-tooltip>
      </template>
    </jm-cascader>
    <div class="insert-param">
      <i class="jm-icon-link-add"></i>
      <span class="text">参数</span>
    </div>
  </div>
</template>

<script lang='ts'>
import { computed, defineComponent, PropType, ref } from 'vue';
import { ISelectableParam } from './model/data';
import { ParamTypeEnum } from '@/components/workflow/workflow-editor/model/data/enumeration';

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
    const getType = (type: string): string => {
      switch (type) {
        case ParamTypeEnum.STRING:
          return '字符串';
        case ParamTypeEnum.NUMBER:
          return '数字';
        case ParamTypeEnum.BOOL:
          return '布尔';
      }
    };
    return {
      insertParam(arr: string[]) {
        emit('inserted', arr);
        selectValue.value = [];
      },
      selectValue,
      hidden,
      getType,
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

<style lang="less">
.el-cascader-panel {
  .el-cascader-menu {
    .el-cascader-menu__wrap {
      .el-scrollbar__view {
        .el-cascader-node {
          .el-cascader-node__label {
            padding: 0;
            width: 100%;
            display: flex;
            justify-content: space-between;
            align-items: center;

            i {
              visibility: hidden;
              margin-left: 10px;
            }
          }

          &:hover, &:focus {
            background-color: #EFF7FF;

            .el-cascader-node__label {
              color: #096DD9;
            }
          }

          &:hover {
            .el-cascader-node__label {
              i {
                visibility: visible;
                color: #6B7B8D;

                &:hover {
                  color: #096DD9;
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>
