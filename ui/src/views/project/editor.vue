<template>
  <div class="project-editor">
    <!-- loading时的加载页面 -->
    <div class="loading-over" v-show="loading" v-loading="loading"></div>
    <div class="top">
      <div class="left-top-param">
        <router-link :to="{ name: 'index' }">
          <i class="jm-icon-button-left back"></i>
        </router-link>
        <div class="project-name">{{ projectName || '未命名项目' }}</div>
        <div class="selector" v-if="!isShowGrouping">
          <jm-select v-model="editorForm.projectGroupId">
            <jm-option v-for="item in projectGroupList" :key="item.id" :label="item.name" :value="item.id"></jm-option>
          </jm-select>
        </div>
        <div class="branch" v-else>
          <img src="~@/assets/svgs/index/branch.svg" alt="" />
          {{ currentBranch }}
        </div>
      </div>
      <div class="right-top-btn">
        <jm-button size="small" class="save-return" @click="save(true)" :loading="loading">保存并返回</jm-button>
        <jm-button type="primary" size="small" @click="save(false)" :loading="loading">保存</jm-button>
      </div>
    </div>
    <div :class="[entry ? 'dsl-editor-entry' : 'dsl-editor']">
      <jm-dsl-editor v-model:value="editorForm.dslText" />
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, inject, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { save } from '@/api/project';
import { ISaveForm } from '@/model/modules/project';
import { fetchProjectDetail, getProcessTemplate, listProjectGroup } from '@/api/view-no-auth';
import { IProcessTemplateVo, IProjectIdVo } from '@/api/dto/project';
import { IProjectGroupVo } from '@/api/dto/project-group';
import { useStore } from 'vuex';
import { IRootState } from '@/model';
import yaml from 'yaml';
import { IGitRepoBranchVo } from '@/api/dto/git-repo';
import { getBranches } from '@/api/git-repo';
import { pushTop } from '@/utils/jump-address';
import { DSL_CURRENT_VERSION } from '@/components/workflow/version';

export default defineComponent({
  props: {
    id: String,
    branch: String,
  },
  setup(props: any) {
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const store = useStore();
    const currentBranch = ref<string>(props.branch);
    const entry = true;
    // 是否展示分组
    const isShowGrouping = true;
    const reloadMain = inject('reloadMain') as () => void;
    const route = useRoute();
    // 项目分支信息
    const branches = ref<IGitRepoBranchVo[]>([]);
    const editMode = computed<boolean>(() => !!props.id);
    // 项目id
    const projectId = computed<string>(() => props.id);
    const editorForm = ref<ISaveForm>({
      id: projectId.value,
      branch: props.branch,
      dslText: `version: ${DSL_CURRENT_VERSION}\n`,
      projectGroupId: '',
    });
    let tempString: string;
    const projectName = computed<string>(() => {
      try {
        if (!editorForm.value.dslText.trim()) {
          return;
        }
        const name = yaml.parse(editorForm.value.dslText)['name'];
        tempString = name;
        return name;
      } catch (err) {
        return tempString;
      }
    });
    const projectGroupList = ref<IProjectGroupVo[]>([]);
    const form = ref<any>();
    const loading = ref<boolean>(false);
    const rootState = store.state as IRootState;
    const checkDsl = (dslText: string): boolean => {
      return !!yaml.parse(dslText)['raw-data'];
    };
    onMounted(async () => {
      // 获取分支信息（如果entry为true时，有必要获取分支信息）
      if (isShowGrouping) {
        branches.value = await getBranches();
        const flag = branches.value.some(item => {
          return item.branchName === currentBranch.value;
        });
        if (!flag) {
          currentBranch.value = branches.value.find(item => item.isDefault)!.branchName;
        }
        return;
      }
      // 请求项目组列表
      projectGroupList.value = await listProjectGroup();
      if (editMode.value) {
        return;
      }
      const defaultGroup = projectGroupList.value.find(item => item.isDefaultGroup);
      editorForm.value.projectGroupId = defaultGroup!.id;
    });
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

    if (editMode.value) {
      loading.value = !loading.value;
      fetchProjectDetail(props.id)
        .then(async ({ dslText, projectGroupId, branch }) => {
          currentBranch.value = branch;
          if (checkDsl(dslText)) {
            if (window.top !== window) {
              pushTop(`/full/project/pipeline-editor/${props.id}`);
              return;
            }
            const payload = {
              projectGroupId,
              branch,
              dslText,
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
      entry,
      currentBranch,
      projectName,
      isShowGrouping,
      projectGroupList,
      form,
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
        if (editorForm.value.dslText === '') {
          proxy.$error('DSL不能为空');
          return;
        }
        // 开启loading
        loading.value = true;
        const payload = { ...editorForm.value };
        if (!payload.projectGroupId) {
          // 如果没有设置项目组id，调用接口时不用传递，后端处理。
          Reflect.deleteProperty(payload, 'projectGroupId');
        }
        save(payload)
          .then(async ({ id }: IProjectIdVo) => {
            editorForm.value.id = id;
            // 关闭loading
            loading.value = false;

            proxy.$success(editMode.value ? '保存成功' : '新增成功');

            if (returnFlag) {
              close();

              return;
            }

            if (!editMode.value) {
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

  .top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background-color: transparent;
    padding: 0 0 20px 0;

    .left-top-param {
      color: #082340;
      font-weight: 400;
      display: flex;
      font-size: 16px;
      align-items: center;

      .back {
        margin-right: 15px;
        color: #6b7b8d;

        &:hover {
          background-color: #eff7ff;
          color: #096dd9;
        }
      }

      .selector,
      .branch {
        position: relative;
        display: flex;
        align-items: center;

        &:before {
          content: '';
          display: inline-block;
          margin: 0 20px;
          width: 2px;
          height: 16px;
          background-color: #cdd1e3;
        }

        ::v-deep(.el-select) {
          .select-trigger {
            .el-input {
              width: 96px;

              .el-input__inner {
                border: none;
                padding: 0;
                color: #082340;
                font-weight: 400;
                font-size: 16px;
                background-color: transparent;
              }

              .el-select__caret {
                color: #666666;
              }
            }
          }
        }
      }

      .branch {
        img {
          width: 16px;
          height: 16px;
          margin-right: 6px;
        }
      }
    }

    .right-top-btn {
      display: flex;

      .el-button {
        margin: 0;

        &:nth-child(1) {
          margin-right: 20px;
        }

        &:nth-child(2) {
          &:hover {
            background-color: #3293fd;
          }
        }

        &.el-button--small {
          min-height: 36px;
          padding: 8px 24px;
          box-shadow: none;
        }

        &.save-return {
          background: #f5f5f5;
          border-radius: 2px;
          color: #082340;
          border: none;
          box-shadow: none;

          &:hover {
            background-color: #eff7ff;
            color: #096dd9;
          }
        }
      }
    }
  }

  .dsl-editor,
  .dsl-editor2 {
    padding: 20px;
    border-radius: 2px;
    border: 1px solid #eaeef2;
    background-color: #fff;
    box-sizing: border-box;

    > div {
      z-index: 1;
    }
  }

  .dsl-editor {
    height: calc(100vh - 170px);
  }

  .dsl-editor-entry {
    height: calc(100vh - 100px);
  }
}
</style>
