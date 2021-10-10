<template>
  <div class="editor-form-dialog">
    <jm-dialog
      :model-value="modelValue"
      destroy-on-close
      @update:model-value="changeDialogVisible"
      width="500px">
      <template v-slot:title>
        <div class="editor-title">{{ title }}</div>
      </template>
      <jm-form :model="editorForm" :rules="editorRule" ref="editorFormRef" @submit.prevent>
        <jm-form-item label="名称" label-position="top" prop="name">
          <jm-input v-model="editorForm.name" clearable placeholder="请输入名称"/>
        </jm-form-item>
        <jm-form-item v-if="formType === SubFormTypeEnum.TARGET" label="唯一标识" label-position="top" prop="ref">
          <jm-input v-model="editorForm.ref" clearable placeholder="请输入ref"/>
        </jm-form-item>
      </jm-form>
      <template #footer>
        <span class="dialog-footer">
          <jm-button size="small" @click="changeDialogVisible(false)" icon="jm-icon-button-cancel">取消</jm-button>
          <jm-button size="small" @click="confirm" type="primary" icon="jm-icon-button-preserve">确定</jm-button>
        </span>
      </template>
    </jm-dialog>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, PropType, ref, SetupContext, watch } from 'vue';
import { SubFormTypeEnum } from '@/model/modules/event-bridge';

export default defineComponent({
  props: {
    modelValue: {
      type: Boolean,
      required: true,
    },
    formType: {
      type: Number as PropType<SubFormTypeEnum | -1>,
      required: true,
    },
    name: {
      type: String,
      required: true,
    },
    _ref: String,
  },
  emits: ['update:modelValue', 'update:name', 'update:_ref', 'confirmed'],
  setup(props: any, { emit }: SetupContext) {
    const editorFormRef = ref<any>(null);
    const editorForm = ref<{
      name: string;
      ref?: string;
    }>({
      name: '',
    });
    const editorRule = ref<object>({
      name: [
        { required: true, message: '名称不能为空', trigger: 'blur' },
      ],
      ref: [
        { required: true, message: '唯一标识不能为空', trigger: 'blur' },
      ],
    });

    const changeDialogVisible = (newValue: boolean) => {
      emit('update:modelValue', newValue);
    };

    watch(() => props.name, (newValue: string) => (editorForm.value.name = newValue));
    watch(() => props._ref, (newValue?: string) => (editorForm.value.ref = newValue));

    return {
      SubFormTypeEnum,
      title: computed<string>(() => {
        let title;

        switch (props.formType) {
          case SubFormTypeEnum.BRIDGE:
            title = '修改事件桥接器名称';
            break;
          case SubFormTypeEnum.SOURCE:
            title = '修改来源名称';
            break;
          case SubFormTypeEnum.TARGET:
            title = '修改目标';
            break;
        }

        return title;
      }),
      editorFormRef,
      editorForm,
      editorRule,
      changeDialogVisible,
      confirm: () => {
        editorFormRef.value.validate((valid: boolean) => {
          if (!valid) {
            return false;
          }

          emit('update:name', editorForm.value.name);
          emit('update:_ref', editorForm.value.ref);
          emit('confirmed');

          changeDialogVisible(false);
        });
      },
    };
  },
});
</script>

<style scoped lang="less">
.editor-form-dialog {

}
</style>