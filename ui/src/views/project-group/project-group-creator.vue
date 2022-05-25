<template>
  <jm-dialog v-model="dialogVisible" width="776px" :destroy-on-close="true">
    <template #title>
      <div class="creator-title">新建项目分组</div>
    </template>
    <jm-form
      :model="createForm"
      :rules="editorRule"
      ref="createFormRef"
      @submit.prevent
    >
      <jm-form-item label="分组名称" label-position="top" prop="name">
        <jm-input
          v-model="createForm.name"
          clearable
          placeholder="请输入分组名称"
        />
      </jm-form-item>
      <jm-form-item
        label="首页展示"
        label-position="top"
        prop="isShow"
        class="is-show"
      >
        <jm-switch v-model="createForm.isShow" active-color="#096DD9"/>
      </jm-form-item>
      <jm-form-item label="描述" label-position="top" prop="description">
        <jm-input
          type="textarea"
          v-model="createForm.description"
          clearable
          maxlength="256"
          show-word-limit
          placeholder="请输入描述"
          :autosize="{ minRows: 6, maxRows: 10 }"
        />
        <div class="tips">描述信息不超过 256个字符</div>
      </jm-form-item>
    </jm-form>
    <template #footer>
      <span>
        <jm-button
          size="small"
          @click="dialogVisible = false"
        >取消</jm-button
        >
        <jm-button
          size="small"
          type="primary"
          @click="create"
          :loading="loading"
        >确定</jm-button
        >
      </span>
    </template>
  </jm-dialog>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, ref, SetupContext } from 'vue';
import { IProjectGroupCreateFrom } from '@/model/modules/project-group';
import { createProjectGroup } from '@/api/project-group';

export default defineComponent({
  emits: ['completed'],
  setup(_, { emit }: SetupContext) {
    const { proxy } = getCurrentInstance() as any;
    const dialogVisible = ref<boolean>(true);
    const createFormRef = ref<any>(null);
    const createForm = ref<IProjectGroupCreateFrom>({
      name: '',
      isShow: true,
    });
    const editorRule = ref<object>({
      name: [{ required: true, message: '分组名称不能为空', trigger: 'blur' }],
    });
    const loading = ref<boolean>(false);
    const create = async () => {
      createFormRef.value.validate(async (valid: boolean) => {
        if (!valid) {
          return;
        }
        const { name, description, isShow } = createForm.value;
        loading.value = true;
        try {
          await createProjectGroup({
            name,
            description,
            isShow,
          });
          proxy.$success('项目分组创建成功');
          emit('completed');
          dialogVisible.value = false;
        } catch (err) {
          proxy.$throw(err, proxy);
        } finally {
          loading.value = false;
        }
      });
    };
    return {
      dialogVisible,
      createFormRef,
      createForm,
      editorRule,
      loading,
      create,
    };
  },
});
</script>

<style scoped lang="less">
.el-form-item {
  &.is-show {
    margin-bottom: 0px;
    margin-top: -10px;
  }
}

.creator-title {
  padding-left: 36px;
  background-image: url('@/assets/svgs/btn/edit.svg');
  background-repeat: no-repeat;
  background-position: left center;
}

.tips {
  color: #6b7b8d;
  margin-left: 15px;
}
</style>
