<template>
  <jm-dialog width="667px" destroy-on-close @close="cancel">
    <template #title>
      <div class="title-container"><i class="jm-icon-workflow-edit"></i>编辑项目信息</div>
    </template>
    <div class="jm-workflow-editor-project-panel">
      <jm-form ref="editProjectInfoRef" :model="projectInfoForm" :rules="rules" @submit.prevent>
        <jm-form-item label="项目名称" prop="name" class="name-item">
          <jm-input
            v-model="projectInfoForm.name"
            :maxlength="45"
            placeholder="请输入项目名称"
            :show-word-limit="true"
          />
        </jm-form-item>
        <jm-form-item label="项目分组" class="group-item" prop="groupId">
          <jm-select v-model="projectInfoForm.groupId" placeholder="请选择项目分组" v-loading="loading">
            <jm-option v-for="item in projectGroupList" :key="item.id" :label="item.name" :value="item.id" />
          </jm-select>
        </jm-form-item>
        <jm-form-item label="项目描述" class="description-item">
          <jm-input
            type="textarea"
            v-model="projectInfoForm.description"
            :maxlength="255"
            placeholder="请输入项目描述"
            :show-word-limit="true"
          />
        </jm-form-item>
        <div class="btn-container">
          <jm-button @click="cancel" class="cancel">取消</jm-button>
          <jm-button type="primary" @click="save(editProjectInfoRef)">确定</jm-button>
        </div>
      </jm-form>
    </div>
  </jm-dialog>
</template>

<script lang="ts">
import { defineComponent, onMounted, PropType, ref } from 'vue';
import { IWorkflow } from '../../model/data/common';
import { IProjectGroupVo } from '@/api/dto/project-group';
import { listProjectGroup } from '@/api/view-no-auth';

export interface IProjectInfo {
  name: string;
  description: string;
  groupId: string;
}

export default defineComponent({
  props: {
    workflowData: {
      type: Object as PropType<IWorkflow>,
      required: true,
    },
  },
  emits: ['update:model-value'],
  setup(props, { emit }) {
    const loading = ref<boolean>(true);
    const workflowForm = ref<IWorkflow>(props.workflowData);
    const projectInfoForm = ref<IProjectInfo>({
      name: props.workflowData.name,
      description: props.workflowData.description || '',
      groupId: props.workflowData.groupId,
    });

    const editProjectInfoRef = ref<HTMLFormElement>();
    // 分组列表
    const projectGroupList = ref<IProjectGroupVo[]>([]);

    onMounted(async () => {
      projectGroupList.value = await listProjectGroup();
      loading.value = false;
    });

    return {
      editProjectInfoRef,
      projectGroupList,
      projectInfoForm,
      loading,
      save: () => {
        editProjectInfoRef.value?.validate((valid: boolean) => {
          if (!valid) {
            return false;
          }
          workflowForm.value.name = projectInfoForm.value.name;
          workflowForm.value.groupId = projectInfoForm.value.groupId;
          workflowForm.value.description = projectInfoForm.value.description;
          emit('update:model-value', false);
        });
      },
      cancel: () => {
        emit('update:model-value', false);
      },
      rules: {
        name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
        groupId: [{ required: true, message: '请选择项目分组', trigger: 'change' }],
      },
    };
  },
});
</script>

<style scoped lang="less">
.el-dialog {
  .title-container {
    font-size: 16px;

    .jm-icon-workflow-edit {
      margin-right: 10px;
    }
  }

  .jm-workflow-editor-project-panel {
    .group-item,
    .description-item {
      padding-top: 10px;
    }

    .description-item {
      padding-bottom: 30px;
    }

    .btn-container {
      display: flex;
      justify-content: flex-end;

      .cancel {
        color: #082340;
        background: #f5f5f5;
        border-radius: 2px;
        border: none;
        box-shadow: none;

        &:hover {
          background: #d9d9d9;
        }
      }
    }
  }
}
</style>
