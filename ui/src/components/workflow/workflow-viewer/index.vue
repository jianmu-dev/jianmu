<template>
  <div class="jm-workflow-viewer">
    <div v-if="!readonly && !dslMode && graph && tasks.length > 0" class="task-states">
      <task-state v-for="{status, count} in taskStates"
                  :key="status" :status="status" :count="count"
                  @mouseenter="graph?.highlightNodeState(status, true)"
                  @mouseleave="graph?.highlightNodeState(status, false)"/>
    </div>
    <toolbar v-if="graph"
      :readonly="readonly"
      :dsl-type="graph?.dslType"
      :dslMode="dslMode"
      :zoom-value="zoom"
      :is-x6="workflowGraph?.isX6()"
      :fullscreen-el="fullscreenEl"
      @change-view-mode="(viewMode)=>$emit('change-view-mode', viewMode)"
      @click-process-log="clickProcessLog"
      @on-zoom="handleZoom"
      @on-fullscreen="handleFullscreen"
      @rotate="handleRotation"/>
    <node-toolbar v-if="!dslMode && nodeEvent"
                  :graph-type="graphType"
                  :readonly="readonly"
                  :task-business-id="selectedTask?.businessId"
                  :task-status="selectedTask?.status"
                  :node-event="nodeEvent"
                  :zoom="zoom"
                  @node-click="clickNode"
                  @mouseleave="destroyNodeToolbar"/>
    <!-- 文档流占位 -->
    <div class="canvas-container">
      <div class="view-mode" :class="{borderBottom: !readonly}">
        <div class="float-view-mode">
          <div class="tab" :class="{select: !dslMode}" @click="changeViewMode(ViewModeEnum.GRAPHIC)">图示</div>
          <div class="tab" :class="{select: dslMode}" @click="changeViewMode(ViewModeEnum.YAML)">yaml</div>
        </div>
        <div class="float-param-log" v-show="!dslMode && !readonly">
          <jm-tooltip content="全局参数" placement="bottom" :appendToBody="false">
            <div class="jm-icon-workflow-param" @click="clickParamLog"></div>
          </jm-tooltip>
          <div class="btn" @click="clickProcessLog">{{entry? '流水线':graph?.dslType===DslTypeEnum.PIPELINE?'管道':'流程'}}日志</div>
        </div>
      </div>
      <!-- <div v-show="!dslMode" class="canvas" ref="container"/> -->
      <div class="canvas" ref="container"/>
      <div class="dsl-editor-container" v-if="dslMode">
        <jm-dsl-editor :value="workflowGraph?.visibleDsl || ''" readonly/>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {
  computed,
  defineComponent,
  getCurrentInstance,
  onBeforeUpdate,
  onMounted,
  onUnmounted,
  PropType,
  ref,
  SetupContext,
} from 'vue';
import G6 from '@antv/g6';
import TaskState from './task-state.vue';
import Toolbar from './toolbar.vue';
import NodeToolbar from './node-toolbar.vue';
import { ITaskExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { DslTypeEnum, ViewModeEnum, TaskStatusEnum, TriggerTypeEnum } from '@/api/dto/enumeration';
import { GraphDirectionEnum, GraphTypeEnum, NodeToolbarTabTypeEnum, NodeTypeEnum } from './model/data/enumeration';
import { INodeMouseoverEvent } from './model/data/common';
import { sortTasks } from './model/util';
import { BaseGraph } from './model/base-graph';
import { WorkflowGraph } from './model/workflow-graph';

// 引入数组工具类
import './utils/array.ts';

// 注册自定义g6元素
Object.values(import.meta.globEager('./shapes/**')).forEach(({ default: register }) => register(G6));

export default defineComponent({
  name: 'jm-workflow-viewer',
  components: { TaskState, Toolbar, NodeToolbar },
  props: {
    entry: {
      type: Boolean,
      default: false,
    },
    dsl: String,
    readonly: {
      type: Boolean,
      default: false,
    },
    viewMode: {
      type: String as PropType<ViewModeEnum>,
      default: ViewModeEnum.GRAPHIC,
    },
    triggerType: String as PropType<TriggerTypeEnum>,
    nodeInfos: {
      type: Array,
      default: () => [],
    },
    tasks: {
      type: Array as PropType<ITaskExecutionRecordVo[]>,
      default: () => [],
    },
    fullscreenRef: HTMLElement,
  },
  emits: ['click-task-node', 'click-webhook-node', 'click-process-log', 'click-param-log', 'change-view-mode'],
  setup(props: any, { emit }: SetupContext) {
    const { proxy } = getCurrentInstance() as any;
    const container = ref<HTMLElement>();
    const workflowGraph = ref<WorkflowGraph>();
    const graph = ref<BaseGraph>();
    const nodeActionConfigured = ref<boolean>(false);
    const dslMode = ref<boolean>(props.viewMode===ViewModeEnum.YAML);
    const nodeEvent = ref<INodeMouseoverEvent>();
    const destroyNodeToolbar = () => {
      graph.value?.hideNodeToolbar(nodeEvent.value!.id);
      nodeEvent.value = undefined;
    };
    const mouseoverNode = (evt: INodeMouseoverEvent) => {
      if (nodeEvent.value) {
        // 上一个事件尚未释放时，保证先释放完，再触发
        destroyNodeToolbar();
        proxy.$nextTick(() => mouseoverNode(evt));
        return;
      }
      nodeEvent.value = evt;
    };
    const zoom = ref<number>();
    const updateZoom = () => {
      setTimeout(() => {
        if (!graph.value) {
          return;
        }

        zoom.value = Math.round(graph.value!.getZoom() * 100);
      });
    };

    const refreshGraph = (direction: GraphDirectionEnum = GraphDirectionEnum.HORIZONTAL) => {
      setTimeout(() => {
        // 保证渲染完成
        if (!graph.value) {
          return;
        }
        // 更新状态
        graph.value.updateNodeStates(props.tasks);
      }, 50);

      if (!graph.value) {
        if (!props.dsl || !props.triggerType || !container.value) {
          return;
        }

        workflowGraph.value = new WorkflowGraph(props.dsl, props.triggerType, props.nodeInfos, container.value as HTMLElement, direction);
        graph.value = workflowGraph.value!.graph;

        updateZoom();
      }

      if (!nodeActionConfigured.value) {
        // 禁止多次配置
        // 配置节点行为
        graph.value!.configNodeAction(mouseoverNode);
        nodeActionConfigured.value = true;
      }
    };

    onBeforeUpdate(() => refreshGraph());
    const destroy = ()=>{
      // fix: #I5EH4G
      workflowGraph.value!.destroy();
      workflowGraph.value = undefined;
      graph.value = undefined;
      nodeActionConfigured.value = false;
    };
    // 保证整个视图都渲染完毕，才能确定图的宽高
    onMounted(() => proxy.$nextTick(() => refreshGraph()));
    onUnmounted(() => {
      destroy();
    });
    
    const handleRotation = async () => {
      if (!graph.value || graph.value?.dslType !== DslTypeEnum.WORKFLOW) {
        return;
      }

      const direction = graph.value!.getDirection() === GraphDirectionEnum.HORIZONTAL ? GraphDirectionEnum.VERTICAL : GraphDirectionEnum.HORIZONTAL;
      destroy();
      refreshGraph(direction);
    };
    return {
      TaskStatusEnum,
      container,
      workflowGraph,
      graph,
      DslTypeEnum,
      ViewModeEnum,
      graphType: computed<GraphTypeEnum>(() => {
        if (!graph.value) {
          return GraphTypeEnum.G6;
        }
        return graph.value!.getGraphType();
      }),
      selectedTask: computed<{
        businessId: string;
        status: TaskStatusEnum;
      } | undefined>(() => {
        if (!nodeEvent.value || nodeEvent.value.type !== NodeTypeEnum.ASYNC_TASK) {
          return;
        }

        // 按开始时间降序排序，保证第一个是最新的
        const tasks = sortTasks(props.tasks, true, nodeEvent.value.id);
        return tasks.length === 0 ? {
          businessId: '',
          status: TaskStatusEnum.INIT,
        } : {
          businessId: tasks[0].businessId,
          status: tasks[0].status,
        };
      }),
      dslMode,
      nodeEvent,
      fullscreenEl: computed<HTMLElement>(() => props.fullscreenRef || container.value?.parentElement),
      clickProcessLog: () => {
        emit('click-process-log');
      },
      clickParamLog: () => {
        emit('click-param-log');
      },
      clickNode: (id: string, nodeType: NodeTypeEnum, tabType: NodeToolbarTabTypeEnum) => {
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
      destroyNodeToolbar,
      zoom,
      handleZoom: (val?: number) => {
        if (!graph.value) {
          return;
        }

        if (val === undefined) {
          graph.value!.fitView();
        } else {
          graph.value!.zoomTo(val);
        }

        updateZoom();
      },
      handleFullscreen: (_: boolean) => {
        if (!graph.value) {
          return;
        }

        if (container.value) {
          container.value.style.visibility = 'hidden';
        }

        setTimeout(() => {
          graph.value!.fitCanvas();
          updateZoom();
          if (container.value) {
            container.value.style.visibility = '';
          }
        }, 100);
      },
      handleRotation,
      taskStates: computed(() => {
        const sArr: {
          status: string;
          count: number;
        }[] = [];

        const tasks = props.tasks.filter(({ defKey }) =>
          defKey !== 'Start' && defKey !== 'End' && defKey !== 'Condition' && defKey !== 'Switch');
        const taskMap = new Map();
        // 按开始时间生序排序，保证最后一个是最新的
        sortTasks(tasks, false)
          .forEach((task: ITaskExecutionRecordVo) => taskMap.set(task.nodeName, task));

        Object.keys(TaskStatusEnum).forEach(status => sArr.push({
          status,
          count: status === TaskStatusEnum.INIT ? (graph.value?.getAsyncTaskNodeCount() - taskMap.size) : 0,
        }));

        taskMap.forEach(({ status }: ITaskExecutionRecordVo) => {
          const s = sArr.find(item => item.status === status);
          if (s) {
            s.count += 1;
          }
        });

        return sArr;
      }),
      async changeViewMode(mode: ViewModeEnum) {
        dslMode.value = mode === ViewModeEnum.YAML;
        emit('change-view-mode', mode);
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

  background-color: #FFFFFF;
  position: relative;
  // height: calc(100% - 60px);
  height: 100%;

  .task-states {
    position: absolute;
    z-index: 1;
    bottom: 22px;
    left: 44px;
    background-color: rgba(255, 255, 255, 0.6);

    > div + div {
      margin-left: 16px;
    }
  }

  .canvas-container {
    position: relative;
    // height: calc(100% - 60px);
    height: 100%;
    background-color: #ffffff;
    .borderBottom {
      border-bottom: 1px solid #E6EBF2;
    }
    .view-mode {
      position: absolute;
      top: 0;
      left: 0;
      padding: 0 30px;
      height: 60px;
      width: 100%;
      box-sizing: border-box;

      .float-view-mode {
        position: absolute;
        display: flex;
        z-index: 100;
        top: 0;
        left: 30px;
        height: 60px;
        .tab {
          text-align: center;
          width: 40px;
          line-height: 59px;
          font-size: 16px;
          margin-right: 55px;
          cursor: pointer;
          &.select {
            color: #096DD9;
            border-bottom: 2px solid #096DD9;
          }
          &:hover {
            color: #096DD9;
          }
        }
      }
      .float-param-log {
        position: absolute;
        display: flex;
        z-index: 100;
        top: 0;
        right: 30px;
        height: 60px;
        .jm-icon-workflow-param {
          margin-top: 12px;
          margin-right: 20px;
          width: 34px;
          height: 34px;
          background-color: #fff;
          border: 1px solid #CAD6EE;
          border-radius: 2px;
          font-size: 24px;
          cursor: pointer;
          &:hover {
            color: #096DD9;
          }
        }
        .btn {
          color: #096DD9;
          width: 96px;
          height: 36px;
          line-height: 36px;
          margin-top: 12px;
          text-align: center;
          border: 1px solid #CAD6EE;
          border-radius: 2px;
          margin-left: 20px;
          cursor: pointer;
        }
      }
    }
    .canvas {
      position: relative;
      min-height: 300px;
  
      .g6-tooltip {
        padding: 5px;
        font-size: 14px;
        font-weight: 400;
        color: #FFFFFF;
        line-height: 22px;
  
        background-color: rgba(51, 51, 51, 0.75);
        box-shadow: 0 9px 28px 8px rgba(51, 51, 51, 0.06), 0 6px 16px 0 rgba(51, 51, 51, 0.08);
        border-radius: 2px;
      }
    }
    .dsl-editor-container {
      position: absolute;
      top: 0;
      left: 0;
      width: calc(100% - 60px);
      padding: 80px 30px 30px;
      height: calc(100% - 110px);
    }
  }
}
</style>