<template>
  <div class="project-group" v-loading="loading">
    <div v-if="!pageable" class="name">
      <div class="group-name">
        <router-link
          :to="{ path: `/project-group/detail/${projectGroup?.id}` }"
          >{{ projectGroup?.name }}</router-link
        >
        <span class="desc">（共有 {{ projectPage.total }} 个项目）</span>
      </div>
    </div>
    <div class="projects">
      <jm-empty v-if="projects.length === 0 && pageable" />
      <jm-draggable
        v-else-if="moveListener"
        class="list"
        v-model="projectList"
        @change="sortList"
        @start="start"
        @end="() => (currentSelected = false)"
      >
        <transition-group type="transition" name="flip-list">
          <project-item
            v-for="(project, index) in projectList"
            :key="project.id"
            :_id="project.id"
            :project="project"
            @mouseenter="over(project.id)"
            @mouseleave="leave"
            :move-mode="moveListener"
            :move="moveClassList[index] === 'move'"
            @running="handleProjectRunning"
            @synchronized="handleProjectSynchronized"
            @deleted="handleProjectDeleted"
          />
        </transition-group>
      </jm-draggable>
      <project-item
        v-else
        v-for="project of projects"
        :key="project.id"
        :project="project"
        @running="handleProjectRunning"
        @synchronized="handleProjectSynchronized"
        @deleted="handleProjectDeleted"
      />
    </div>
    <!-- 显示更多 -->
    <div class="load-more" v-if="pageable" v-scroll="scrollObj">
      <jm-load-more
        :state="loadState"
        :load-more="btnDown"
        v-if="projects.length !== 0"
      ></jm-load-more>
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
  watch,
  inject,
} from 'vue';
import { IProjectVo } from '@/api/dto/project';
import { IProjectGroupVo } from '@/api/dto/project-group';
import { queryProject } from '@/api/view-no-auth';
import { IQueryForm } from '@/model/modules/project';
import { ProjectStatusEnum } from '@/api/dto/enumeration';
import ProjectItem from '@/views/common/project-item.vue';
import { HttpError, TimeoutError } from '@/utils/rest/error';
import { IPageVo } from '@/api/dto/common';
import { Mutable } from '@/utils/lib';
import { updateProjectGroupProjectSort } from '@/api/project-group';
import { START_PAGE_NUM, DEFAULT_PAGE_SIZE } from '@/utils/constants';
import { StateEnum } from '@/components/load-more/enumeration';

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
    // 是否开启移动模式
    move: {
      type: Boolean,
      default: false,
    },
  },
  setup(props: any) {
    const { proxy } = getCurrentInstance() as any;
    const loading = ref<boolean>(false);
    const scrollableEl = inject('scrollableEl');
    const projectPage = ref<Mutable<IPageVo<IProjectVo>>>({
      total: 0,
      pages: 0,
      list: [],
    });
    const projects = computed<IProjectVo[]>(() => projectPage.value.list);
    // 显示更多
    const loadState = ref<StateEnum>(StateEnum.MORE);
    const projectList = ref<Mutable<IProjectVo>[]>([]);
    const queryForm = ref<IQueryForm>({
      pageNum: START_PAGE_NUM,
      pageSize: props.pageable?40:DEFAULT_PAGE_SIZE,
      projectGroupId: props.projectGroup?.id,
      name: props.name,
    });
    const autoRefreshingOfNoRunningCount = ref<number>(0);
    const loadingMore = ref<boolean>(false);
    console.log('开启自动刷新项目列表');
    let autoRefreshingInterval: any;
    const refreshHandler = () => {
      autoRefreshingInterval = setInterval(async () => {
        if (loadingMore.value === true) {
          return;
        }
        if (
          !projects.value.find(
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

        try {
          projectPage.value = await queryProject({
            pageNum: START_PAGE_NUM,
            pageSize: props.pageable?projects.value.length:DEFAULT_PAGE_SIZE,
            projectGroupId: props.projectGroup?.id,
            name: props.name,
          });
        } catch (err) {
          // 忽略错误
          console.warn(err.message);
        }
      }, 3000);
    };
    const loadProject = async () => {
      try {
        // 不分页加载项目列表数据
        if (!props.pageable) {
          projectPage.value = await queryProject({ ...queryForm.value });
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
      if(props.pageable){
        loading.value = true;
      }
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
    });
    // onUpdated(() => {
    //   if (props.move) {
    //     return;
    //   }
    //   // 关闭拖拽模式将拖拽后的新数组数据同步
    //   projectPage.value.list = projectList.value;
    // });

    // TODO watch待优化
    watch(
      () => props.move,
      flag => {
        if (flag) {
          return;
        }
        // 关闭拖拽模式将拖拽后的新数组数据同步
        projectPage.value.list = projectList.value;
      },
    );
    // 拖拽排序
    const currentSelected = ref<boolean>(false);
    const currentItem = ref<string>('-1');
    let setCurrentItemTimer: any;
    const sortList = async (e: any) => {
      const {
        moved: { newIndex: targetSort, oldIndex: originSort, element },
      } = e;
      try {
        // 向移动
        targetSort < originSort
          ? await updateProjectGroupProjectSort(props.projectGroup.id, {
            originProjectId: element.id,
            targetProjectId: projectList.value[targetSort + 1].id,
          })
          : await updateProjectGroupProjectSort(props.projectGroup.id, {
            originProjectId: element.id,
            targetProjectId: projectList.value[targetSort - 1].id,
          });
      } catch (err) {
        proxy.$throw(err, proxy);
        // 未调换成功，将数据位置对调状态还原
        const spliceProjectList = projectList.value.splice(targetSort, 1);
        projectList.value.splice(originSort, 0, ...spliceProjectList);
      }
      // 设置定时延迟，不让mouseenter事件因为页面渲染的问题被自动触发，导致选中样式出现问题
      currentSelected.value = true;
      setCurrentItemTimer = setTimeout(() => {
        currentItem.value = e.moved.element.id;
        currentSelected.value = false;
      }, 400);
    };
    const moveClassList = computed<string[]>(() =>
      projectList.value.map(({ id }) => {
        return id === currentItem.value ? 'move' : '';
      }),
    );
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
      scrollObj,
      scrollableEl,
      loadState,
      btnDown,
      moveClassList,
      leave() {
        currentItem.value = '';
      },
      over(id: string) {
        if (currentSelected.value) {
          return;
        }
        currentItem.value = id;
      },
      projectList,
      sortList,
      start(e: any) {
        currentSelected.value = true;
        currentItem.value = e.item.getAttribute('_id');
      },
      currentSelected,
      moveListener,
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
    };
  },
});
</script>

<style scoped lang="less">
.project-group {
  margin-top: 30px;

  .name {
    font-size: 18px;
    font-weight: bold;
    color: #082340;
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    padding-right: 5px;

    .group-name {
      .desc {
        margin-left: 12px;
        font-size: 14px;
        font-weight: normal;
        color: #082340;
        opacity: 0.46;
      }
    }
  }

  .projects {
    display: flex;
    flex-wrap: wrap;
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
