<template>
  <div class="project-group" v-loading="loading">
    <folding :status="toggle" :page-able="pageable">
      <template #prefix v-if="!pageable">
        <span class="prefix-wrapper">
          <i
            :class="['jm-icon-button-right', 'prefix', toggle ? 'rotate' : '']"
            :disabled="projectPage.total === 0"
            @click="saveFoldStatus(toggle, projectGroup.id)"
          />
        </span>
      </template>
      <template #title>
        <div v-if="!pageable" class="name">
          <div class="group-name">
            <router-link :to="{ path: `/project-group/detail/${projectGroup?.id}` }"
              >{{ projectGroup?.name }}
            </router-link>
            <span class="desc">（共有 {{ projectPage.total >= 0 ? projectPage.total : 0 }} 个项目）</span>
          </div>
          <div class="more-container" v-if="!pageable && projectPage.total > 10">
            <router-link :to="{ path: `/project-group/detail/${projectGroup?.id}` }">
              查看更多
              <i class="more-icon"></i>
            </router-link>
          </div>
        </div>
      </template>
      <template #default>
        <div>
          <div class="projects" ref="projectsRef">
            <jm-empty description="暂无项目" :image-size="98" v-if="projects.length === 0 && pageable && isDetail" />
            <jm-empty
              :image="noDataImg"
              description="没有搜到相关结果"
              :image-size="98"
              v-else-if="projects.length === 0 && pageable"
            />
            <jm-sorter v-else-if="moveListener" class="list" v-model="projectList" @change="sortList">
              <project-item
                v-for="(project, index) in projectList"
                :concurrent="project.concurrent"
                :key="project.id"
                :_id="project.id"
                :project="project"
                @mouseenter="enter(project.id)"
                @mouseleave="leave"
                :move-mode="moveListener"
                :move="moveClassList[index] === 'move'"
                @triggered="handleProjectTriggered"
                @terminated="handleProjectTerminated"
                @synchronized="handleProjectSynchronized"
                @deleted="handleProjectDeleted"
              />
            </jm-sorter>
            <project-item
              v-else
              v-for="project of projects"
              :concurrent="project.concurrent"
              :key="project.id"
              :project="project"
              @triggered="handleProjectTriggered"
              @synchronized="handleProjectSynchronized"
              @deleted="handleProjectDeleted"
              @terminated="handleProjectTerminated"
            />
          </div>
          <!-- 显示更多 -->
          <div class="load-more" v-if="pageable" v-scroll="scrollObj">
            <jm-load-more :state="loadState" :load-more="btnDown" v-if="projects.length !== 0"></jm-load-more>
          </div>
        </div>
      </template>
    </folding>
  </div>
</template>

<script lang="ts">
import {
  computed,
  defineComponent,
  getCurrentInstance,
  inject,
  nextTick,
  onBeforeMount,
  onBeforeUnmount,
  onUpdated,
  PropType,
  ref,
  watch,
} from 'vue';
import { IProjectVo } from '@/api/dto/project';
import { IProjectGroupVo } from '@/api/dto/project-group';
import { queryProject } from '@/api/view-no-auth';
import { IQueryForm } from '@/model/modules/project';
import { ProjectStatusEnum, SortTypeEnum } from '@/api/dto/enumeration';
import ProjectItem from '@/views/common/project-item.vue';
import { IPageVo } from '@/api/dto/common';
import { Mutable } from '@/utils/lib';
import { updateProjectGroupProjectSort } from '@/api/project-group';
import { DEFAULT_PAGE_SIZE, START_PAGE_NUM } from '@/utils/constants';
import { StateEnum } from '@/components/load-more/enumeration';
import Folding from '@/views/common/folding.vue';
import { createNamespacedHelpers, useStore } from 'vuex';
import { namespace } from '@/store/modules/project-group';
import JmSorter from '@/components/sorter/index.vue';
import noDataImg from '@/assets/svgs/index/no-data.svg';
import sleep from '@/utils/sleep';

const MAX_AUTO_REFRESHING_OF_NO_RUNNING_COUNT = 5;

export default defineComponent({
  components: { JmSorter, ProjectItem, Folding },
  props: {
    isDetail: {
      type: Boolean,
      default: false,
    },
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
    // 是否开启移动模式
    move: {
      type: Boolean,
      default: false,
    },
    // 项目组排序类型
    sortType: {
      type: String,
      default: SortTypeEnum.DEFAULT_SORT,
    },
  },
  setup(props: any) {
    const store = useStore();
    const { mapMutations } = createNamespacedHelpers(namespace);
    const projectGroupFoldingMapping = store.state[namespace];
    // 根据项目组在vuex中保存的状态，进行展开、折叠间的切换
    const toggle = computed<boolean>(() => {
      // 只有全等于为undefined说明该项目组一开始根本没有做折叠操作
      if (projectGroupFoldingMapping[props.projectGroup?.id] === undefined) {
        return true;
      }
      return projectGroupFoldingMapping[props.projectGroup.id];
    });
    const projectsRef = ref<HTMLElement>();
    const spacing = ref<string>('');
    // 是否正在排序
    const isSorting = ref<boolean>(false);
    const { proxy } = getCurrentInstance() as any;
    const loading = ref<boolean>(false);
    const scrollableEl = inject('scrollableEl');
    const projectPage = ref<Mutable<IPageVo<IProjectVo>>>({
      total: -1,
      pages: 0,
      list: [],
      pageNum: START_PAGE_NUM,
    });
    const projects = computed<IProjectVo[]>(() => projectPage.value.list);
    // 显示更多
    const loadState = ref<StateEnum>(StateEnum.MORE);
    const projectList = ref<Mutable<IProjectVo>[]>([]);
    const queryForm = ref<IQueryForm>({
      pageNum: START_PAGE_NUM,
      pageSize: props.pageable ? 40 : DEFAULT_PAGE_SIZE,
      projectGroupId: props.projectGroup?.id,
      name: props.name,
      sortType: props.sortType,
    });
    const autoRefreshingOfNoRunningCount = ref<number>(0);
    const loadingMore = ref<boolean>(false);
    console.log('开启自动刷新项目列表');
    let autoRefreshingInterval: any;
    // 保存单个项目组的展开折叠状态
    const saveFoldStatus = (status: boolean, id: string) => {
      // 改变状态
      const toggle = !status;
      // 调用vuex的mutations更改对应项目组的状态
      proxy.mutate({
        id,
        status: toggle,
      });
    };
    const refreshHandler = () => {
      autoRefreshingInterval = setInterval(async () => {
        if (loadingMore.value === true) {
          return;
        }
        if (!projects.value.find(item => item.status === ProjectStatusEnum.RUNNING)) {
          // 不存在running场景
          if (autoRefreshingOfNoRunningCount.value < MAX_AUTO_REFRESHING_OF_NO_RUNNING_COUNT) {
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
          projectPage.value = await queryProject({
            pageNum: START_PAGE_NUM,
            pageSize: props.pageable ? projects.value.length : DEFAULT_PAGE_SIZE,
            projectGroupId: props.projectGroup?.id,
            name: props.name,
            sortType: props.sortType,
          });
        } catch (err) {
          // 忽略错误
          console.warn(err.message);
        }
      }, 3000);
    };
    // 重新加载当前已经加载过的项目
    const reloadCurrentProjectList = async () => {
      try {
        const { pageSize, pageNum } = queryForm.value;
        // 获得当前已经加载了的总数
        const currentCount = pageSize * pageNum;
        projectPage.value = await queryProject({ ...queryForm.value, pageNum: 1, pageSize: currentCount });
      } catch (err) {
        proxy.$throw(err, proxy);
      }
    };

    const loadProject = async () => {
      try {
        // 不分页加载项目列表数据
        if (!props.pageable) {
          projectPage.value = await queryProject({ ...queryForm.value });
          // 项目组中项目为空，将其自动折叠
          if (projectPage.value.total === 0) {
            saveFoldStatus(true, props.projectGroup?.id);
          }
          return;
        }
        loadState.value = StateEnum.LOADING;
        // 在加载时，控制不自动刷新
        loadingMore.value = true;
        const { list, pages } = await queryProject({
          ...queryForm.value,
        });
        // 点击加载更多按钮请求的数据加入到排序后的数组里
        projectList.value.push(...list);
        projectPage.value.pages = pages;
        projectPage.value.list = projectList.value;
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        loading.value = false;
        loadState.value = StateEnum.MORE;
        if (queryForm.value.pageNum >= projectPage.value.pages) {
          // 加载完成
          loadState.value = StateEnum.NO_MORE;
        }
        loadingMore.value = false;
      }
    };
    // 项目组加载更多
    const btnDown = async () => {
      // 如果状态为没有更多控制加载
      if (loadState.value === StateEnum.NO_MORE) {
        return;
      }
      clearInterval(autoRefreshingInterval);
      queryForm.value.pageNum += 1;
      await loadProject();
      refreshHandler();
    };
    const moveListener = computed(() => {
      props.move ? clearInterval(autoRefreshingInterval) : refreshHandler();
      return props.move;
    });
    // 初始化项目列表
    onBeforeMount(async () => {
      await nextTick(() => {
        queryForm.value.name = props.name;
      });
      if (props.pageable) {
        loading.value = true;
      }
      await loadProject();
    });
    onUpdated(async () => {
      if (queryForm.value.name === props.name && queryForm.value.projectGroupId === props.projectGroup?.id) {
        return;
      }
      queryForm.value.name = props.name;
      queryForm.value.projectGroupId = props.projectGroup?.id;
    });
    // TODO watch待优化
    watch(
      () => props.move,
      flag => {
        if (flag) {
          // 计算每个项之间的间距大小
          spacing.value = projectsRef.value?.offsetWidth * 0.008 + 'px';
          return;
        }
        // 关闭拖拽模式将拖拽后的新数组数据同步
        projectPage.value.list = projectList.value;
      },
    );
    const currentItem = ref<string>('');
    let setCurrentItemTimer: any;
    const sortList = async (e: any) => {
      isSorting.value = true;
      const { oldElement, newElement, originArr } = e;
      try {
        await updateProjectGroupProjectSort(props.projectGroup.id, {
          originProjectId: newElement.id,
          targetProjectId: oldElement.id,
        });
      } catch (err) {
        proxy.$throw(err, proxy);
        // 未调换成功，将数据位置对调状态还原
        projectList.value = originArr;
      }
      await sleep(300);
      isSorting.value = false;
    };
    const moveClassList = computed<string[]>(() => {
      return projectList.value.map(({ id }) => {
        return id === currentItem.value ? 'move' : '';
      });
    });
    onBeforeUnmount(() => {
      console.log('终止自动刷新项目列表');
      clearInterval(autoRefreshingInterval);
      clearTimeout(setCurrentItemTimer);
    });
    const scrollObj = {
      loadMore: btnDown,
      scrollableEl,
    };
    return {
      noDataImg,
      projectsRef,
      spacing,
      ...mapMutations({
        mutate: 'mutate',
      }),
      toggle,
      scrollObj,
      scrollableEl,
      loadState,
      btnDown,
      moveClassList,
      leave() {
        if (isSorting.value) {
          return;
        }
        currentItem.value = '';
      },
      enter(id: string) {
        if (isSorting.value) {
          return;
        }
        currentItem.value = id;
      },
      projectList,
      sortList,
      moveListener,
      loading,
      ProjectStatusEnum,
      projectPage,
      projects,
      queryForm,
      handleProjectSynchronized: async () => {
        // 刷新项目列表，保留查询状态
        await reloadCurrentProjectList();
      },
      handleProjectDeleted: (id: string) => {
        const index = projects.value.findIndex(item => item.id === id);
        projects.value.splice(index, 1);
      },
      handleProjectTriggered: async (id: string) => {
        await sleep(400);
        // 刷新项目列表，保留查询状态
        await reloadCurrentProjectList();
      },
      handleProjectTerminated: async (id: string) => {
        // 刷新项目列表，保留查询状态
        await reloadCurrentProjectList();
      },
      saveFoldStatus,
      projectGroupFoldingMapping,
    };
  },
});
</script>

<style scoped lang="less">
.project-group {
  margin-top: 24px;

  .prefix-wrapper {
    cursor: not-allowed;
    display: flex;
    align-items: center;
  }

  .prefix {
    cursor: pointer;
    font-size: 12px;
    transition: all 0.1s linear;
    color: #6b7b8d;

    &[disabled='true'] {
      pointer-events: none;
      color: #a7b0bb;
    }

    &.rotate {
      transform: rotate(90deg);
      transition: all 0.1s linear;
    }
  }

  .name {
    margin-left: 10px;
    font-size: 18px;
    font-weight: bold;
    color: #082340;
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    padding-right: 0.7%;

    .group-name {
      .desc {
        margin-left: 12px;
        font-size: 14px;
        font-weight: normal;
        color: #082340;
        opacity: 0.46;
      }
    }

    .more-container {
      width: 86px;
      height: 24px;
      background: #eff7ff;
      border-radius: 15px;
      font-size: 12px;
      font-weight: 400;
      cursor: pointer;
      display: flex;
      justify-content: center;
      align-items: center;

      a {
        color: #6b7b8d;
        line-height: 24px;
      }

      .more-icon {
        display: inline-block;
        width: 12px;
        height: 12px;
        text-align: center;
        line-height: 12px;
        background: url('@/assets/svgs/btn/more.svg') no-repeat;
        position: relative;
        top: 1.4px;
        right: 0px;
      }

      &:hover {
        color: #096dd9;

        a {
          color: #096dd9;
        }

        .more-icon {
          background: url('@/assets/svgs/btn/more-active.svg') no-repeat;
        }
      }
    }
  }

  .projects {
    display: flex;
    flex-wrap: wrap;

    .el-empty {
      padding-top: 102px;
    }

    ::v-deep(.jm-sorter) {
      .drag-target-insertion {
        width: v-bind(spacing);
        border-width: 0;
        background-color: transparent;

        &::after {
          content: '';
          width: 60%;
          height: 100%;
          box-sizing: border-box;
          border: 2px solid #096dd9;
          background: rgba(9, 109, 217, 0.3);
          position: absolute;
          top: 0;
          left: 20%;
        }
      }
    }

    .list {
      width: 100%;
      display: flex;
      flex-wrap: wrap;
    }
  }

  .load-more {
    margin: 10px auto 0px;
    display: flex;
    justify-content: center;
  }
}
</style>
