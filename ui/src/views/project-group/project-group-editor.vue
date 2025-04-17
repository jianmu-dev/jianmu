<template>
  <jm-dialog v-model="dialogVisible" width="776px" :destroy-on-close="true">
    <template #title>
      <div class="editor-title">{{ t('projectGroupEditor.title') }}</div>
    </template>
    <jm-form
      :model="editorForm"
      :rules="editorRule"
      ref="editorFormRef"
      @submit.prevent
    >
      <jm-form-item :label="t('projectGroupEditor.name')" label-position="top" prop="name">
        <jm-input
          :disabled="defaultGroup"
          v-model="editorForm.name"
          clearable
          :placeholder="t('projectGroupEditor.namePlaceholder')"
        />
      </jm-form-item>
      <jm-form-item
        :label="t('projectGroupEditor.isShow')"
        label-position="top"
        prop="isShow"
        class="is-show"
      >
        <jm-switch v-model="editorForm.isShow" active-color="#096DD9"/>
      </jm-form-item>
      <jm-form-item :label="t('projectGroupEditor.description')" label-position="top" prop="description">
        <jm-input
          type="textarea"
          v-model="editorForm.description"
          clearable
          maxlength="256"
          show-word-limit
          :placeholder="t('projectGroupEditor.descriptionPlaceholder')"
          :autosize="{ minRows: 6, maxRows: 10 }"
        />
        <div class="tips">{{ t('projectGroupEditor.descriptionTips') }}</div>
      </jm-form-item>
    </jm-form>
    <template #footer>
      <span>
        <jm-button
          size="small"
          @click="dialogVisible = false"
        >{{ t('projectGroupEditor.cancel') }}</jm-button
        >
        <jm-button
          size="small"
          type="primary"
          @click="save"
          :loading="loading"
        >{{ t('projectGroupEditor.save') }}</jm-button
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
import { useLocale } from '@/utils/i18n';

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
    const { t } = useLocale();
    const { proxy } = getCurrentInstance() as any;
    const dialogVisible = ref<boolean>(true);
    const editorFormRef = ref<any>(null);
    const editorForm = ref<IProjectGroupEditFrom>({
      name: '',
      isShow: true,
    });
    const editorRule = ref<object>({
      name: [{ required: true, message: t('projectGroupEditor.nameRequired'), trigger: 'blur' }],
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
          proxy.$success(t('projectGroupEditor.success'));
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
      t,
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
