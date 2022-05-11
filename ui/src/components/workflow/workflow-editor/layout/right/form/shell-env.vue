<template>
  <div :class="{'shell-env':true,'switch-bgc':switchBackgroundFlag}">
    <jm-form-item :prop="`${formModelName}.${index}.name`" :rules="rules.name">
      <jm-input
        v-model="envName"
        @input="upperCase"
        @change="changeEnv"
        class="change-env"
        placeholder="变量名"
        @focus="switchBackgroundFlag=true"
        @blur="switchBackgroundFlag=false"/>
    </jm-form-item>

    <span class="separate">:</span>
    <jm-form-item :prop="`${formModelName}.${index}.value`" :rules="rules.value">
      <jm-input
        v-model="envVal"
        @change="changeVal"
        class="shell-env-val"
        placeholder="变量值"
        @focus="switchBackgroundFlag= true"
        @blur="switchBackgroundFlag=false"
        ref="envValRef"
      />
    </jm-form-item>
    <div class="delete-icon">
      <i class="jm-icon-button-delete" @click="remove"/>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType, ref } from 'vue';
import { CustomRule } from '@/components/workflow/workflow-editor/model/data/common';

export default defineComponent({
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

    return {
      envName,
      envVal,
      switchBackgroundFlag,
      envValRef,
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
  width: 100%;
  box-sizing: border-box;
  display: flex;
  align-items: flex-start;
  font-size: 14px;
  padding: 20px 20px 10px;

  &.switch-bgc {
    background: #EFF7FF;

    &:hover {
      background: #EFF7FF;
    }
  }

  &:hover {
    background: #FAFAFA;
  }

  .change-env {
    width: 106px;
  }

  .shell-env-val {
    width: 146px;
  }

  .separate {
    margin: 0 10px;
    height: 36px;
    line-height: 36px;
  }

  .delete-icon {
    height: 36px;
    line-height: 36px;
    margin-left: 10px;

    .jm-icon-button-delete {
      width: 20px;
      height: 20px;
      cursor: pointer;

      &:hover {
        background: #EFF7FF;
        color: #086CD8;
      }
    }
  }
}
</style>