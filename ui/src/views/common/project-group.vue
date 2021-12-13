<template>
  <div class="project-group" v-loading="loading">
    <div v-if="!pageable" class="name">
      <div class="group-name">
        <span>{{ projectGroup?.name }}</span>
        <span class="desc">（共有 {{ projectPage.total }} 个项目）</span>
      </div>
      <div
        class="more"
        v-if="projectPage.total > 9"
        @click="more(projectGroup)"
      >
        查看更多
      </div>
    </div>
    <div class="projects">
      <jm-empty v-if="projects.length === 0" />
      <project-item
        v-for="project of projects"
        :key="project.id"
        :project="project"
        @running="handleProjectRunning"
        @synchronized="handleProjectSynchronized"
        @deleted="handleProjectDeleted"
      />
    </div>
  </div>
</template>

<script lang="ts">
import {
  computed,
  defineComponent,
  getCurrentInstance,
  onBeforeMount,
  onBeforeUnmount,
  onUpdated,
  PropType,
  nextTick,
  ref,
} from 'vue';
import { IProjectVo } from '@/api/dto/project';
import { IProjectGroupVo } from '@/api/dto/project-group';
import { queryProject } from '@/api/view-no-auth';
import { IQueryForm } from '@/model/modules/project';
import { ProjectStatusEnum } from '@/api/dto/enumeration';
import ProjectItem from '@/views/common/project-item.vue';
import { HttpError, TimeoutError } from '@/utils/rest/error';
import { IPageVo } from '@/api/dto/common';

const MAX_AUTO_REFRESHING_OF_NO_RUNNING_COUNT = 5;

export default defineComponent({
  components: { ProjectItem },
  props: {
    // 项目组
    projectGroup: {
      type: Object as PropType<IProjectGroupVo>,
    },
    // 是否分页
    pageable: {
      type: Boolean,
      required: true,
    },
    // 查询关键字
    name: {
      type: String,
    },
  },
  setup(props: any) {
    const { proxy } = getCurrentInstance() as any;
    const loading = ref<boolean>(false);
    const projectPage = ref<IPageVo<IProjectVo>>({
      total: 0,
      pages: 0,
      list: [],
    });
    const projects = computed<IProjectVo[]>(() => projectPage.value.list);
    const queryForm = ref<IQueryForm>({
      pageNum: 1,
      // TODO 待完善分页
      pageSize: props.pageable ? 10 * 1000 : 10,
      projectGroupId: props.projectGroup?.id,
      name: props.name,
    });

    const autoRefreshingOfNoRunningCount = ref<number>(0);

    console.log('开启自动刷新项目列表');
    const autoRefreshingInterval = setInterval(async () => {
      if (
        !projects.value.find(item => item.status === ProjectStatusEnum.RUNNING)
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

      try {
        projectPage.value = await queryProject({ ...queryForm.value });
      } catch (err) {
        if (err instanceof TimeoutError) {
          // 忽略超时错误
          console.warn(err.message);
        } else if (err instanceof HttpError) {
          const { response } = err as HttpError;

          if (response && response.status !== 502) {
            throw err;
          }

          // 忽略错误
          console.warn(err.message);
        }
      }
    }, 3000);

    const loadProject = async () => {
      try {
        loading.value = true;
        projectPage.value = await queryProject({ ...queryForm.value });
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        loading.value = false;
      }
    };
    // 初始化项目列表
    onBeforeMount(async () => {
      await nextTick(() => {
        queryForm.value.name = props.name;
      });
      await loadProject();
    });
    onUpdated(async () => {
      if (
        queryForm.value.name === props.name &&
        queryForm.value.projectGroupId === props.projectGroup?.id
      ) {
        return;
      }
      queryForm.value.name = props.name;
      queryForm.value.projectGroupId = props.projectGroup?.id;
      await nextTick(() => {
        loadProject();
      });
    });

    onBeforeUnmount(() => {
      console.log('终止自动刷新项目列表');
      clearInterval(autoRefreshingInterval);
    });

    return {
      loading,
      ProjectStatusEnum,
      projectPage,
      projects,
      queryForm,
      handleProjectRunning: (id: string) => {
        const index = projects.value.findIndex(item => item.id === id);
        projects.value[index] = {
          ...projects.value[index],
          status: ProjectStatusEnum.RUNNING,
        };
      },
      handleProjectSynchronized: () => {
        // 刷新项目列表，保留查询状态
        loadProject();
      },
      handleProjectDeleted: (id: string) => {
        const index = projects.value.findIndex(item => item.id === id);
        projects.value.splice(index, 1);
      },
      // 显示更多
      more: (group: any) => {
        // console.log('显示更多组id', group.id);
        // 获取当前组id
        // queryForm.value.projectGroupId = group.id;
        // 重新请求
        // loadProject();
        // 将当前组名或组id传递给search-project.vue渲染到下拉框
      },
    };
  },
});
</script>

<style scoped lang="less">
.project-group {
  margin-top: 30px;

  .name {
    margin: 0 0.5%;
    margin-bottom: 20px;
    font-size: 18px;
    font-weight: bold;
    color: #082340;
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    padding-right: 16px;

    .group-name {
      .desc {
        margin-left: 12px;
        font-size: 14px;
        color: #082340;
        opacity: 0.46;
      }
    }
    .more {
      font-size: 14px;
      color: #6b7b8d;
      cursor: pointer;
      &::after {
        display: inline-block;
        content: '';
        width: 16px;
        height: 16px;
        background: url('@/assets/svgs/group/more.svg');
        position: relative;
        top: 3px;
        right: -4px;
      }
      &:hover {
        color: #096dd9;
        &::after {
          background: url('@/assets/svgs/group/more-active.svg');
        }
      }
    }
  }

  .projects {
    display: flex;
    flex-wrap: wrap;
  }
}
</style>
