<template>
  <!-- workflow-viewer整体容器 -->
  <div class="jm-workflow-viewer" v-loading="loadingGraph">
  <!-- container.value?.parentElement -->
    <graph-dsl-panel
      ref="graphDslPanel"
      :tasks="tasks"
      :readonly="readonly"
      :dslMode="dslMode"
      :triggerType="triggerType"
      :workflow-ref="workflowRef"
      :workflow-version="workflowVersion"
      @mouseenter-node="mouseenterNode"
      @sync-graph-data="getWorkflowGraph"
      @sync-graph-loading="openLoading"
    ></graph-dsl-panel>
    <!-- 顶部工具栏 -->
    <top-toolbar
      :readonly="readonly"
      :dslMode="dslMode"
      @change-view-mode="changeViewMode"
    ></top-toolbar>
    <!-- 底部工具栏 -->
    <bottom-toolbar
      v-if="!firstLoad"
      :is-x6="isX6"
      :dslMode="dslMode"
      :zoom-value="zoom"
      :dslType="dslType"
      :readonly="readonly"
      :fullscreen-el="fullscreenEl"
      @on-zoom="handleZoom"
      @rotate="handleRotation"
      @on-fullscreen="handleFullscreen"
    ></bottom-toolbar>
    <!-- 底部状态统计栏 -->
    <task-state
      v-if="!readonly && !dslMode && !firstLoad && taskStates.length > 0"
      :taskStates="taskStates"
      @mouse-event="highlightNodeState"
    ></task-state>
    <!-- 节点悬浮操作栏 -->
    <node-toolbar
      v-if="!dslMode && nodeEvent"
      :zoom="zoom"
      :readonly="readonly"
      :graph-type="graphType"
      :node-event="nodeEvent"
      :task-status="selectedTask.status"
      :task-business-id="selectedTask.businessId"
      @node-click="clickNode"
      @mouseleave="destroyNodeToolbar"
    ></node-toolbar>
  </div>
</template>

<script lang="ts">
import {
  ref,
  computed,
  PropType,
  SetupContext,
  defineComponent,
} from 'vue';
import G6 from '@antv/g6';
import { ITaskExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { ViewModeEnum, TaskStatusEnum, TriggerTypeEnum } from '@/api/dto/enumeration';
import { GraphTypeEnum, NodeToolbarTabTypeEnum, NodeTypeEnum } from './model/data/enumeration';
import { INodeMouseoverEvent } from './model/data/common';
import { sortTasks } from './model/util';
import { WorkflowGraph } from './model/workflow-graph';
import graphDslPanel from './layout/graph-dsl-panel.vue';
import topToolbar from './layout/top-toolbar.vue';
import bottomToolbar from './layout/bottom-toolbar.vue';
import taskState from './layout/task-states.vue';
import nodeToolbar from './layout/node-toolbar.vue';

//  引入数组工具类
import './utils/array.ts';
// 注册自定义g6元素
Object.values(import.meta.globEager('./shapes/**')).forEach(({ default: register }) => register(G6));
export default defineComponent({
  name: 'jm-workflow-viewer',
  props: {
    // 是否集成
    entry: {
      type: Boolean,
      default: false,
    },
    readonly: {
      type: Boolean,
      default: false,
    },
    viewMode: {
      type: String as PropType<ViewModeEnum>,
      default: ViewModeEnum.GRAPHIC,
    },
    tasks: {
      type: Array as PropType<ITaskExecutionRecordVo[]>,
      // required: true,
    },
    triggerType: {
      type: String as PropType<TriggerTypeEnum>,
      required: true,
    },
    workflowRef: {
      type: String,
      required: true,
    },
    workflowVersion: {
      type: String,
      required: true,
    },
    fullscreenRef: HTMLElement,
  },
  emits: ['click-task-node', 'click-webhook-node', 'change-view-mode', 'async-dsl', 'is-fullscreen'],
  components: { graphDslPanel, topToolbar, bottomToolbar, taskState, nodeToolbar },
  setup(props, { emit }: SetupContext) {
    // 缩放级别
    const zoom = ref<number>(0);
    // workflowGraph实例
    let workflowGraph: WorkflowGraph;
    // 是否为X6
    const isX6 = ref<boolean>(false);
    // 鼠标移动到节点上的事件对象值
    const nodeEvent = ref<INodeMouseoverEvent>();
    // workflowGraph实例化ing中
    const loadingGraph = ref<boolean>(false);
    // 预览模式(图示-> false/yaml-> true)
    const dslMode = ref<boolean>(props.viewMode===ViewModeEnum.YAML);
    // 第一次加载viewer 也用作加载状态栏和底部工具栏
    const firstLoad = ref<boolean>(true);
    // 第一次加载viewer 也用作加载状态栏和底部工具栏
    const graphDslPanel = ref();

    return {
      isX6,
      zoom,
      dslMode,
      nodeEvent,
      loadingGraph,
      firstLoad,
      graphDslPanel,
      // 鼠标移动到不同状态上高亮对应状态节点
      highlightNodeState(status: string, boo: boolean) {
        workflowGraph.highlightNodeState(status, boo);
      },
      // 更新画布后同步实例数据
      getWorkflowGraph(syncData: WorkflowGraph) {
        // 更新实例
        workflowGraph = syncData;
        // 同步detail的dsl->弹窗展示
        emit('async-dsl', workflowGraph.dsl);
        // 更新缩放视图
        zoom.value = workflowGraph.getZoom();
        // X6赋值
        isX6.value = workflowGraph.getIsX6();
        // 延迟更新节点状态(解决第一次进入或刷新时 props.tasks长度为0的情况会报错，故延长50毫秒)
        if (firstLoad.value) {
          firstLoad.value = false;
          setTimeout(()=> {
            props.tasks?.length && workflowGraph.updateNodeStates(props.tasks);
          }, 500);
        } else {
          props.tasks?.length && workflowGraph.updateNodeStates(props.tasks);
        }
        // loading完毕
        loadingGraph.value = false;
      },
      TaskStatusEnum,
      dslType: computed(() => workflowGraph.getDslType()),
      graphType: computed<GraphTypeEnum>(() => workflowGraph.getGraphType()),
      taskStates: computed(() => {
        if (!props.tasks) {
          return [];
        }
        const sArr: {
          status: string;
          count: number;
        }[] = [];

        const tasks = props.tasks.filter(({ defKey }: { defKey: string}) =>
          defKey !== 'Start' && defKey !== 'End' && defKey !== 'Condition' && defKey !== 'Switch');
        const taskMap = new Map();
        // 按开始时间升序排序，保证最后一个是最新的
        sortTasks(tasks, false)
          .forEach((task: ITaskExecutionRecordVo) => taskMap.set(task.nodeName, task));
        Object.keys(TaskStatusEnum).forEach(status => sArr.push({
          status,
          count: status === TaskStatusEnum.INIT ? (workflowGraph.getAsyncTaskNodeCount() - taskMap.size) : 0,
        }));

        taskMap.forEach(({ status }: ITaskExecutionRecordVo) => {
          const s = sArr.find(item => item.status === status);
          if (s) {
            s.count += 1;
          }
        });

        return sArr;
      }),
      selectedTask: computed<{ businessId: string, status: TaskStatusEnum}>(() => {
        if (!nodeEvent.value || nodeEvent.value.type !== NodeTypeEnum.ASYNC_TASK || props.tasks?.length === 0 || !props.tasks) {
          return { businessId: '', status: TaskStatusEnum.INIT };
        }
        // 按开始时间降序排序，保证第一个是最新的
        const tasks = sortTasks(props.tasks, true, nodeEvent.value.id);
        return {
          businessId: tasks[0].businessId,
          status: tasks[0].status,
        };
      }),
      // 父级元素->用于全屏
      fullscreenEl: computed(() => props.fullscreenRef || graphDslPanel.value.container.parentElement),
      // 开启实例化中页面加载动画
      openLoading() {
        loadingGraph.value = true;
      },
      // 缩放
      handleZoom(val?: number) {
        // 执行缩放
        workflowGraph.handleZoom(val);
        // 更新缩放视图
        zoom.value = workflowGraph.getZoom();
      },
      // 全屏/退出全屏
      handleFullscreen(boo: boolean) {
        // 同步全屏状态 预览需要这个状态
        emit('is-fullscreen', boo);
        // 执行内置 全屏/退出全屏方法
        workflowGraph.handleFullscreen((num: number) => {
          // 更新缩放视图
          zoom.value = num;
        });
      },
      // 旋转
      handleRotation() {
        workflowGraph.rotate(props.tasks? props.tasks : []);
      },
      // 点击节点上的任务和参数
      clickNode(id: string, nodeType: NodeTypeEnum, tabType: NodeToolbarTabTypeEnum) {
        switch (nodeType) {
          case NodeTypeEnum.ASYNC_TASK:
            // id为taskInstanceId
            emit('click-task-node', id, tabType);
            break;
          case NodeTypeEnum.WEBHOOK:
            // id为eb目标唯一标识
            emit('click-webhook-node', id, tabType);
        }
      },
      // 图示/yaml 切换
      changeViewMode(mode: ViewModeEnum) {
        // 改变本组件(预览时)
        dslMode.value = mode === ViewModeEnum.YAML;
        // 改变父组件(地址栏，详情页)
        emit('change-view-mode', mode);
      },
      // 重置节点悬浮窗(隐藏dom,重置nodeEvent)
      destroyNodeToolbar() {
        workflowGraph.hideNodeToolbar(nodeEvent.value!.id);
        nodeEvent.value = undefined;
      },
      // 鼠标移动节点上去 x6evt数据监听回调
      mouseenterNode(evt: INodeMouseoverEvent) {
        nodeEvent.value = evt;
      },
    };
  },
});
</script>

<style lang="less">
.jm-workflow-viewer {
  @import './theme/x6';
  @keyframes x6-edge-running {
    to {
      stroke-dashoffset: -1000
    }
  }
  position: relative;
  height: 100%;
  background-color: #FFFFFF;
}
</style>