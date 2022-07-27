<template>
  <div class="jm-workflow-detail-graph-panel" ref="workflowRef">
    <jm-workflow-viewer
      :dsl="dslSourceCode"
      :trigger-type="record.triggerType"
      :node-infos="nodeInfos"
      :tasks="taskRecords"
      :fullscreenRef="workflowRef"
      :entry="entry"
      @click-process-log="openProcessLog"
      @click-task-node="openTaskLog"
      @click-webhook-node="openWebhookLog"
      @click-param-log="openParamLog"
      @change-view-mode="viewMode=>$emit('change-view-mode', viewMode)"
      :viewMode="viewMode"
      :hasGlobalParam="Boolean(globalParams?.length)"
    />
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
import { computed, defineComponent, onBeforeUnmount, onMounted, onUpdated, PropType, ref } from 'vue';
import { GraphPanel } from '../../model/graph-panel';
import ProcessLog from '../right/process-log.vue';
import TaskLog from '../right/task-log.vue';
import WebhookLog from '../right/webhook-log.vue';
import ParamLog from '../right/param-log.vue';
import { ignoreTask, retryTask } from '@/api/workflow-execution-record';
import { ViewModeEnum } from '@/api/dto/enumeration';

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
    let graphPanel:GraphPanel;
    // record 页面变化引起数据变化 函数执行
    onUpdated(()=>{
      if (recordStatus.value !== props.record.status) {
        recordStatus.value = props.record.status;
        graphPanel.reflushGparam(props.record);
      }
      if (triggerId.value === props.record.triggerId) {
        return;
      }
      triggerId.value = props.record.triggerId;
      graphPanel.reflushGparam(props.record);
      graphPanel.resetSuspended();
      graphPanel.getDslAndNodeinfos();
      graphPanel.getTaskRecords();
      graphPanel.getGlobalParams();
    });
    onMounted(async ()=>{
      // 赋值 dslSourceCode nodeInfos
      const dslCallbackFn = (dsl: string, node: INodeDefVo[]) => {
        dslSourceCode.value = dsl;
        nodeInfos.value = node;
      };
      // 赋值 taskRecords
      const taskCallbackFn = (tasks: ITaskExecutionRecordVo[]) => {
        taskRecords.value = tasks;
      };
      // 赋值 globalParams
      const globalParamsCallbackFn = (globalParam: IGlobalParamseterVo[]) => {
        globalParams.value = globalParam;
      };
      graphPanel = await new GraphPanel(gparam.value, dslCallbackFn, taskCallbackFn, globalParamsCallbackFn);
      graphPanel.listen();
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
        graphPanel.getDslAndNodeinfos();
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