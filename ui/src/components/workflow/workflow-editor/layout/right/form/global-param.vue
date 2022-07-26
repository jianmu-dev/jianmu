<template>
  <div :class="['global-param',switchFlag?'switch-bgc':'']">
    <jm-form-item label="唯一标识" :prop="`${index}.ref`" :rules="rules.ref" class="param-item">
      <jm-input
        v-model="referenceVal"
        placeholder="请输入唯一标识"
        show-word-limit
        :maxlength="30"
        @change="changeRef"
        @focus="switchFlag = true"
        @blur="switchFlag = false"
      />
    </jm-form-item>
    <jm-form-item label="名称" class="param-item">
      <jm-input
        v-model="nameVal"
        placeholder="请输入参数名称"
        show-word-limit
        :maxlength="45"
        @change="changeName"
        @focus="switchFlag = true"
        @blur="switchFlag = false"
      />
    </jm-form-item>
    <jm-form-item label="类型" :prop="`${index}.type`" :rules="rules.type" class="param-item">
      <jm-radio-group v-model="typeVal" @change="changeType">
        <jm-radio :label="ParamTypeEnum.STRING">字符串</jm-radio>
        <jm-radio :label="ParamTypeEnum.NUMBER">数字</jm-radio>
        <jm-radio :label="ParamTypeEnum.BOOL">布尔</jm-radio>
      </jm-radio-group>
    </jm-form-item>
    <jm-form-item label="值" :prop="`${index}.value`" :rules="rules.value" class="param-item">
      <expression-editor
        v-model="valueVal"
        :type="ExpressionTypeEnum.GLOBAL_PARAM"
        :param-type="typeVal"
        placeholder="请输入参数值"
        @change="changeValue"
        @focus="switchFlag=true"
        @blur="switchFlag=false"
      />
    </jm-form-item>
    <jm-form-item label="是否必填" @change="changeRequired" :prop="`${index}.required`" :rules="rules.required"
                  class="param-item">
      <jm-radio-group v-model="requiredVal">
        <jm-radio :label="false">否</jm-radio>
        <jm-radio :label="true">是</jm-radio>
      </jm-radio-group>
    </jm-form-item>
    <jm-form-item @change="changeHidden" :prop="`${index}.hidden`" :rules="rules.hidden"
                  class="param-item">
      <template #label>
        脱敏显示
        <jm-tooltip placement="top">
          <template #content>
            脱敏后参数值将以******的形式显示
          </template>
          <i class="jm-icon-button-help"></i>
        </jm-tooltip>
      </template>
      <jm-radio-group v-model="hiddenVal">
        <jm-radio :label="false">否</jm-radio>
        <jm-radio :label="true">是</jm-radio>
      </jm-radio-group>
    </jm-form-item>
    <i class="jm-icon-button-delete" @click="deleteParam"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType, ref } from 'vue';
import { ExpressionTypeEnum, ParamTypeEnum } from '../../../model/data/enumeration';
import { CustomRule } from '../../../model/data/common';
import ExpressionEditor from '../form/expression-editor.vue';

export default defineComponent({
  components: { ExpressionEditor },
  props: {
    reference: {
      type: String,
      default: '',
    },
    name: {
      type: String,
      default: '',
    },
    type: {
      type: String as PropType<ParamTypeEnum>,
      default: ParamTypeEnum.STRING,
    },
    required: {
      type: Boolean,
      default: false,
    },
    value: {
      type: String,
      default: '',
    },
    hidden: {
      type: Boolean,
      default: false,
    },
    index: {
      type: Number,
      required: true,
    },
    rules: {
      type: Object as PropType<Record<string, CustomRule>>,
      required: true,
    },
  },
  emits: ['update:reference', 'update:name', 'update:type', 'update:required', 'update:value', 'update:hidden', 'delete', 'change'],
  setup(props, { emit }) {
    const referenceVal = ref<string>(props.reference);
    const nameVal = ref<string>(props.name);
    const typeVal = ref<ParamTypeEnum>(props.type);
    const requiredVal = ref<boolean>(props.required);
    const valueVal = ref<string>(props.value);
    const hiddenVal = ref<boolean>(props.hidden);
    // 切换背景
    const switchFlag = ref<boolean>(false);

    return {
      referenceVal,
      nameVal,
      typeVal,
      requiredVal,
      valueVal,
      hiddenVal,
      switchFlag,
      ParamTypeEnum,
      ExpressionTypeEnum,
      changeRef: () => {
        emit('update:reference', referenceVal.value);
        emit('change');
      },
      changeName: () => {
        emit('update:name', nameVal.value);
        emit('change');
      },
      changeType: () => {
        emit('update:type', typeVal.value);
        emit('change');
      },
      changeRequired: () => {
        emit('update:required', requiredVal.value);
        emit('change');
      },
      changeValue: () => {
        emit('update:value', valueVal.value);
        emit('change');
      },
      changeHidden: () => {
        emit('update:hidden', hiddenVal.value);
        emit('change');
      },
      deleteParam: () => {
        emit('delete', props.index);
      },
    };
  },
});
</script>

<style scoped lang="less">
.global-param {
  border-bottom: 1px solid #E6EBF2;
  padding: 10px 20px 10px;
  position: relative;

  &:hover {
    background: #FAFAFA;
  }

  &.switch-bgc {
    background: #EFF7FF;

    &:hover {
      background: #EFF7FF;
    }
  }

  .param-item {
    padding-top: 10px;
  }

  // 删除
  .jm-icon-button-delete {
    display: inline-block;
    width: 20px;
    height: 20px;
    cursor: pointer;
    position: absolute;
    top: 10px;
    right: 10px;

    &:hover {
      color: #086CD8;
      background: #EFF7FF;
    }
  }
}
</style>
