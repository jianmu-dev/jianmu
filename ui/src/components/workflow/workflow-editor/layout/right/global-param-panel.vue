<template>
  <jm-drawer
    title="全局参数"
    :size="410"
    direction="rtl"
    destroy-on-close
    v-model="visible"
    @closed="close"
  >
    <template #title>
      全局参数
      <span class="link-container">
        <a href="https://docs.jianmu.dev/guide/vars.html" target="_blank" class="jm-icon-button-help"/>
      </span>
    </template>
    <div class="drawer-content">
      <jm-scrollbar class="global-scroll">
        <div class="param-container">
          <jm-form label-position="top" :model="workflowForm.global.params" ref="globalFormRef">
            <global-param
              v-for="(param,index) in workflowForm.global.params"
              :key="paramKeys[index]"
              v-model:reference="param.ref"
              v-model:name="param.name"
              v-model:type="param.type"
              v-model:required="param.required"
              v-model:value="param.value"
              v-model:hidden="param.hidden"
              :index="index"
              :rules="workflowForm.global.params[index].getFormRules()"
              @delete="deleteParam"
              @change="updateInfo"
            />
          </jm-form>
          <div class="add-param" @click="addParam">
            <i class="jm-icon-button-add"/>
            全局参数
          </div>
        </div>
      </jm-scrollbar>
    </div>
  </jm-drawer>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, nextTick, onMounted, onUpdated, PropType, ref } from 'vue';
import { ParamTypeEnum, RefTypeEnum } from '../../model/data/enumeration';
import GlobalParam from './form/global-param.vue';
import { GlobalParam as _GlobalParam } from '../../model/data/global-param';
import { IWorkflow } from '../../model/data/common';
import { v4 as uuidv4 } from 'uuid';
import { checkDuplicate } from '../../model/util/reference';

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
    const { proxy } = getCurrentInstance() as any;
    const visible = ref<boolean>(props.modelValue);
    const workflowForm = ref<IWorkflow>(props.workflowData);
    const paramKeys = ref<string[]>([]);
    workflowForm.value.global.params.forEach(() => paramKeys.value.push(uuidv4()));
    const globalFormRef = ref<HTMLFormElement>();
    const paramRefs = computed<string[]>(
      () => workflowForm.value.global.params.map(({ ref }) => ref));

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
        globalFormRef.value!.validate().catch(() => {
        });
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
        const globalParam = new _GlobalParam('', '', ParamTypeEnum.STRING, false, '', false);
        workflowForm.value.global.params.push(globalParam);
        paramKeys.value.push(uuidv4());
      },
      updateInfo: () => {
        try {
          checkDuplicate(paramRefs.value, RefTypeEnum.GLOBAL_PARAM);
        } catch (err) {
          proxy.$error(err.message);
        }
      },
    };
  },
});
</script>

<style scoped lang="less">
.el-drawer {
  .jm-icon-button-help {
    display: inline-block;
    width: 20px;
    height: 20px;
    line-height: 20px;
    text-align: center;
    font-size: 14px;
    margin-left: 2px;

    &:hover {
      background: #EFF7FF;
      color: #086CD8;
    }
  }

  .drawer-content {
    .global-scroll {
      height: calc(100vh - 140px);

      .param-container {

        .add-param {
          border-bottom: 1px solid #E6EBF2;
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