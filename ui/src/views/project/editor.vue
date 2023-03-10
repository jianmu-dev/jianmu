<template>
  <div class="project-editor">
    <!-- loading时的加载页面 -->
    <div class="loading-over" v-show="loading" v-loading="loading"></div>
    <div class="right-top-btn">
      <jm-button class="jm-icon-button-cancel" size="small" @click="close">取消</jm-button>
      <jm-button
        v-if="source === 'processTemplates'"
        type="primary"
        class="jm-icon-button-previous"
        size="small"
        @click="previousStep"
        >上一步
      </jm-button>
      <jm-button class="jm-icon-button-preserve" size="small" @click="save(true)" :loading="loading"
        >保存并返回
      </jm-button>
      <jm-button type="primary" class="jm-icon-button-preserve" size="small" @click="save(false)" :loading="loading"
        >保存
      </jm-button>
    </div>
    <div class="form">
      <jm-form :model="editorForm" :rules="rules" ref="form">
        <jm-form-item label="选择项目组" prop="projectGroupId">
          <jm-select v-model="editorForm.projectGroupId" placeholder="请选择项目组">
            <jm-option v-for="item in projectGroupList" :key="item.id" :label="item.name" :value="item.id"></jm-option>
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
import { defineComponent, getCurrentInstance, inject, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { save } from '@/api/project';
import { ISaveForm } from '@/model/modules/project';
import { fetchProjectDetail, getProcessTemplate, listProjectGroup } from '@/api/view-no-auth';
import { IProcessTemplateVo, IProjectIdVo } from '@/api/dto/project';
import { IProjectGroupVo } from '@/api/dto/project-group';
import { useStore } from 'vuex';
import { IRootState } from '@/model';
import { namespace } from '@/store/modules/session';
import yaml from 'yaml';
import LoginVerify from '@/views/login/dialog.vue';

export default defineComponent({
  props: {
    id: String,
  },
  setup(props: any) {
    const { proxy, appContext } = getCurrentInstance() as any;
    const router = useRouter();
    const store = useStore();
    const sessionState = { ...store.state[namespace] };
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
    const rootState = store.state as IRootState;
    const checkDsl = (dslText: string): boolean => {
      return !!yaml.parse(dslText)['raw-data'];
    };
    onMounted(async () => {
      // 请求项目组列表
      projectGroupList.value = await listProjectGroup();
      if (editMode) {
        return;
      }
      const defaultGroup = projectGroupList.value.find(item => item.isDefaultGroup);
      editorForm.value.projectGroupId = defaultGroup!.id;
    });

    const rules = {
      projectGroupId: [{ required: true, message: '请选择项目组', trigger: 'change' }],
    };
    // 没有登录时做的弹框判断
    if (!sessionState.session) {
      store.dispatch(`${namespace}/openAuthDialog`, { appContext, LoginVerify });
    }
    if (route.query.templateId) {
      getProcessTemplate(route.query.templateId as unknown as number)
        .then((res: IProcessTemplateVo) => {
          const dsl = res.dsl;
          if (route.query.processTemplatesName !== res.name) {
            const name = `name: ${res.name}`;
            editorForm.value.dslText = dsl.replace(name, `name: ${route.query.processTemplatesName}`);
          } else {
            editorForm.value.dslText = dsl;
          }
        })
        .catch((err: Error) => {
          console.warn(err.message);
        });
    } else if (route.query.source === 'processTemplates') {
      editorForm.value.dslText = `name: "${route.query.processTemplatesName || ''}"\n\nworkflow:\n`;
    }

    if (editMode) {
      loading.value = !loading.value;
      fetchProjectDetail(props.id)
        .then(async ({ dslText, projectGroupId }) => {
          if (checkDsl(dslText)) {
            const rawData = yaml.parse(dslText)['raw-data'];
            const { name, global, description } = yaml.parse(dslText);
            const payload = {
              name,
              groupId: projectGroupId,
              description,
              global: {
                concurrent: global ? global.concurrent : 1,
                caches: global.cache ? global.cache : undefined,
              },
              data: rawData,
            };
            await router.replace({
              name: 'update-pipeline',
              params: {
                id: props.id,
                payload: JSON.stringify(payload),
              },
            });
            return;
          }
          // 判断dslText是否有raw-data后再回显数据
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

    const close = () => {
      if (rootState.fromRoute.fullPath.startsWith('/project/editor')) {
        router.push({ name: 'index' });
        return;
      }
      router.push(rootState.fromRoute.fullPath);
    };

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
      save: (returnFlag: boolean) => {
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

              proxy.$success(editMode ? '保存成功' : '新增成功');

              if (returnFlag) {
                close();

                return;
              }

              if (!editMode) {
                await router.push({ name: 'update-project', params: { id } });
                reloadMain();

                return;
              }
            })
            .catch((err: Error) => {
              // 关闭loading
              loading.value = false;

              proxy.$throw(err, proxy);
            });
        });
      },
      close,
    };
  },
});
</script>

<style scoped lang="less">
.project-editor {
  font-size: 14px;
  color: #333333;
  margin-bottom: 25px;

  .loading-over {
    position: fixed;
    top: 0;
    left: 0;
    z-index: 20;
    background-color: #ffffff;
    width: 100vw;
    height: 100vh;
  }

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
