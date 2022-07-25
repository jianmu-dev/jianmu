<template>
  <div :class="{'webhook-param':true,'switch-bgc':switchBackgroundFlag}">
    <i class="jm-icon-button-delete" @click="deleteParam"/>
    <jm-form-item label="唯一标识" :prop="`${formModelName}.${index}.ref`" :rules="rules.ref">
      <jm-input
        v-model="refVal"
        placeholder="请输入参数唯一标识"
        @change="changeReference"
        @focus="switchBackgroundFlag = true;"
        @blur="switchBackgroundFlag = false;"
      />
    </jm-form-item>
    <jm-form-item label="名称" :prop="`${formModelName}.${index}.name`" :rules="rules.name">
      <jm-input
        v-model="nameVal"
        placeholder="请输入参数名称"
        @change="changeName"
        @focus="switchBackgroundFlag = true;"
        @blur="switchBackgroundFlag = false;"
      />
    </jm-form-item>
    <jm-form-item label="类型" :prop="`${formModelName}.${index}.type`" :rules="rules.type">
      <jm-radio-group v-model="typeVal" @change="changeType">
        <jm-radio :label="ParamTypeEnum.STRING">字符串</jm-radio>
        <jm-radio :label="ParamTypeEnum.NUMBER">数字</jm-radio>
        <jm-radio :label="ParamTypeEnum.BOOL">布尔</jm-radio>
      </jm-radio-group>
    </jm-form-item>
    <jm-form-item :prop="`${formModelName}.${index}.value`" :rules="rules.value">
      <template #label>值
        <jm-tooltip placement="top">
          <template #content>
            详见<a
            href="https://docs.jianmu.dev/guide/webhook.html#%E8%A7%A6%E5%8F%91%E5%99%A8%E5%8F%82%E6%95%B0%E6%8F%90%E5%8F%96%E8%A7%84%E5%88%99"
            target="_blank"
            style="color:#fff;text-decoration: underline;">提取规则</a>
          </template>
          <i class="jm-icon-button-help"></i>
        </jm-tooltip>
      </template>
      <expression-editor
        v-model="valueVal"
        :type="ExpressionTypeEnum.WEBHOOK_PARAM"
        :node-id="nodeId"
        :param-type="typeVal"
        placeholder="请输入参数值"
        @change="changeValue"
        @focus="switchBackgroundFlag=true"
        @blur="switchBackgroundFlag=false"
      />
    </jm-form-item>
    <!--  是否必填  -->
    <jm-form-item :prop="`${formModelName}.${index}.required`" :rules="rules.required"
                  label="是否必填">
      <jm-radio-group v-model="requiredVal" @change="changeRequired">
        <jm-radio :label="false">否</jm-radio>
        <jm-radio :label="true">是</jm-radio>
      </jm-radio-group>
    </jm-form-item>
    <jm-form-item  :prop="`${formModelName}.${index}.hidden`" :rules="rules.hidden">
      <template #label>
        脱敏显示
        <jm-tooltip placement="top">
          <template #content>
            脱敏后参数值将以******的形式显示
          </template>
          <i class="jm-icon-button-help"></i>
        </jm-tooltip>
      </template>
      <jm-radio-group v-model="hiddenVal" @change="changeHidden">
        <jm-radio :label="false">否</jm-radio>
        <jm-radio :label="true">是</jm-radio>
      </jm-radio-group>
    </jm-form-item>
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
    nodeId: {
      type: String,
      required: true,
    },
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
    value: {
      type: String,
      default: '',
    },
    required: {
      type: Boolean,
      default: false,
    },
    hidden: {
      type: Boolean,
      default: false,
    },
    index: {
      type: Number,
      required: true,
    },
    formModelName: {
      type: String,
      required: true,
    },
    rules: {
      type: Object as PropType<Record<string, CustomRule>>,
      required: true,
    },
  },
  emits: [
    'update:reference',
    'update:name',
    'update:type',
    'update:value',
    'update:required',
    'update:hidden',
    'delete',
  ],
  setup(props, { emit }) {
    const refVal = ref<string>(props.reference);
    const nameVal = ref<string>(props.name);
    const typeVal = ref<ParamTypeEnum>(props.type);
    const valueVal = ref<string>(props.value);
    const requiredVal = ref<boolean>(props.required);
    const hiddenVal = ref<boolean>(props.hidden);
    const switchBackgroundFlag = ref<boolean>(false);

    // 初始化required的值
    emit('update:required', requiredVal.value);

    return {
      refVal,
      nameVal,
      typeVal,
      valueVal,
      requiredVal,
      hiddenVal,
      ParamTypeEnum,
      ExpressionTypeEnum,
      switchBackgroundFlag,
      changeReference: () => {
        emit('update:reference', refVal.value);
      },
      changeName: () => {
        emit('update:name', nameVal.value);
      },
      changeType: () => {
        emit('update:type', typeVal.value);
      },
      changeValue: () => {
        emit('update:value', valueVal.value);
      },
      changeRequired: () => {
        emit('update:required', requiredVal.value);
      },
      changeHidden:()=>{
        emit('update:hidden', hiddenVal.value);
      },
      deleteParam: () => {
        emit('delete', props.index, props.name);
      },
    };
  },
});
</script>

<style scoped lang="less">
.webhook-param {
  font-size: 14px;
  padding: 20px 20px 10px;
  position: relative;
  border-bottom: 1px solid #E6EBF2;

  &.switch-bgc {
    background: #EFF7FF;

    &:hover {
      background: #EFF7FF;
    }
  }

  //  默认值
  .default-value {
    margin-top: 10px;

    .default-value-label {
      display: inline-block;
      margin-bottom: 10px;
    }
  }

  &:hover {
    background: #FAFAFA;
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

  // 帮助
  .jm-icon-button-help::before {
    margin: 0;
  }
}


</style>