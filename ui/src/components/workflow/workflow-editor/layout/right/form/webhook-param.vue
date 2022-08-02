<template>
  <div :class="{'webhook-param':true,'switch-bgc':switchBackgroundFlag}">
    <i class="jm-icon-button-delete" @click="deleteParam"/>
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
      <jm-select
        v-model="typeVal" @change="changeType"
        placeholder="请选择参数类型"
        @focus="switchBackgroundFlag = true;"
        @blur="switchBackgroundFlag = false;">
        <jm-option v-for="(item,index) in ParamTypeEnum" :key="index" :label="item" :value="item"/>
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
            style="color:#fff;text-decoration: underline;">提取规则</a>
          </template>
          <i class="jm-icon-button-help"></i>
        </jm-tooltip>
      </template>
      <jm-input
        v-model="expVal"
        placeholder="请输入参数表达式"
        @change="changeExp" class="exp"
        @focus="switchBackgroundFlag = true;"
        @blur="switchBackgroundFlag = false;"
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
    <jm-form-item v-if="!requiredVal"
                  :prop="`${formModelName}.${index}.default`" :rules="rules.default"
                  label="默认值">
      <jm-input v-model="defaultVal" @change="changeDefault" placeholder="请输入默认值" @focus="switchBackgroundFlag = true;"
                @blur="switchBackgroundFlag = false;"/>
    </jm-form-item>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType, ref } from 'vue';
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
      type: String,
      default: '',
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
    const defaultVal = ref<string>(props.default || '');
    const switchBackgroundFlag = ref<boolean>(false);

    // 初始化required的值
    emit('update:required', requiredVal.value);

    return {
      nameVal,
      typeVal,
      expVal,
      requiredVal,
      defaultVal,
      ParamTypeEnum,
      switchBackgroundFlag,
      changeName: () => {
        emit('update:name', nameVal.value);
      },
      changeType: () => {
        emit('update:type', typeVal.value);
      },
      changeExp: () => {
        emit('update:exp', expVal.value);
      },
      changeDefault: () => {
        emit('update:default', defaultVal.value);
      },
      changeRequired: () => {
        emit('update:required', requiredVal.value);
        defaultVal.value = requiredVal.value ? undefined : '';
        emit('update:default', defaultVal.value);
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