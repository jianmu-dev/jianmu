<template>
  <div :class="{'shell-env':true,'switch-bgc':switchBackgroundFlag}">
    <jm-form-item :prop="`${formModelName}.${index}.name`" :rules="rules.name" label="变量名称">
      <jm-input
        v-model="envName"
        @input="upperCase"
        @change="changeEnv"
        class="change-env"
        placeholder="请输入变量名称"
        @focus="switchBackgroundFlag=true"
        @blur="switchBackgroundFlag=false"/>
    </jm-form-item>

    <jm-form-item :prop="`${formModelName}.${index}.value`" :rules="rules.value" v-if="valueVisible">
      <template #label>变量值
        <jm-tooltip placement="top" :content="switchValueType?'切换至选择密钥模式':'切换至输入参数模式'">
          <i class="jm-icon-workflow-select-mode" v-if="switchValueType" @click="switchEnvMode(false)"/>
          <i class="jm-icon-workflow-edit-mode" v-else @click="switchEnvMode(true)"/>
        </jm-tooltip>
      </template>
      <expression-editor
        v-if="switchValueType"
        v-model="envVal"
        :type="ExpressionTypeEnum.SHELL_ENV"
        :node-id="nodeId"
        :param-type="ParamTypeEnum.STRING"
        @change="changeVal"
        class="shell-env-val"
        placeholder="请输入变量值"
        @focus="switchBackgroundFlag=true"
        @blur="switchBackgroundFlag=false"/>
      <secret-key-selector
        v-else
        v-model="envVal"
        placeholder="请选择变量值"
        @update:model-value="updateEnvVal"
      />
    </jm-form-item>
    <i class="jm-icon-button-delete" @click="remove"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject, nextTick, onMounted, PropType, ref } from 'vue';
import { CustomRule } from '../../../model/data/common';
import ExpressionEditor from './expression-editor.vue';
import SecretKeySelector from './secret-key-selector.vue';
import { Node } from '@antv/x6';
import { ExpressionTypeEnum, ParamTypeEnum } from '../../../model/data/enumeration';

export default defineComponent({
  components: { ExpressionEditor, SecretKeySelector },
  props: {
    name: {
      type: String,
      default: '',
    },
    value: {
      type: String,
      default: '',
    },
    type: {
      type: String as PropType<ParamTypeEnum>,
      default: ParamTypeEnum.STRING,
    },
    index: {
      type: Number,
      required: true,
    },
    rules: {
      type: Object as PropType<Record<string, CustomRule>>,
      required: true,
    },
    formModelName: {
      type: String,
      required: true,
    },
  },
  emits: ['update:name', 'update:value', 'update:type', 'delete'],
  setup(props, { emit }) {
    const envName = ref<string>(props.name);
    const envVal = ref<string>(props.value);
    const switchBackgroundFlag = ref<boolean>(false);
    const switchValueType = ref<boolean>(true);
    const envValRef = ref<HTMLElement>();
    const nodeId = ref<string>('');
    const getNode = inject('getNode') as () => Node;
    const valueVisible = ref<boolean>(true);
    nodeId.value = getNode().id;

    onMounted(() => {
      switchValueType.value = props.type === ParamTypeEnum.STRING;
    });

    return {
      ParamTypeEnum,
      ExpressionTypeEnum,
      envName,
      envVal,
      switchBackgroundFlag,
      envValRef,
      nodeId,
      switchValueType,
      valueVisible,
      upperCase: () => {
        envName.value = envName.value.toUpperCase();
      },
      changeEnv: (val: string) => {
        emit('update:name', val);
      },
      changeVal: (val: string) => {
        emit('update:value', val);
      },
      remove: () => {
        emit('delete', props.index);
      },
      // 带参数区分tab
      switchEnvMode: async(flag: boolean) => {
        switchValueType.value = flag;
        valueVisible.value = false;
        await nextTick();
        valueVisible.value = true;
        emit('update:type', flag ? ParamTypeEnum.STRING : ParamTypeEnum.SECRET);
      },
      // 密钥组件更新值
      updateEnvVal: (val: string) => {
        envVal.value = val;
        emit('update:value', val);
      },
    };
  },
});
</script>

<style scoped lang="less">
.shell-env {
  font-size: 14px;
  padding: 20px 20px 10px;
  position: relative;
  border-bottom: 1px solid #E6EBF2;

  &:hover {
    background: #FAFAFA;
  }

  &.switch-bgc {
    background: #EFF7FF;

    &:hover {
      background: #EFF7FF;
    }
  }

  // 输入
  .jm-icon-workflow-select-mode,
  .jm-icon-workflow-edit-mode {
    width: 20px;
    height: 20px;
    color: #6B7B8D;

    &:hover {
      cursor: pointer;
      color: #096DD9;
    }
  }

  .jm-icon-button-delete {
    width: 20px;
    height: 20px;
    line-height: 20px;
    cursor: pointer;
    position: absolute;
    top: 10px;
    right: 10px;

    &:hover {
      background: #EFF7FF;
      color: #086CD8;
    }
  }
}
</style>