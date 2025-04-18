<template>
  <jm-dialog
    v-model="dialogVisible"
    width="500px">
    <template v-slot:title>
      <div class="editor-title">{{ t('skEditor.title') }}</div>
    </template>
    <jm-form :model="editorForm" :rules="editorRule" ref="editorFormRef" @submit.prevent>
      <jm-form-item :label="t('skEditor.name')" label-position="top" prop="key">
        <jm-input v-model="editorForm.key" clearable :placeholder="t('skEditor.namePlaceholder')" />
      </jm-form-item>
      <jm-form-item :label="t('skEditor.value')" label-position="top" prop="value">
        <jm-input
          type="textarea"
          v-model="editorForm.value"
          clearable
          :placeholder="t('skEditor.valuePlaceholder')"
          :autosize="{minRows: 6, maxRows: 10}"/>
      </jm-form-item>
    </jm-form>
    <template #footer>
        <span class="dialog-footer">
          <jm-button size="small" @click="dialogVisible = false">{{ t('skEditor.cancel') }}</jm-button>
          <jm-button size="small" type="primary" @click="create" :loading="loading">{{ t('skEditor.confirm') }}</jm-button>
        </span>
    </template>
  </jm-dialog>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, ref, SetupContext } from 'vue';
import { ICreateSecretKeyForm } from '@/model/modules/secret-key';
import { createSecretKey } from '@/api/secret-key';
import { useLocale } from '@/utils/i18n';

export default defineComponent({
  props: {
    ns: {
      type: String,
      required: true,
    },
  },
  emits: ['completed'],
  setup(props: any, { emit }: SetupContext) {
    const { t } = useLocale();
    const { proxy } = getCurrentInstance() as any;
    const dialogVisible = ref<boolean>(true);

    const editorFormRef = ref<any>(null);
    const editorForm = ref<ICreateSecretKeyForm>({
      namespace: props.ns,
      key: '',
      value: '',
    });
    const editorRule = ref<object>({
      key: [
        { required: true, message: t('skEditor.nameRequired'), trigger: 'blur' },
      ],
      value: [
        { required: true, message: t('skEditor.valueRequired'), trigger: 'blur' },
      ],
    });
    const loading = ref<boolean>(false);

    return {
      t,
      dialogVisible,
      editorFormRef,
      editorForm,
      editorRule,
      loading,
      create: () => {
        // 开启loading
        loading.value = true;

        editorFormRef.value.validate((valid: boolean) => {
          if (!valid) {
            // 关闭loading
            loading.value = false;

            return false;
          }

          createSecretKey({ ...editorForm.value })
            .then(() => {
              // 发已完成事件，用于后续处理
              emit('completed', editorForm.value.namespace, editorForm.value.key);

              proxy.$success(t('skEditor.success'));

              dialogVisible.value = false;
            })
            .catch((err: Error) => {
              // 关闭loading
              loading.value = false;

              proxy.$throw(err, proxy);
            });
        });
      },
    };
  },
});
</script>

<style scoped lang="less">
.editor-title {
  padding-left: 36px;
  background-image: url('@/assets/svgs/btn/edit.svg');
  background-repeat: no-repeat;
  background-position: left center;
}
</style>
