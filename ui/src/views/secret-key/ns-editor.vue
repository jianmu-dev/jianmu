<template>
  <jm-dialog
    v-model="dialogVisible"
    width="500px">
    <template v-slot:title>
      <div class="editor-title">{{ t('nsEditor.title') }}</div>
    </template>
    <jm-form :model="editorForm" :rules="editorRule" ref="editorFormRef" @submit.prevent>
      <jm-form-item :label="t('nsEditor.name')" label-position="top" prop="name">
        <jm-input v-model="editorForm.name" clearable :placeholder="t('nsEditor.namePlaceholder')" />
      </jm-form-item>
      <jm-form-item v-if="credentialManagerType === CredentialManagerTypeEnum.LOCAL" :label="t('nsEditor.description')" label-position="top"
                    prop="description">
        <jm-input
          type="textarea"
          v-model="editorForm.description"
          clearable
          maxlength="256"
          show-word-limit
          :placeholder="t('nsEditor.descriptionPlaceholder')"
          :autosize="{minRows: 6, maxRows: 10}"/>
      </jm-form-item>
    </jm-form>
    <template #footer>
        <span class="dialog-footer">
          <jm-button size="small" @click="dialogVisible = false">{{ t('nsEditor.cancel') }}</jm-button>
          <jm-button size="small" type="primary" @click="create" :loading="loading">{{ t('nsEditor.confirm') }}</jm-button>
        </span>
    </template>
  </jm-dialog>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, ref, SetupContext } from 'vue';
import { ISaveNamespaceForm } from '@/model/modules/secret-key';
import { saveNamespace } from '@/api/secret-key';
import { CredentialManagerTypeEnum } from '@/api/dto/enumeration';
import { useLocale } from '@/utils/i18n';

export default defineComponent({
  props: {
    credentialManagerType: {
      type: String,
      required: true,
    },
  },
  emits: ['completed'],
  setup(_, { emit }: SetupContext) {
    const { t } = useLocale();
    const { proxy } = getCurrentInstance() as any;
    const dialogVisible = ref<boolean>(true);

    const editorFormRef = ref<any>(null);
    const editorForm = ref<ISaveNamespaceForm>({
      name: '',
    });
    const editorRule = ref<object>({
      name: [
        { required: true, message: t('nsEditor.nameRequired'), trigger: 'blur' },
      ],
    });
    const loading = ref<boolean>(false);

    return {
      t,
      CredentialManagerTypeEnum,
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

          saveNamespace({ ...editorForm.value })
            .then(() => {
              // 发已完成事件，用于后续处理
              emit('completed');

              proxy.$success(t('nsEditor.success'));

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
