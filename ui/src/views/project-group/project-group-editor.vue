<template>
  <jm-dialog v-model="dialogVisible" width="776px" :destroy-on-close="true">
    <template #title>
      <div class="editor-title">编辑项目分组</div>
    </template>
    <jm-form
      :model="editorForm"
      :rules="editorRule"
      ref="editorFormRef"
      @submit.prevent
    >
      <jm-form-item label="分组名称" label-position="top" prop="name">
        <jm-input
          :disabled="defaultGroup"
          v-model="editorForm.name"
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
        <jm-switch v-model="editorForm.isShow" active-color="#096DD9"/>
      </jm-form-item>
      <jm-form-item label="描述" label-position="top" prop="description">
        <jm-input
          type="textarea"
          v-model="editorForm.description"
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
          @click="save"
          :loading="loading"
        >保存</jm-button
        >
      </span>
    </template>
  </jm-dialog>
</template>

<script lang="ts">
import {
  defineComponent,
  getCurrentInstance,
  ref,
  SetupContext,
  onMounted,
} from 'vue';
import { IProjectGroupEditFrom } from '@/model/modules/project-group';
import { editProjectGroup } from '@/api/project-group';

export default defineComponent({
  emits: ['completed'],
  props: {
    name: {
      type: String,
      required: true,
    },
    id: { type: String, required: true },
    description: {
      type: String,
    },
    defaultGroup: {
      type: Boolean,
      required: true,
    },
    isShow: {
      type: Boolean,
      required: true,
    },
  },
  setup(props, { emit }: SetupContext) {
    const { proxy } = getCurrentInstance() as any;
    const dialogVisible = ref<boolean>(true);
    const editorFormRef = ref<any>(null);
    const editorForm = ref<IProjectGroupEditFrom>({
      name: '',
      isShow: true,
    });
    const editorRule = ref<object>({
      name: [{ required: true, message: '分组名称不能为空', trigger: 'blur' }],
    });
    const loading = ref<boolean>(false);
    onMounted(() => {
      editorForm.value.name = props.name;
      editorForm.value.description = props.description;
      editorForm.value.isShow = props.isShow;
    });
    const save = async () => {
      editorFormRef.value.validate(async (valid: boolean) => {
        if (!valid) {
          return;
        }
        const { name, description, isShow } = editorForm.value;
        try {
          loading.value = true;
          await editProjectGroup(props.id, {
            name,
            description,
            isShow,
          });
          proxy.$success('项目分组修改成功');
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
      editorFormRef,
      editorForm,
      editorRule,
      loading,
      save,
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

.editor-title {
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
