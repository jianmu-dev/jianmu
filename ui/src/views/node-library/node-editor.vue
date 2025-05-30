<template>
  <jm-dialog
    v-model="dialogVisible"
    width="800px">
    <template v-slot:title>
      <div class="editor-title">{{ $t('nodeLibrary.addLocalNode') }}</div>
    </template>
    <jm-form :model="editorForm" :rules="editorRule" ref="editorFormRef" @submit.prevent>
      <jm-form-item :label="$t('nodeLibrary.name')" label-position="top" prop="name">
        <jm-input v-model="editorForm.name" clearable :placeholder="$t('nodeLibrary.inputName')"/>
      </jm-form-item>
      <jm-form-item :label="$t('nodeLibrary.description')" label-position="top" prop="description">
        <jm-input
          type="textarea"
          v-model="editorForm.description"
          clearable
          maxlength="200"
          show-word-limit
          :placeholder="$t('nodeLibrary.inputDescription')"
          :autosize="{minRows: 3, maxRows: 3}"
          resize="none"/>
      </jm-form-item>
      <jm-form-item prop="dsl">
        <jm-dsl-editor class="dsl-editor" v-model:value="editorForm.dsl"/>
      </jm-form-item>
    </jm-form>
    <div>{{$t('nodeLibrary.note')}}</div>
    <template #footer>
        <span class="dialog-footer">
          <jm-button size="small" @click="dialogVisible = false">{{ $t('nodeLibrary.cancel') }}</jm-button>
          <jm-button size="small" type="primary" @click="create" :loading="loading">{{ $t('nodeLibrary.confirm') }}</jm-button>
        </span>
    </template>
  </jm-dialog>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, ref, SetupContext } from 'vue';
import { ICreateNodeForm } from '@/model/modules/node-library';
import { createNode } from '@/api/node-library';
import { useLocale } from '@/utils/i18n';
export default defineComponent({
  emits: ['completed'],
  setup(_, { emit }: SetupContext) {
    const { t } = useLocale();
    const { proxy } = getCurrentInstance() as any;
    const dialogVisible = ref<boolean>(true);

    const editorFormRef = ref<any>(null);
    const editorForm = ref<ICreateNodeForm>({
      name: '',
      dsl: '',
    });
    const editorRule = ref<object>({
      name: [
        { required: true, message: t('nodeLibrary.nameRequired'), trigger: 'blur' },
      ],
      dsl: [
        { required: true, message: t('nodeLibrary.dslRequired'), trigger: 'blur' },
      ],
    });
    const loading = ref<boolean>(false);

    return {
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

          createNode({ ...editorForm.value })
            .then(() => {
              // 发已完成事件，用于后续处理
              emit('completed');

              proxy.$success(t('nodeLibrary.createSuccess'));

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

.dsl-editor {
  width: 100%;
  height: 300px;
  line-height: normal;
  z-index: 1;
}
</style>
