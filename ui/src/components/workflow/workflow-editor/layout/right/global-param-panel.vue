<template>
  <jm-drawer title="全局参数" :size="410" direction="rtl" destroy-on-close v-model="visible" @closed="close">
    <template #title>
      <div>全局参数</div>
    </template>
    <div class="drawer-content">
      <jm-scrollbar class="global-scroll">
        <div class="param-container">
          <jm-form label-position="top" :model="workflowForm.global.params" ref="globalFormRef" @submit.prevent>
            <global-param
              v-for="(param, index) in workflowForm.global.params"
              :key="paramKeys[index]"
              v-model:reference="param.ref"
              v-model:name="param.name"
              v-model:type="param.type"
              v-model:required="param.required"
              v-model:value="param.value"
              v-model:hidden="param.hidden"
              :index="index"
              :rules="workflowForm.global.getFormRules().params.fields[index].fields"
              @change-reference="(newVal, oldVal) => changeReference(index, oldVal)"
              @delete="deleteParam"
            />
          </jm-form>
          <div class="add-param" @click="addParam">
            <i class="jm-icon-button-add" />
            全局参数
          </div>
        </div>
      </jm-scrollbar>
    </div>
  </jm-drawer>
</template>

<script lang="ts">
import { defineComponent, nextTick, onMounted, onUpdated, PropType, ref } from 'vue';
import { ParamTypeEnum } from '../../model/data/enumeration';
import GlobalParam from './form/global-param.vue';
import { IWorkflow } from '../../model/data/common';
import { v4 as uuidv4 } from 'uuid';

export default defineComponent({
  components: { GlobalParam },
  props: {
    modelValue: {
      type: Boolean,
      default: false,
    },
    workflowData: {
      type: Object as PropType<IWorkflow>,
      required: true,
    },
  },
  emits: ['closed'],
  setup(props, { emit }) {
    const visible = ref<boolean>(props.modelValue);
    const workflowForm = ref<IWorkflow>(props.workflowData);
    const paramKeys = ref<string[]>([]);
    workflowForm.value.global.params.forEach(() => paramKeys.value.push(uuidv4()));
    const globalFormRef = ref<HTMLFormElement>();

    onUpdated(async () => {
      if (visible.value === props.modelValue) {
        return;
      }
      visible.value = props.modelValue;
    });
    onMounted(async () => {
      // 抽屉打开并且params有值的时候触发表单校验
      if (visible.value && workflowForm.value.global.params.length > 0) {
        await nextTick();
        // eslint-disable-next-line @typescript-eslint/no-empty-function
        globalFormRef.value!.validate().catch(() => {});
      }
    });
    return {
      visible,
      workflowForm,
      paramKeys,
      globalFormRef,
      close: () => {
        workflowForm.value.global.params.forEach(param => {
          if (!param.name) {
            param.name = param.ref;
          }
        });
        visible.value = false;
        emit('closed', visible.value);
      },
      deleteParam: (index: number) => {
        workflowForm.value.global.params.splice(index, 1);
        paramKeys.value.splice(index, 1);
      },
      addParam: () => {
        // 解构params参数
        workflowForm.value.global.params.push({
          ref: '',
          name: '',
          type: ParamTypeEnum.STRING,
          required: false,
          value: '',
          hidden: false,
        });
        paramKeys.value.push(uuidv4());
      },
      changeReference: (index: number, oldVal: string) => {
        workflowForm.value.global.params.forEach(({ ref }, idx) => {
          if (index === idx || oldVal !== ref) {
            return;
          }
          globalFormRef.value?.validateField(`${idx}.ref`);
        });
      },
    };
  },
});
</script>

<style scoped lang="less">
.el-drawer {
  .drawer-content {
    .global-scroll {
      height: calc(100vh - 140px);

      .param-container {
        .add-param {
          border-bottom: 1px solid #e6ebf2;
          padding: 10px 20px;
          color: #306bd2;
          font-size: 14px;
          cursor: pointer;

          .jm-icon-button-add::before {
            font-weight: 700;
          }
        }
      }
    }
  }
}
</style>
