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

    <jm-form-item :prop="`${formModelName}.${index}.value`" :rules="rules.value" label="变量值">
      <expression-editor
        v-model="envVal"
        :node-id="nodeId"
        @change="changeVal"
        class="shell-env-val"
        placeholder="请输入变量值"
        @focus="switchBackgroundFlag=true"
        @blur="switchBackgroundFlag=false"/>
    </jm-form-item>
    <i class="jm-icon-button-delete" @click="remove"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject, PropType, ref } from 'vue';
import { CustomRule } from '../../../model/data/common';
import ExpressionEditor from './expression-editor.vue';
import { Node } from '@antv/x6';

export default defineComponent({
  components: { ExpressionEditor },
  props: {
    name: {
      type: String,
      default: '',
    },
    value: {
      type: String,
      default: '',
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
  emits: ['update:name', 'update:value', 'delete'],
  setup(props, { emit }) {
    const envName = ref<string>(props.name);
    const envVal = ref<string>(props.value);
    const switchBackgroundFlag = ref<boolean>(false);
    const envValRef = ref<HTMLElement>();
    const nodeId = ref<string>('');
    const getNode = inject('getNode') as () => Node;
    nodeId.value = getNode().id;


    return {
      envName,
      envVal,
      switchBackgroundFlag,
      envValRef,
      nodeId,
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