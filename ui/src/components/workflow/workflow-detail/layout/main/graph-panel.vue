<template>
  <div class="jm-workflow-detail-graph-panel" ref="workflowRef">
    <jm-workflow-viewer
      :trigger-type="record.triggerType"
      :workflow-ref="record.workflowRef"
      :workflow-version="record.workflowVersion"
      :tasks="taskRecords"
      :fullscreenRef="workflowRef"
      :entry="entry"
      :viewMode="viewMode"
      @click-task-node="openTaskLog"
      @click-webhook-node="openWebhookLog"
      @async-dsl="dslText => (dslSourceCode = dslText)"
      @change-view-mode="viewMode => $emit('change-view-mode', viewMode)"
    />
    <!-- :readonly="true" 不能加true 组件内部逻辑需要处理 -->
    <!-- 查看任务执行日志 -->
    <jm-drawer
      title="查看任务执行日志"
      :size="850"
      v-model="taskLogForm.drawerVisible"
      direction="rtl"
      destroy-on-close
    >
      <task-log
        :dsl="dslSourceCode"
        :business-id="taskLogForm.id"
        :tab-type="taskLogForm.tabType"
        :record="record"
        :taskRecords="taskRecords"
      />
    </jm-drawer>
    <!-- 查看流程日志 -->
    <jm-drawer title="查看流程日志" :size="850" v-model="processLogDrawer" direction="rtl" destroy-on-close>
      <process-log :record="record" />
    </jm-drawer>
    <!-- 查看Webhook日志 -->
    <jm-drawer
      title="查看Webhook日志"
      :size="850"
      v-model="webhookLogForm.drawerVisible"
      direction="rtl"
      destroy-on-close
    >
      <webhook-log
        :node-name="webhookLogForm.nodeName"
        :trigger-id="webhookLogForm.triggerId"
        :trigger-type="webhookLogForm.triggerType"
        :tab-type="webhookLogForm.tabType"
        :record="record"
      />
    </jm-drawer>
    <!-- 查看全局参数 -->
    <jm-drawer title="查看全局参数" :size="810" v-model="paramLogDrawer" direction="rtl" destroy-on-close>
      <param-log :globalParams="globalParams"></param-log>
    </jm-drawer>
  </div>
</template>

<script lang="ts">
import { IGlobalParamseterVo, INodeDefVo } from '@/api/dto/project';
import { ITaskExecutionRecordVo, IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { NodeToolbarTabTypeEnum } from '@/components/workflow/workflow-viewer/model/data/enumeration';
import { IOpenTaskLogForm, IOpenWebhookLogForm } from '@/model/modules/workflow-execution-record';
import { computed, defineComponent, onMounted, onUpdated, PropType, ref } from 'vue';
import { GraphPanel } from '../../model/graph-panel';
import ProcessLog from '../right/process-log.vue';
import TaskLog from '../right/task-log.vue';
import WebhookLog from '../right/webhook-log.vue';
import ParamLog from '../right/param-log.vue';
import { ignoreTask, retryTask } from '@/api/workflow-execution-record';
import { ViewModeEnum, WorkflowExecutionRecordStatusEnum } from '@/api/dto/enumeration';
import { IAsyncTaskInstanceStatusUpdatedEvent } from '@/api/event/workflow-execution-record';

export default defineComponent({
  components: { ProcessLog, TaskLog, WebhookLog, ParamLog },
  props: {
    record: {
      type: Object as PropType<IWorkflowExecutionRecordVo>,
      required: true,
    },
    currentRecordStatus: {
      type: String as PropType<WorkflowExecutionRecordStatusEnum>,
      default: '',
    },
    entry: {
      type: Boolean,
      default: false,
    },
    viewMode: {
      type: String as PropType<ViewModeEnum>,
      default: ViewModeEnum.GRAPHIC,
    },
    event: {
      type: Object as PropType<IAsyncTaskInstanceStatusUpdatedEvent>,
    },
  },
  emits: ['change-view-mode', 'trigger-refresh', 'has-global-param'],
  setup(props, { emit }) {
    const workflowRef = ref<HTMLElement>();
    const dslSourceCode = ref<string>('');
    const nodeInfos = ref<INodeDefVo[]>();
    const taskRecords = ref<ITaskExecutionRecordVo[]>([]);
    const globalParams = ref<IGlobalParamseterVo[]>([]);
    const gparam = computed<IWorkflowExecutionRecordVo>(() => ({
      ...props.record,
    }));
    const triggerId = ref(props.record.triggerId);
    const recordStatus = ref(props.record.status);
    let graphPanel: GraphPanel;
    const timestamp = ref<number>(props.event?.timestamp || 0);
    // 节流
    let shortTimeGetOne = true;
    // record 页面变化引起数据变化 函数执行
    onUpdated(async () => {
      // 非手动更改当前实例状态
      if (recordStatus.value !== props.record.status) {
        recordStatus.value = props.record.status;
        graphPanel.refreshGparam(props.record);
      }
      // 异步任务实例状态更新事件
      if (props.event && props.event.timestamp !== timestamp.value) {
        // 记录当前触发事件时间戳(时间戳没改变即不会改变任务状态)
        timestamp.value = props.event.timestamp;
        // 修改任务数据和状态
        const i = taskRecords.value.findIndex(e => e.businessId === props.event!.id);
        // 找不到更新的那条任务就查询数据并返回
        if (i === -1 && shortTimeGetOne) {
          shortTimeGetOne = false;
          graphPanel.getTaskRecords();
          setTimeout(() => {
            shortTimeGetOne = true;
          }, 60000);
          return;
        }
        // 查询任务数据后还是找不到更新那条 即返回
        if (i === -1) return;
        taskRecords.value.splice(i, 1, {
          ...taskRecords.value[i],
          // 下面三行添加开始结束时间字段 解决左侧弹窗拿不到数据问题
          // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
          startTime: props.event.startTime!,
          endTime: props.event.endTime,
          status: props.event.status,
        });
        // 引起子组件 onUpdated 更新状态灯
        taskRecords.value = JSON.parse(JSON.stringify(taskRecords.value));
      }
      if (triggerId.value === props.record.triggerId) {
        return;
      }
      triggerId.value = props.record.triggerId;
      graphPanel.refreshGparam(props.record);
      await graphPanel.getTaskRecords();
      graphPanel.getGlobalParams();
    });
    onMounted(async () => {
      // 赋值 dslSourceCode nodeInfos
      const dslCallbackFn = (dsl: string, nodes: INodeDefVo[]) => {
        dslSourceCode.value = dsl;
        nodeInfos.value = nodes;
      };
      // 赋值 taskRecords
      const taskCallbackFn = (tasks: ITaskExecutionRecordVo[]) => {
        taskRecords.value = tasks;
      };
      // 赋值 globalParams
      const globalParamsCallbackFn = (globalParam: IGlobalParamseterVo[]) => {
        globalParams.value = globalParam;
        emit('has-global-param', Boolean(globalParam?.length));
      };
      graphPanel = await new GraphPanel(gparam.value, dslCallbackFn, taskCallbackFn, globalParamsCallbackFn);
    });
    const taskLogForm = ref<IOpenTaskLogForm>({
      drawerVisible: false,
      id: '',
      tabType: '',
    });
    const webhookLogForm = ref<IOpenWebhookLogForm>({
      drawerVisible: false,
      nodeName: '',
      tabType: '',
    });
    const processLogDrawer = ref<boolean>(false);
    const paramLogDrawer = ref<boolean>(false);
    return {
      workflowRef,
      dslSourceCode,
      nodeInfos,
      taskRecords,
      globalParams,
      processLogDrawer,
      paramLogDrawer,
      taskLogForm,
      webhookLogForm,
      openTaskLog: async (nodeId: string, tabType: NodeToolbarTabTypeEnum) => {
        if ([NodeToolbarTabTypeEnum.RETRY, NodeToolbarTabTypeEnum.IGNORE].includes(tabType)) {
          const nodeData = taskRecords.value && taskRecords.value.find(({ businessId }) => businessId === nodeId)!;
          await (tabType === NodeToolbarTabTypeEnum.RETRY ? retryTask : ignoreTask)(
            props.record!.id,
            nodeData ? nodeData.nodeName : 'null',
          );
          return;
        }
        taskLogForm.value.drawerVisible = true;
        taskLogForm.value.id = nodeId;
        taskLogForm.value.tabType = tabType;
      },
      openProcessLog: () => {
        processLogDrawer.value = true;
      },
      openWebhookLog: (nodeId: string, tabType: NodeToolbarTabTypeEnum) => {
        webhookLogForm.value.drawerVisible = true;
        webhookLogForm.value.nodeName = nodeId;
        webhookLogForm.value.tabType = tabType;
        webhookLogForm.value.triggerId = props.record?.triggerId;
        webhookLogForm.value.triggerType = props.record?.triggerType;
      },
      openParamLog() {
        paramLogDrawer.value = true;
      },
      async refreshGraphPanel() {
        graphPanel.getTaskRecords();
      },
    };
  },
});
</script>

<style scoped lang="less">
.jm-workflow-detail-graph-panel {
  background-color: #ffffff;
  height: calc(100vh - 140px);

  ::v-deep(.el-tabs__nav-scroll) {
    line-height: 46px;
  }

  ::v-deep(.el-drawer) {
    .el-drawer__header {
      > span::before {
        font-family: 'jm-icon-input';
        content: '\e803';
        margin-right: 10px;
        color: #6b7b8d;
        font-size: 20px;
        vertical-align: bottom;
      }
    }
  }
}
</style>
