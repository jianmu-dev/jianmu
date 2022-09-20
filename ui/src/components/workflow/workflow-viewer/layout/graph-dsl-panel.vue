<template>
  <!-- x6/g6容器 -->
  <div class="canvas" ref="container"></div>
  <!-- jm-dsl-editor父容器 -->
  <div v-if="dslMode" class="dsl-editor-container">
    <jm-dsl-editor :value="visibleDsl" readonly/>
  </div>
</template>
<script lang="ts">
import { TriggerTypeEnum } from '@/api/dto/enumeration';
import { ITaskExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { fetchWorkflow } from '@/api/view-no-auth';
import { defineComponent, onMounted, onUpdated, PropType, ref } from 'vue';
import { INodeMouseoverEvent } from '../model/data/common';
import { WorkflowGraph } from '../model/workflow-graph';
export default defineComponent({
  props: {
    dslMode: {
      type: Boolean,
      required: true,
    },
    readonly: {
      type: Boolean,
      required: true,
    },
    tasks: {
      type: Array as PropType<ITaskExecutionRecordVo[]>,
      default: () => [],
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
  },
  emits: ['sync-graph-data', 'sync-graph-loading', 'mouseenter-node'],
  setup(props, { emit }) {
    const container = ref<HTMLElement>();
    let workflowGraph:WorkflowGraph;
    const visibleDsl = ref<string>();
    // record版本号
    const workflowVersion = ref(props.workflowVersion);
    // triggerType
    const triggerType = ref(props.triggerType);
    // 任务状态字符串
    const taskStatus = ref<string>('');
    // 实例化workflowGraph并传向父组件
    const getGraphData = async () => {
      // 开启画布加载
      emit('sync-graph-loading');
      // console.log('获取新画布', props.workflowVersion);
      const { dslText: dsl, nodes } = await fetchWorkflow(props.workflowRef, props.workflowVersion);
      const nodeInfos = nodes.filter(({ metadata }) => metadata).map(({ metadata }) => JSON.parse(metadata as string));
      workflowGraph = new WorkflowGraph(dsl, nodeInfos, triggerType.value, container.value!, (evt: INodeMouseoverEvent) => {
        emit('mouseenter-node', evt);
      });
      visibleDsl.value = workflowGraph.visibleDsl;
      emit('sync-graph-data', workflowGraph);
    };
    // 执行动画
    const executeAnimation = () => {
      // 状态不同，执行动画(''时不用更新)
      if (taskStatus.value && taskStatus.value !== props.tasks.map(e => e.status).join()) {
        // console.log('props.tasks', props.tasks.map(e=>e.status));
        // 运行时动画
        workflowGraph.updateNodeStates(props.tasks);
      }
      taskStatus.value = props.tasks.map(e => e.status).join();
    };
    let lastedTaskStatus = 0;
    // 切换画布实例
    const recordChangeGraph = async () => {
      // 状态是否为INIT
      // console.log('noInit', taskStatus.value.length, lastedTaskStatus);
      // version 和 triggerType 都没变化并且非INIT状态(taskStatus.value.length>0)且上一次字符长度非0 返回
      if (workflowVersion.value === props.workflowVersion && triggerType.value === props.triggerType && taskStatus.value.length && lastedTaskStatus) {
        return;
      }
      // 记录上一次状态字符长度
      lastedTaskStatus = taskStatus.value.length;
      workflowVersion.value = props.workflowVersion;
      triggerType.value = props.triggerType;
      // 获取新画布前 销毁旧画布
      workflowGraph && workflowGraph.destroy();
      // 获取新画布
      getGraphData();
    };
    onUpdated(() => {
      // 执行动画
      executeAnimation();
      // 切换record 重新生成画布
      recordChangeGraph();
    });
    // 保证整个视图都渲染完毕，才能确定图的宽高
    onMounted(() => {
      getGraphData();
    });
    return {
      container,
      visibleDsl,
    };
  },
});
</script>

<style lang="less">
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
  z-index: 2;
  top: 0;
  left: 0;
  width: calc(100% - 60px);
  padding: 60px 30px 30px;
  height: calc(100% - 90px);
  background-color: #ffffff;
}
</style>