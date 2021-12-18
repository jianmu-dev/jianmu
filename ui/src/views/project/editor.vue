<template>
  <div class="project-editor" v-loading="loading">
    <div class="right-top-btn">
      <jm-button class="jm-icon-button-cancel" size="small" @click="close"
        >取消</jm-button
      >
      <jm-button
        v-if="source === 'processTemplates'"
        type="primary"
        class="jm-icon-button-previous"
        size="small"
        @click="previousStep"
        >上一步
      </jm-button>

      <jm-button
        type="primary"
        class="jm-icon-button-preserve"
        size="small"
        @click="save"
        :loading="loading"
        >保存
      </jm-button>
    </div>
    <div class="form">
      <jm-form :model="editorForm" :rules="rules" ref="form">
        <jm-form-item label="选择项目组" prop="projectGroupId">
          <jm-select
            v-model="editorForm.projectGroupId"
            placeholder="请选择项目组"
          >
            <jm-option
              v-for="item in projectGroupList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
            </jm-option>
          </jm-select>
        </jm-form-item>
      </jm-form>
    </div>
    <jm-tabs v-model="activatedTab" class="tabs">
      <jm-tab-pane name="dsl" lazy>
        <template #label><i class="jm-icon-tab-dsl"></i> DSL模式</template>
        <div class="dsl-editor">
          <jm-dsl-editor v-model:value="editorForm.dslText" />
        </div>
      </jm-tab-pane>
    </jm-tabs>
  </div>
</template>

<script lang="ts">
import {
  defineComponent,
  getCurrentInstance,
  inject,
  onMounted,
  ref,
} from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { save } from '@/api/project';
import { ISaveForm } from '@/model/modules/project';
import {
  fetchProjectDetail,
  getProcessTemplate,
  listProjectGroup,
} from '@/api/view-no-auth';
import { IProcessTemplateVo, IProjectIdVo } from '@/api/dto/project';
import { IProjectGroupVo } from '@/api/dto/project-group';
import { useStore } from 'vuex';
import { IRootState } from '@/model';

export default defineComponent({
  props: {
    id: String,
  },
  setup(props: any) {
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const reloadMain = inject('reloadMain') as () => void;
    const route = useRoute();
    const editMode = !!props.id;
    const editorForm = ref<ISaveForm>({
      id: props.id,
      dslText: '',
      projectGroupId: '',
    });
    const projectGroupList = ref<IProjectGroupVo[]>([]);
    const form = ref<any>();
    const loading = ref<boolean>(false);
    const store = useStore();
    const rootState = store.state as IRootState;
    onMounted(async () => {
      // 请求项目组列表
      projectGroupList.value = await listProjectGroup();
    });
    const rules = {
      projectGroupId: [
        { required: true, message: '请选择项目组', trigger: 'change' },
      ],
    };
    if (route.query.templateId) {
      getProcessTemplate(route.query.templateId as unknown as number)
        .then((res: IProcessTemplateVo) => {
          let dsl = res.dsl;
          if (route.query.processTemplatesName !== res.name) {
            let name = `name: ${res.name}`;
            editorForm.value.dslText = dsl.replace(
              name,
              `name: ${route.query.processTemplatesName}`,
            );
          } else {
            editorForm.value.dslText = dsl;
          }
        })
        .catch((err: Error) => {
          console.warn(err.message);
        });
    } else if (route.query.source === 'processTemplates') {
      editorForm.value.dslText = `name: "${
        route.query.processTemplatesName || ''
      }"\n\nworkflow:\n`;
    }

    if (editMode) {
      loading.value = !loading.value;

      fetchProjectDetail(props.id)
        .then(({ dslText, projectGroupId }) => {
          editorForm.value.dslText = dslText;
          // 回显项目组
          editorForm.value.projectGroupId = projectGroupId;
          loading.value = !loading.value;
        })
        .catch((err: Error) => {
          loading.value = !loading.value;

          proxy.$throw(err, proxy);
        });
    }

    return {
      projectGroupList,
      form,
      rules,
      editorForm,
      loading,
      activatedTab: ref<string>('dsl'),
      source: route.query.source,
      previousStep: () => {
        router.push({
          name: 'process-template',
        });
      },
      save: () => {
        form.value.validate((valid: boolean) => {
          if (!valid) {
            return false;
          }
          if (editorForm.value.dslText === '') {
            proxy.$error('DSL不能为空');
            return;
          }
          // 开启loading
          loading.value = true;

          save({ ...editorForm.value })
            .then(async ({ id }: IProjectIdVo) => {
              // 关闭loading
              loading.value = false;

              proxy.$success(editMode ? '编辑成功' : '新增成功');

              if (!editMode) {
                await router.push({ name: 'update-project', params: { id } });
                reloadMain();
              }
            })
            .catch((err: Error) => {
              // 关闭loading
              loading.value = false;

              proxy.$throw(err, proxy);
            });
        });
      },
      close: () => {
        if (rootState.fromRouteFullPath &&
          !rootState.fromRouteFullPath.startsWith('/project/editor')) {
          router.push(rootState.fromRouteFullPath);
          return;
        }
        router.push({ name: 'index' });
      },
    };
  },
});
</script>

<style scoped lang="less">
.project-editor {
  font-size: 14px;
  color: #333333;
  margin-bottom: 25px;
  .form {
    padding: 24px;
    padding-top: 10px;
    background-color: #fff;
    margin-bottom: 20px;
    .el-form-item {
      &.is-required {
        display: flex;
        flex-direction: column;
        ::v-deep(.el-form-item__label) {
          text-align: left;
        }
      }
      margin-bottom: 0px;
      .el-form-item__content {
        .el-select {
          width: 50%;
        }
      }
    }
  }

  .right-top-btn {
    position: fixed;
    right: 20px;
    top: 78px;

    .jm-icon-button-back::before,
    .jm-icon-button-cancel::before,
    .jm-icon-button-preserve::before {
      font-weight: bold;
    }

    a {
      margin-right: 10px;
    }
  }

  .tabs {
    background-color: #ffffff;
    border-radius: 4px 4px 0 0;

    .dsl-editor {
      // height: calc(100vh - 215px);
      height: calc(100vh - 350px);
      > div {
        z-index: 1;
      }
    }
  }
}
</style>
