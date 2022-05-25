<template>
  <jm-dialog
    v-model="dialogVisible"
    width="500px">
    <template v-slot:title>
      <div class="editor-title">新增密钥</div>
    </template>
    <jm-form :model="editorForm" :rules="editorRule" ref="editorFormRef" @submit.prevent>
      <jm-form-item label="名称" label-position="top" prop="key">
        <jm-input v-model="editorForm.key" clearable placeholder="请输入名称"/>
      </jm-form-item>
      <jm-form-item label="值" label-position="top" prop="value">
        <jm-input
          type="textarea"
          v-model="editorForm.value"
          clearable
          placeholder="请输值"
          :autosize="{minRows: 6, maxRows: 10}"/>
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
import { ICreateSecretKeyForm } from '@/model/modules/secret-key';
import { createSecretKey } from '@/api/secret-key';

export default defineComponent({
  props: {
    ns: {
      type: String,
      required: true,
    },
  },
  emits: ['completed'],
  setup(props: any, { emit }: SetupContext) {
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
        { required: true, message: '名称不能为空', trigger: 'blur' },
      ],
      value: [
        { required: true, message: '值不能为空', trigger: 'blur' },
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

          createSecretKey({ ...editorForm.value })
            .then(() => {
              // 发已完成事件，用于后续处理
              emit('completed', editorForm.value.namespace, editorForm.value.key);

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
  padding-left: 36px;
  background-image: url('@/assets/svgs/btn/edit.svg');
  background-repeat: no-repeat;
  background-position: left center;
}
</style>
