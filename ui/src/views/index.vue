<template>
  <div class="index" v-loading="loading">
    <div class="main">
      <div class="menu-bar">
        <div class="left-area">
          <router-link :to="{ name: 'create-project' }">
            <jm-tooltip content="新增项目" placement="top">
              <button class="add"></button>
            </jm-tooltip>
          </router-link>
          <router-link :to="{ name: 'import-project' }">
            <jm-tooltip content="导入项目" placement="top">
              <button class="git"></button>
            </jm-tooltip>
          </router-link>
        </div>
        <div class="right-area">
          <router-link :to="{ name: 'node-library' }">
            <jm-tooltip content="本地节点库" placement="top">
              <button class="node-library"></button>
            </jm-tooltip>
          </router-link>
          <router-link :to="{ name: 'secret-key' }">
            <jm-tooltip content="密钥管理" placement="top">
              <button class="secret-key"></button>
            </jm-tooltip>
          </router-link>
        </div>
      </div>
      <div class="separator">全部项目</div>
      <div class="group">
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
    </div>
    <bottom-nav/>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, onBeforeMount, onBeforeUnmount, ref } from 'vue';
import { IProjectVo } from '@/api/dto/project';
import { queryProject } from '@/api/view-no-auth';
import { IQueryForm } from '@/model/modules/project';
import { ProjectStatusEnum } from '@/api/dto/enumeration';
import BottomNav from '@/views/nav/bottom2.vue';
import ProjectItem from '@/views/common/project-item.vue';
import { HttpError, TimeoutError } from '@/utils/rest/error';

const MAX_AUTO_REFRESHING_OF_NO_RUNNING_COUNT = 5;

export default defineComponent({
  components: { BottomNav, ProjectItem },
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
.index {
  .main {
    min-height: calc(100vh - 125px);

    .menu-bar {
      padding: 45px 0;
      display: flex;
      justify-content: space-between;
      align-items: center;

      .left-area {
        white-space: nowrap;

        button {
          width: 186px;
          height: 64px;
          background-color: #ffffff;
          box-shadow: 0 6px 14px 0 #acc3ee;
          border-radius: 4px;
          border: 0;
          background-position: center center;
          background-repeat: no-repeat;
          cursor: pointer;

          &:active {
            opacity: 0.8;
          }

          &.add {
            margin-left: 14px;
            background-image: url('@/assets/svgs/index/add-btn.svg');
          }

          &.git {
            margin-left: 40px;
            background-image: url('@/assets/svgs/index/git-btn.svg');
          }
        }
      }

      .right-area {
        margin-right: 0.5%;

        button {
          width: 48px;
          height: 48px;
          background-color: transparent;
          border: 0;
          background-position: center center;
          background-repeat: no-repeat;
          cursor: pointer;

          &:active {
            opacity: 0.8;
          }

          &.hub {
            background-image: url('@/assets/svgs/index/hub-btn.svg');
          }

          &.node-library {
            margin-left: 40px;
            background-image: url('@/assets/svgs/index/node-library-btn.svg');
          }

          &.secret-key {
            margin-left: 40px;
            background-image: url('@/assets/svgs/index/secret-key-btn.svg');
          }
        }
      }
    }

    .separator {
      margin-left: 0.5%;
      font-size: 24px;
      font-weight: bold;
      color: #082340;
      opacity: 0.5;
    }

    .group {
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
  }
}
</style>
