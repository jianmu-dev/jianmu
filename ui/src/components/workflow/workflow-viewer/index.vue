<template>
  <div class="jm-workflow-viewer">
    <div v-if="!readonly && !dslMode && graph && tasks.length > 0" class="task-states">
      <task-state v-for="{status, count} in taskStates"
                  :key="status" :status="status" :count="count"
                  @mouseenter="graph?.highlightNodeState(status, true)"
                  @mouseleave="graph?.highlightNodeState(status, false)"
                  @change="graph?.refreshNodeStateHighlight(status)"/>
    </div>
    <toolbar v-if="graph" :readonly="readonly" :dsl-type="graph?.dslType" v-model:dsl-mode="dslMode" :zoom-value="zoom"
             :fullscreen-el="fullscreenEl"
             @click-process-log="clickProcessLog"
             @on-zoom="handleZoom"
             @on-fullscreen="handleFullscreen"
             @rotate="handleRotation"/>
    <node-toolbar v-if="!dslMode && nodeEvent"
                  :readonly="readonly"
                  :task-business-id="selectedTask?.businessId"
                  :task-status="selectedTask?.status"
                  :node-event="nodeEvent"
                  :zoom="zoom"
                  @node-click="clickNode"
                  @mouseout="handleNodeBarMouseout"/>
    <div v-show="!dslMode" class="canvas" ref="container"/>
    <jm-dsl-editor v-if="dslMode" :value="dsl" readonly/>
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
import { DslTypeEnum, TaskStatusEnum, TriggerTypeEnum } from '@/api/dto/enumeration';
import { GraphDirectionEnum, NodeToolbarTabTypeEnum, NodeTypeEnum } from './model/data/enumeration';
import { INodeMouseoverEvent } from './model/data/common';
import { sortTasks } from './model/util';
import { BaseGraph } from './model/base-graph';
import { WorkflowGraph } from './model/workflow-graph';

// 注册自定义g6元素
Object.values(import.meta.globEager('./shapes/**')).forEach(({ default: register }) => register(G6));

export default defineComponent({
  name: 'jm-workflow-viewer',
  components: { TaskState, Toolbar, NodeToolbar },
  props: {
    dsl: String,
    readonly: {
      type: Boolean,
      default: false,
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
  emits: ['click-task-node', 'click-webhook-node', 'click-process-log'],
  setup(props: any, { emit }: SetupContext) {
    const { proxy } = getCurrentInstance() as any;
    const container = ref<HTMLElement>();
    let workflowGraph: WorkflowGraph | undefined;
    const graph = ref<BaseGraph>();
    const nodeActionConfigured = ref<boolean>(false);
    const dslMode = ref<boolean>(false);
    const nodeEvent = ref<INodeMouseoverEvent>();
    const destroyNodeToolbar = () => {
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
      if (!graph.value) {
        if (!props.dsl || !props.triggerType || !container.value) {
          return;
        }

        workflowGraph = new WorkflowGraph(props.dsl, props.triggerType, props.nodeInfos, container.value as HTMLElement, direction);
        graph.value = workflowGraph.graph;

        updateZoom();
      }

      if (!nodeActionConfigured.value) {
        // 禁止多次配置
        // 配置节点行为
        graph.value!.configNodeAction(mouseoverNode);
        nodeActionConfigured.value = true;
      }

      // 更新状态
      graph.value!.updateNodeStates(props.tasks);
    };

    onBeforeUpdate(() => refreshGraph());

    // 保证整个视图都渲染完毕，才能确定图的宽高
    onMounted(() => proxy.$nextTick(() => refreshGraph()));
    onUnmounted(() => workflowGraph?.destroy());

    return {
      TaskStatusEnum,
      container,
      graph,
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
      handleNodeBarMouseout: (evt: any) => {
        let isOut = true;
        let tempObj = evt.relatedTarget || evt.toElement;
        // 10级以内可定位
        for (let i = 0; i < 10; i++) {
          if (!tempObj) {
            break;
          }

          if (tempObj.className === 'jm-workflow-viewer-node-toolbar') {
            isOut = false;
            break;
          }

          tempObj = tempObj.parentElement;
        }

        if (isOut) {
          destroyNodeToolbar();
        }
      },
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
      handleRotation: () => {
        if (!graph.value || graph.value?.dslType !== DslTypeEnum.WORKFLOW) {
          return;
        }

        const direction = graph.value!.getDirection() === GraphDirectionEnum.HORIZONTAL ?
          GraphDirectionEnum.VERTICAL : GraphDirectionEnum.HORIZONTAL;

        workflowGraph!.destroy();

        workflowGraph = undefined;
        graph.value = undefined;
        nodeActionConfigured.value = false;

        refreshGraph(direction);
      },
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

        const allTaskNodes = (graph.value?.getNodes() || [])
          .filter(node => node.type === NodeTypeEnum.ASYNC_TASK);

        Object.keys(TaskStatusEnum).forEach(status => sArr.push({
          status,
          count: status === TaskStatusEnum.INIT ? (allTaskNodes.length - taskMap.size) : 0,
        }));

        taskMap.forEach(({ status }: ITaskExecutionRecordVo) => {
          const s = sArr.find(item => item.status === status);
          if (s) {
            s.count += 1;
          }
        });

        return sArr;
      }),
    };
  },
});
</script>

<style lang="less">
.jm-workflow-viewer {
  background-color: #FFFFFF;
  position: relative;
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
}
</style>