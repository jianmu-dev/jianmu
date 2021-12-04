<template>
  <div class="project-group" v-loading="loading">
    <div class="name">
      <span>默认分组</span>
      <span class="desc">（共有 {{ projects.length }} 个项目）</span>
    </div>
    <div class="projects">
      <jm-empty v-if="projects.length === 0"/>
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
import { defineComponent, getCurrentInstance, onBeforeMount, onBeforeUnmount, ref } from 'vue';
import { IProjectVo } from '@/api/dto/project';
import { queryProject } from '@/api/view-no-auth';
import { IQueryForm } from '@/model/modules/project';
import { ProjectStatusEnum } from '@/api/dto/enumeration';
import ProjectItem from '@/views/common/project-item.vue';
import { HttpError, TimeoutError } from '@/utils/rest/error';

const MAX_AUTO_REFRESHING_OF_NO_RUNNING_COUNT = 5;

export default defineComponent({
  components: { ProjectItem },
  setup() {
    const { proxy } = getCurrentInstance() as any;
    const loading = ref<boolean>(false);
    const projects = ref<IProjectVo[]>([]);
    const queryForm = ref<IQueryForm>({});
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
        projects.value = await queryProject({ ...queryForm.value });
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
        projects.value = await queryProject({ ...queryForm.value });
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        loading.value = false;
      }
    };

    // 初始化项目列表
    onBeforeMount(() => loadProject());

    onBeforeUnmount(() => {
      console.log('终止自动刷新项目列表');
      clearInterval(autoRefreshingInterval);
    });

    return {
      loading,
      ProjectStatusEnum,
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
    };
  },
});
</script>

<style scoped lang="less">
.project-group {
  margin-top: 30px;

  .name {
    margin-left: 0.5%;
    margin-bottom: 20px;
    font-size: 18px;
    font-weight: bold;
    color: #082340;

    .desc {
      margin-left: 12px;
      font-size: 14px;
      color: #082340;
      opacity: 0.46;
    }
  }

  .projects {
    display: flex;
    flex-wrap: wrap;
  }
}
</style>