<template>
  <div class="jm-workflow-detail-graph-panel" ref="workflowRef" v-loading="!reloadViewer">
    <jm-workflow-viewer
      v-if="reloadViewer"
      :dsl="dslSourceCode"
      :trigger-type="record.triggerType"
      :node-infos="nodeInfos"
      :tasks="taskRecords"
      :fullscreenRef="workflowRef"
      :entry="entry"
      :viewMode="viewMode"
      :hasGlobalParam="Boolean(globalParams?.length)"
      :showLogBtn="record.status!==''"
      @click-process-log="openProcessLog"
      @click-task-node="openTaskLog"
      @click-webhook-node="openWebhookLog"
      @click-param-log="openParamLog"
      @change-view-mode="viewMode=>$emit('change-view-mode', viewMode)"
    />
    <!-- :readonly="true" 不能加true 组件内部逻辑需要处理 -->
    <jm-drawer
      title="查看任务执行日志"
      :size="850"
      v-model="taskLogForm.drawerVisible"
      direction="rtl"
      destroy-on-close
    >
      <task-log :dsl="(dslSourceCode || '')" :business-id="taskLogForm.id" :tab-type="taskLogForm.tabType" :record="record" :taskRecords="taskRecords"/>
    </jm-drawer>
    <jm-drawer
      title="查看流程日志"
      :size="850"
      v-model="processLogDrawer"
      direction="rtl"
      destroy-on-close
    >
      <process-log :record="record"/>
    </jm-drawer>
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
    <jm-drawer
      title="查看全局参数"
      :size="810"
      v-model="paramLogDrawer"
      direction="rtl"
      destroy-on-close
    >
      <param-log :globalParams="globalParams||[]"></param-log>
    </jm-drawer>
  </div>
</template>

<script lang="ts">
import { IGlobalParamseterVo, INodeDefVo } from '@/api/dto/project';
import { ITaskExecutionRecordVo, IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { NodeToolbarTabTypeEnum } from '@/components/workflow/workflow-viewer/model/data/enumeration';
import { IOpenTaskLogForm, IOpenWebhookLogForm } from '@/model/modules/workflow-execution-record';
import { nextTick, computed, defineComponent, onBeforeUnmount, onMounted, onUpdated, PropType, ref } from 'vue';
import { GraphPanel } from '../../model/graph-panel';
import ProcessLog from '../right/process-log.vue';
import TaskLog from '../right/task-log.vue';
import WebhookLog from '../right/webhook-log.vue';
import ParamLog from '../right/param-log.vue';
import { ignoreTask, retryTask } from '@/api/workflow-execution-record';
import { ViewModeEnum, WorkflowExecutionRecordStatusEnum } from '@/api/dto/enumeration';

export default defineComponent({
  components: { ProcessLog, TaskLog, WebhookLog, ParamLog },
  props: {
    record: {
      type: Object as PropType<IWorkflowExecutionRecordVo>,
      required: true,
    },
    currentRecordStatus: {
      type: String,
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
  },
  emits: ['change-view-mode', 'trigger-refresh'],
  setup(props, { emit }) {
    const workflowRef = ref<HTMLElement>();
    const dslSourceCode = ref<string>();
    const nodeInfos = ref<INodeDefVo[]>();
    const taskRecords = ref<ITaskExecutionRecordVo[]>();
    const globalParams = ref<IGlobalParamseterVo[]>();
    const gparam = computed<IWorkflowExecutionRecordVo>(()=>({
      ...props.record,
    }));
    const triggerId = ref(props.record.triggerId);
    const recordStatus = ref(props.record.status);
    const reloadViewer = ref<boolean>(false);
    let graphPanel:GraphPanel;
    // 点击Record标识
    let ifClickRecord = false;
    // record 页面变化引起数据变化 函数执行
    onUpdated(async ()=>{
      if (recordStatus.value !== props.record.status) {
        recordStatus.value = props.record.status;
        graphPanel.refreshGparam(props.record);
      }
      if (triggerId.value === props.record.triggerId) {
        return;
      }
      // 打开点击Record标识
      ifClickRecord = true;
      triggerId.value = props.record.triggerId;
      graphPanel.refreshGparam(props.record);
      graphPanel.resetSuspended();
      (async () => {
        await graphPanel.getDslAndNodeinfos();
        await graphPanel.getTaskRecords();
      })();
      graphPanel.getGlobalParams();
    });
    // 仅record运行中、挂起状态(workflow)->不会复位=false
    const ifResetRefresh = (status: any): boolean=>{
      return ![
        WorkflowExecutionRecordStatusEnum.RUNNING,
        WorkflowExecutionRecordStatusEnum.SUSPENDED,
      ].includes(status);
    };
    onMounted(async ()=>{
      // 赋值 dslSourceCode nodeInfos
      const dslCallbackFn = (dsl: string, nodes: INodeDefVo[]) => {
        dslSourceCode.value = dsl;
        nodeInfos.value = nodes;
      };
      // 赋值 taskRecords
      const taskCallbackFn = (tasks: ITaskExecutionRecordVo[]) => {
        if (ifResetRefresh(props.currentRecordStatus) || ifClickRecord) {
          // 重置点击Record标识
          ifClickRecord = false;
          reloadViewer.value = false;
        }
        taskRecords.value = tasks;
        nextTick(()=>{
          reloadViewer.value = true;
        });
      };
      // 赋值 globalParams
      const globalParamsCallbackFn = (globalParam: IGlobalParamseterVo[]) => {
        globalParams.value = globalParam;
      };
      graphPanel = await new GraphPanel(gparam.value, dslCallbackFn, taskCallbackFn, globalParamsCallbackFn);
      graphPanel.listen();
      if (props.record.status === '') {
        reloadViewer.value = true;
      }
    });
    onBeforeUnmount(()=>{
      graphPanel.destroy();
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
      reloadViewer,
      openTaskLog: async (nodeId: string, tabType: NodeToolbarTabTypeEnum) => {
        if ([NodeToolbarTabTypeEnum.RETRY, NodeToolbarTabTypeEnum.IGNORE].includes(tabType)) {
          const nodeData = taskRecords.value&&taskRecords.value.find(({ businessId }) => businessId === nodeId)!;
          await (tabType === NodeToolbarTabTypeEnum.RETRY ? retryTask : ignoreTask)(props.record!.id, nodeData? nodeData.nodeName: 'null');
          // 重试 忽略
          graphPanel.refreshSuspended();
          // 传递给index 调用 recordListRefreshSuspended
          emit('trigger-refresh');
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
        console.log('刷新GraphPanel');
        await graphPanel.getDslAndNodeinfos();
        await graphPanel.getTaskRecords();
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