<template>
  <div :class="{ 'webhook-param': true, 'switch-bgc': switchBackgroundFlag }">
    <i class="jm-icon-button-delete" @click="deleteParam" />
    <jm-form-item label="名称" :prop="`${formModelName}.${index}.name`" :rules="rules.name">
      <jm-input
        v-model="nameVal"
        placeholder="请输入参数名称"
        @change="changeName"
        @focus="switchBackgroundFlag = true"
        @blur="switchBackgroundFlag = false"
      />
    </jm-form-item>
    <jm-form-item label="类型" :prop="`${formModelName}.${index}.type`" :rules="rules.type">
      <jm-select
        v-model="typeVal"
        @change="changeType"
        placeholder="请选择参数类型"
        @focus="switchBackgroundFlag = true"
        @blur="switchBackgroundFlag = false"
      >
        <jm-option v-for="(item, index) in ParamTypeEnum" :key="index" :label="item" :value="item" />
      </jm-select>
    </jm-form-item>
    <jm-form-item :prop="`${formModelName}.${index}.exp`" :rules="rules.exp">
      <template #label>
        表达式
        <jm-tooltip placement="top">
          <template #content>
            详见<a
              href="https://v2.jianmu.dev/guide/webhook.html#%E8%A7%A6%E5%8F%91%E5%99%A8%E5%8F%82%E6%95%B0%E6%8F%90%E5%8F%96%E8%A7%84%E5%88%99"
              target="_blank"
              style="color: #fff; text-decoration: underline"
              >提取规则</a
            >
          </template>
          <i class="jm-icon-button-help"></i>
        </jm-tooltip>
      </template>
      <jm-input
        v-model="expVal"
        placeholder="请输入参数表达式"
        @change="changeExp"
        class="exp"
        @focus="switchBackgroundFlag = true"
        @blur="switchBackgroundFlag = false"
      />
    </jm-form-item>
    <!--  是否必填  -->
    <jm-form-item :prop="`${formModelName}.${index}.required`" :rules="rules.required" label="是否必填">
      <jm-radio-group :model-value="requiredVal" @update:model-value="changeRequired">
        <jm-radio :label="false">否</jm-radio>
        <jm-radio :label="true">是</jm-radio>
      </jm-radio-group>
    </jm-form-item>
    <jm-form-item
      v-if="!requiredVal && isReload"
      :prop="`${formModelName}.${index}.default`"
      :rules="rules.default"
      label="默认值"
    >
      <jm-radio-group :model-value="defaultVal" v-if="type === ParamTypeEnum.BOOL" @update:model-value="changeDefault">
        <jm-radio :label="true">true</jm-radio>
        <jm-radio :label="false">false</jm-radio>
      </jm-radio-group>
      <jm-input
        v-else-if="type === ParamTypeEnum.NUMBER"
        v-model="defaultVal"
        type="number"
        @change="changeDefault"
        placeholder="请输入默认值"
        @focus="switchBackgroundFlag = true"
        @blur="switchBackgroundFlag = false"
      />
      <jm-input
        v-else
        v-model="defaultVal"
        @change="changeDefault"
        placeholder="请输入默认值"
        @focus="switchBackgroundFlag = true"
        @blur="switchBackgroundFlag = false"
      />
    </jm-form-item>
  </div>
</template>

<script lang="ts">
import { defineComponent, nextTick, PropType, ref } from 'vue';
import { ParamTypeEnum } from '../../../model/data/enumeration';
import { CustomRule } from '../../../model/data/common';

export default defineComponent({
  props: {
    name: {
      type: String,
      default: '',
    },
    type: {
      type: String as PropType<ParamTypeEnum>,
      default: '',
    },
    exp: {
      type: String,
      default: '',
    },
    required: {
      type: Boolean,
      default: false,
    },
    default: {
      type: [String, Boolean, Number],
      default: undefined,
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
  emits: ['update:name', 'update:type', 'update:exp', 'update:required', 'update:default', 'delete'],
  setup(props, { emit }) {
    const nameVal = ref<string>(props.name);
    const typeVal = ref<ParamTypeEnum>(props.type);
    const expVal = ref<string>(props.exp);
    const requiredVal = ref<boolean>(props.required);
    const defaultVal = ref<string | boolean | number | undefined>(props.default);
    const switchBackgroundFlag = ref<boolean>(false);
    // 重载，避免报错提示影响到其他类型默认值
    const isReload = ref<boolean>(true);

    // 初始化required的值
    emit('update:required', requiredVal.value);

    const resetDefaultVal = () => {
      defaultVal.value =
        requiredVal.value || typeVal.value === ParamTypeEnum.NUMBER || typeVal.value === ParamTypeEnum.BOOL
          ? undefined
          : '';
      emit('update:default', defaultVal.value);
    };

    return {
      nameVal,
      typeVal,
      expVal,
      requiredVal,
      defaultVal,
      ParamTypeEnum,
      switchBackgroundFlag,
      isReload,
      changeName: () => {
        emit('update:name', nameVal.value);
      },
      changeType: async () => {
        emit('update:default', defaultVal.value);
        emit('update:type', typeVal.value);
        isReload.value = false;
        await nextTick();
        isReload.value = true;
      },
      changeExp: () => {
        emit('update:exp', expVal.value);
      },
      changeDefault: (val: string | number | boolean) => {
        emit('update:default', val);
        defaultVal.value = val;
      },
      changeRequired: (val: boolean) => {
        emit('update:required', val);
        requiredVal.value = val;
        resetDefaultVal();
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
  border-bottom: 1px solid #e6ebf2;

  &.switch-bgc {
    background: #eff7ff;

    &:hover {
      background: #eff7ff;
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
    background: #fafafa;
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
      color: #086cd8;
      background: #eff7ff;
    }
  }

  // 帮助
  .jm-icon-button-help::before {
    margin: 0;
  }
}
</style>
