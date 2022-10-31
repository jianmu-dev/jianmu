<template>
  <jm-dialog :custom-class="entry ? 'entry' : 'center'" v-model="dialogVisible" width="700px">
    <template v-slot:title>
      <div class="editor-title">新增密钥命名空间</div>
    </template>
    <jm-form :model="editorForm" :rules="editorRule" ref="editorFormRef" @submit.prevent>
      <jm-form-item label="命名空间" label-position="top" prop="name">
        <jm-input v-model="editorForm.name" placeholder="支持下划线、数字、英文字母" show-word-limit :maxlength="100" />
      </jm-form-item>
    </jm-form>
    <template #footer>
      <span class="dialog-footer">
        <jm-button size="small" @click="dialogVisible = false">取消</jm-button>
        <jm-button size="small" type="primary" @click="create" :loading="loading">确定</jm-button>
      </span>
    </template>
  </jm-dialog>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, ref, SetupContext } from 'vue';
import { ISaveNamespaceForm } from '@/model/modules/secret-key';
import { saveNamespace } from '@/api/secret-key';
import { CredentialManagerTypeEnum } from '@/api/dto/enumeration';
import { useStore } from 'vuex';

export default defineComponent({
  props: {
    credentialManagerType: {
      type: String,
      required: true,
    },
  },
  emits: ['completed'],
  setup(_, { emit }: SetupContext) {
    const { proxy } = getCurrentInstance() as any;
    const entry = true;
    const dialogVisible = ref<boolean>(true);

    const editorFormRef = ref<any>(null);
    const editorForm = ref<ISaveNamespaceForm>({
      name: '',
    });
    const editorRule = ref<Record<string, unknown>>({
      name: [
        { required: true, message: '请输入命名空间', trigger: 'blur' },
        { pattern: /^[a-zA-Z0-9_]([a-zA-Z0-9_]+)?$/, message: '支持下划线、数字、英文字母', trigger: 'blur' },
      ],
    });
    const loading = ref<boolean>(false);

    return {
      entry,
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

              proxy.$success('新增成功');

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
  background-repeat: no-repeat;
  background-position: left center;
}
</style>
