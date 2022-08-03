<template>
  <div class="integration-index">
    <div class="top">
      <!-- 分类导航 -->
      <div class="classification-tabs">
        <div :class="['tab-item',currentTab===index?'is-active':'']" v-for="(item,index) in data" :key="index"
             @click="currentTab=index">{{
            item.label
          }}・{{ item.counter }}
        </div>
      </div>
      <div class="selector">
        <div class="all-branch item">
          <jm-select v-model="branch" popper-class="no-arrow">
            <jm-option label="全部分支" value="default">全部分支</jm-option>
            <jm-option :label="b.branchName" v-for='(b,i) in branches' :key="i" :value="b.branchName">{{
                b.branchName
              }}
            </jm-option>
          </jm-select>
        </div>
        <div class="divider"></div>
        <div class="sort item">
          <jm-select v-model="sortType" popper-class="no-arrow">
            <jm-option label="最近执行" :value="GitRepoEnum.LAST_EXECUTION_TIME">最近执行</jm-option>
            <jm-option label="最近修改" :value="GitRepoEnum.LAST_MODIFIED_TIME">最近修改</jm-option>
          </jm-select>
        </div>
      </div>
    </div>
    <div class="assembly-line-wrapper">
      <div class="loading" v-loading="loading" v-if="loading"></div>
      <div class="empty-project" v-else-if="initProjects.length===0">
        <span class="tip">
          点击下方模块快速创建
        </span>
        <div class="create-project">
          <div class="item graph" @click="createGraph">
            <img src="~@/assets/svgs/index/graph-project-btn-square.svg" alt="">
            <span>图形流水线</span>
          </div>
          <div class="item code" @click="createCode">
            <img src="~@/assets/svgs/index/code-project-btn-square.svg" alt="">
            <span>代码流水线</span>
          </div>
        </div>
      </div>
      <div class="empty-assembly-line" v-else-if="data[currentTab].projects.length===0">
        <jm-empty description="暂无流水线" :image-size="98" class="empty"/>
      </div>
      <project-item
        v-else
        v-for="project of data[currentTab].projects"
        :concurrent="project.concurrent"
        :key="project.id"
        :project="project"
        :no-disable="true"
        @select-project-id="()=>{previewId=project.id;dslDialogFlag=true;}"
        @running="handleProjectRunning"
        @synchronized="handleProjectSynchronized"
        @deleted="handleProjectDeleted"
      />
    </div>
    <!-- 选择分支的弹框 -->
    <jm-dialog
      custom-class="select-branch-dialog"
      :model-value="dialogVisible"
      @close="close"
      width="460px">
      <template #title>
        <div class="editor-title">请选择流水线保存的分支</div>
      </template>
      <jm-form ref="editorFormRef" @submit.prevent>
        <jm-form-item>
          <jm-select v-model="selectBranch" style="width: 100%" popper-class="no-arrow">
            <jm-option v-for="(b,i) in branches" :key="i" :label="b.branchName" :value="b.branchName">
              {{ b.branchName }}
            </jm-option>
          </jm-select>
        </jm-form-item>
      </jm-form>
      <template #footer>
        <span class="dialog-footer">
          <jm-button size="small" @click="close">取消</jm-button>
          <!-- 选择了分支后，确认按钮可点 -->
          <jm-button size="small" type="primary" :loading="loading" @click="submit"
                     :disabled="!selectBranch">确定</jm-button>
        </span>
      </template>
    </jm-dialog>
    <!--预览弹窗 -->
    <project-preview-dialog
      v-if="dslDialogFlag"
      :project-id="previewId"
      :projects="data[currentTab].projects"
      @close="()=>{dslDialogFlag=false;previewId=''}"
    />
  </div>
</template>
<script lang="ts">
import { computed, defineComponent, getCurrentInstance, onBeforeUnmount, onMounted, onUpdated, ref } from 'vue';
import { getAssemblyLineList, getBranches } from '@/api/git-repo';
import { GitRepoEnum, ProjectStatusEnum } from '@/api/dto/enumeration';
import ProjectItem from '@/views/common/project-item.vue';
import { IProjectVo } from '@/api/dto/project';
import { IGitRepoBranchVo } from '@/api/dto/git-repo';
import { useRouter } from 'vue-router';
import { pushTop } from '@/utils/push-top';
import ProjectPreviewDialog from '@/views/common/project-preview-dialog.vue';

const MAX_AUTO_REFRESHING_OF_NO_RUNNING_COUNT = 5;
export default defineComponent({
  emits: ['update:model-value', 'update:create-type'],
  props: {
    // 搜索流水线名称
    keyword: {
      type: String,
      required: true,
    },
    // 控制dialog显隐
    modelValue: {
      type: Boolean,
      required: true,
    },
    // 创建流水线类型
    createType: {
      type: Number,
      required: true,
    },
  },
  components: {
    ProjectItem,
    ProjectPreviewDialog,
  },
  setup(props, { emit }) {
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const loading = ref<boolean>(false);
    const dialogVisible = computed<boolean>(() => props.modelValue);
    // createProjectType为 0创建代码流水线,1创建创建图形流水线
    const createProjectType = computed<number>(() => props.createType);
    const previewId = ref<string>('');
    const dslDialogFlag = ref<boolean>(false);
    const key = ref<string>(props.keyword);
    // 流水线状态类型tab索引
    const currentTab = ref<number>(0);
    // 全部项目
    const initProjects = ref<IProjectVo[]>([]);
    // 分支下拉框选择的值
    const branch = ref<string>('default');
    // 所有分支
    const branches = ref<IGitRepoBranchVo[]>([]);
    // 弹窗选择的分支
    const selectBranch = ref<string>('');
    // 项目排序类型
    const sortType = ref<string>(GitRepoEnum.LAST_EXECUTION_TIME);
    const filteredProjects = computed<IProjectVo[]>(() => {
      let result = [...initProjects.value].sort((pre, next) => {
        if (sortType.value === GitRepoEnum.LAST_EXECUTION_TIME) {
          // 根据时间戳降序排列
          return next.latestTime ? Date.parse(next.latestTime) : 0 -
            pre.latestTime ? Date.parse(pre.latestTime) : 0;
        } else {
          return Date.parse(next.lastModifiedTime as string) - Date.parse(pre.lastModifiedTime as string);
        }
      });
      if (branch.value !== 'default') {
        result = result.filter(({ branch: b }) => {
          return b === branch.value;
        });
      }
      if (key.value) {
        result = result.filter(({ name }) => name.includes(key.value));
      }
      return result;
    },
    );
    const data = computed<Array<{ label: string, projects: IProjectVo[], counter: number }>>(() => [
      {
        label: '全部',
        projects: filteredProjects.value,
        counter: filteredProjects.value.length,
      },
      {
        label: '未启动',
        projects: filteredProjects.value.filter(({ status }) => status === ProjectStatusEnum.INIT),
        counter: filteredProjects.value.filter(({ status }) => status === ProjectStatusEnum.INIT).length,
      },
      {
        label: '执行中',
        projects: filteredProjects.value.filter(({ status }) => status === ProjectStatusEnum.RUNNING),
        counter: filteredProjects.value.filter(({ status }) => status === ProjectStatusEnum.RUNNING).length,
      },
      {
        label: '挂起',
        projects: filteredProjects.value.filter(({ status }) => status === ProjectStatusEnum.SUSPENDED),
        counter: filteredProjects.value.filter(({ status }) => status === ProjectStatusEnum.SUSPENDED).length,
      },
      {
        label: '失败',
        projects: filteredProjects.value.filter(({ status }) => status === ProjectStatusEnum.FAILED),
        counter: filteredProjects.value.filter(({ status }) => status === ProjectStatusEnum.FAILED).length,
      },
      {
        label: '成功',
        projects: filteredProjects.value.filter(({ status }) => status === ProjectStatusEnum.SUCCEEDED),
        counter: filteredProjects.value.filter(({ status }) => status === ProjectStatusEnum.SUCCEEDED).length,
      },
    ]);
    // 对请求的所有项目,项目分支数据进行处理
    const handleData = async (init?: boolean) => {
      initProjects.value = await getAssemblyLineList({});
      // 保证只有在mounted里面请求分支数据
      init && (branches.value = await getBranches()) && (selectBranch.value = branches.value.find(item => !!item.isDefault)!.branchName);
    };
    // 请求数据
    const loadData = async (init?: boolean) => {
      try {
        loading.value = true;
        await handleData(init);
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        loading.value = false;
      }
    };

    let autoRefreshingInterval: any;
    const autoRefreshingOfNoRunningCount = ref<number>(0);
    const refreshHandler = () => {
      autoRefreshingInterval = setInterval(async () => {
        if (
          !initProjects.value?.find(
            item => item.status === ProjectStatusEnum.RUNNING,
          )
        ) {
          // 不存在running场景
          if (
            autoRefreshingOfNoRunningCount.value <
            MAX_AUTO_REFRESHING_OF_NO_RUNNING_COUNT
          ) {
            autoRefreshingOfNoRunningCount.value++;
            return;
          } else {
            console.debug('刷新项目列表，检查是否存在running中的项目');
          }
        } else {
          console.debug('存在running中的项目，刷新项目列表');
        }
        autoRefreshingOfNoRunningCount.value = 0;
        await handleData();
      }, 3000);
    };
    onMounted(async () => {
      await loadData(true);
      refreshHandler();
    });
    onUpdated(async () => {
      // 输入框内容发生变化重新查询
      if (key.value !== props.keyword) {
        key.value = props.keyword;
        // 状态初始化
        currentTab.value = 0;
        branch.value = 'default';
        sortType.value = GitRepoEnum.LAST_EXECUTION_TIME;
        return;
      }
    });
    onBeforeUnmount(() => {
      clearInterval(autoRefreshingInterval);
    });
    return {
      previewId,
      dslDialogFlag,
      initProjects,
      loading,
      dialogVisible,
      currentTab,
      data,
      branch,
      selectBranch,
      branches,
      sortType,
      GitRepoEnum,
      handleProjectRunning: (id: string) => {
        const index = initProjects.value.findIndex(item => item.id === id);
        initProjects.value[index] = {
          ...initProjects.value[index],
          startTime: new Date().toISOString(),
          status: ProjectStatusEnum.RUNNING,
        };
      },
      handleProjectSynchronized: () => {
        // 刷新项目列表，保留查询状态
        loadData();
      },
      handleProjectDeleted: (id: string) => {
        const index = initProjects.value.findIndex(item => item.id === id);
        initProjects.value.splice(index, 1);
      },
      close() {
        emit('update:model-value', false);
      },
      async submit() {
        if (window.top !== window && createProjectType.value) {
          pushTop(`/full/project/pipeline-editor?branch=${selectBranch.value}`);
          return;
        }
        await router.push({
          name: createProjectType.value ? 'create-pipeline' : 'create-project',
          query: {
            branch: selectBranch.value,
          },
        });
        emit('update:model-value', false);
      },
      createGraph() {
        emit('update:model-value', true);
        emit('update:create-type', 1);
      },
      createCode() {
        emit('update:model-value', true);
        emit('update:create-type', 0);
      },
    };
  },
});
</script>
<style lang="less" scoped>
.integration-index {

  .top {
    z-index: 1;
    background-color: #FFFFFF;
    position: sticky;
    top: 0;
    box-sizing: border-box;
    padding: 20px 0;
    display: flex;
    justify-content: space-between;
    font-size: 14px;

    .classification-tabs, .dropdown {
      font-weight: 400;
      display: flex;
      align-items: center;
    }

    .classification-tabs {
      //color: #666666;
      color: #082340;

      .tab-item {
        cursor: pointer;
        box-sizing: border-box;
        padding: 5px 15px;

        &.is-active {
          font-weight: bold;
          background-color: #EBF4FF;
          border-radius: 15px;
          //color: #466AFF;
          color: #096DD9;
        }
      }
    }

    .selector {
      display: flex;
      align-items: center;

      ::v-deep(.el-select) {
        .select-trigger {
          .el-input {
            width: 86px;

            .el-input__inner {
              border: none;
              padding: 0;
              color: #082340;
              font-weight: 400;
              background-color: transparent;
            }

            .el-select__caret {
              color: #666666;
            }
          }
        }
      }

      .divider {
        margin: 0 14px 0 4px;
        width: 1px;
        height: 14px;
        background-color: #E7ECF1;

      }

      .item {
        display: flex;
        align-items: center;
        color: #082340;
        cursor: pointer;
        user-select: none;

        .icon {
          display: inline-block;
          transform: rotate(90deg) scale(.7);
        }
      }
    }
  }

  .assembly-line-wrapper {

    display: flex;
    flex-wrap: wrap;
    min-height: 300px;

    .loading {
      position: fixed;
      top: 158px;
      width: 1200px;
      height: 300px;
    }

    ::v-deep(.project-item) {
      box-sizing: border-box;
      min-width: 277px;
      box-shadow: none;
      border: 1px solid #E7ECF1;
      border-bottom-left-radius: 4px;
      border-bottom-right-radius: 4px;
      margin: 0.5%;
      height: 180px;

      &:hover {
        box-shadow: 0 4px 6px 1px #E1EBF5;
        border: 1px solid transparent;
      }
    }

    .empty-project {
      margin: 110px auto 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      font-size: 14px;
      color: #082340;
      font-weight: 400;

      .create-project {
        display: flex;

        .item {
          margin-top: 22px;
          cursor: pointer;
          display: flex;
          flex-direction: column;
          align-items: center;
          font-size: 12px;

          &.graph {
            margin-right: 32px;
          }

          img {
            width: 56px;
            height: 56px;
            margin-bottom: 6px;
          }

          &:hover {
            //color: #466AFF;
            color: #096DD9;
          }
        }
      }
    }

    .empty-assembly-line {
      width: 100%;

      .empty {
        margin: 110px auto 0;
      }
    }
  }

  ::v-deep(.el-dialog) {
    .el-dialog__footer {
      background-color: #fff;

      .el-button--small {
        padding: 11px 25px;
        border: none;
        box-shadow: none;

        &.el-button--default {
          background-color: #F5F5F5;

          &:hover {
            //background-color: #EBEFFF;
            background-color: #EFF7FF;

            //color: #466AFF;
            color: #096DD9;
          }
        }

        &.el-button--primary {
          //background-color: #466AFF;
          background-color: #096DD9;

          &:hover {
            //background-color: #8199FE;
            background-color: #3293FD;
            color: #FFFFFF;
          }
        }
      }
    }
  }
}
</style>
