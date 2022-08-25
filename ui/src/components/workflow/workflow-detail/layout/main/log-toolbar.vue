<template>
  <div class="float-param-log" v-show="!dslMode">
    <jm-tooltip content="全局参数" placement="bottom" :appendToBody="false">
      <div class="jm-icon-workflow-param" v-show="showParamBtn" @click="$emit('click-param-log')"></div>
    </jm-tooltip>
    <div class="btn" v-show="showLogBtn" @click="$emit('click-process-log')">{{entry? '流水线':dslType===DslTypeEnum.PIPELINE?'管道':'流程'}}日志</div>
  </div>
</template>
<script lang="ts">
import { DslTypeEnum } from '@/api/dto/enumeration';
import { defineComponent, PropType } from 'vue';
export default defineComponent({
  props: {
    entry: {
      type: Boolean,
      default: true,
    },
    dslMode: {
      type: Boolean,
      required: true,
    },
    dslType: {
      type: String as PropType<DslTypeEnum>,
      required: true,
    },
    showLogBtn: {
      type: Boolean,
      required: true,
    },
    showParamBtn: {
      type: Boolean,
      required: true,
    },
  },
  emits: ['click-param-log', 'click-process-log'],
  setup() {
    return {
      DslTypeEnum,
    };
  },
});
</script>

<style lang="less">
.float-param-log {
  display: flex;
  position: absolute;
  z-index: 10;
  top: 139px;
  right: 30px;
  height: 60px;
  align-items: center;
  .jm-icon-workflow-param {
    margin-right: 60px;
    padding: 0 6px;
    background-color: #fff;
    border-radius: 2px;
    border: 0.5px solid rgba(227, 233, 246, 1);
    font-size: 24px;
    cursor: pointer;
    &:hover:before {
      color: #096DD9;
    }
    &::before {
      color: #52657a;
    }
  }
  .btn {
    font-size: 14px;
    color: #096DD9;
    width: 96px;
    line-height: 36px;
    text-align: center;
    cursor: pointer;
    border-radius: 2px;
    border: 0.5px solid rgba(227, 233, 246, 1);
    &:hover {
      background-color: #EFF7FF;
    }
  }
}
</style>