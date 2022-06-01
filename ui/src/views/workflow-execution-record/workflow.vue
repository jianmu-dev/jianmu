<template>
  <div class="workflow-execution-record-workflow" ref="workflowRef">
    <jm-workflow-viewer
      :dsl="dslSourceCode"
      :trigger-type="data.record?.triggerType"
      :node-infos="nodeInfos"
      :tasks="data.taskRecords"
      :fullscreenRef="workflowRef"
      @click-process-log="openProcessLog"
      @click-task-node="openTaskLog"
      @click-webhook-node="openWebhookLog"
    />
    <jm-drawer
      title="查看任务执行日志"
      :size="850"
      v-model="taskLogForm.drawerVisible"
      direction="rtl"
      destroy-on-close
    >
      <task-log :dsl="dslSourceCode" :business-id="taskLogForm.id" :tab-type="taskLogForm.tabType"/>
    </jm-drawer>
    <jm-drawer
      title="查看流程日志"
      :size="850"
      v-model="processLogDrawer"
      direction="rtl"
      destroy-on-close
    >
      <process-log/>
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
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, inject, ref } from 'vue';
import { useStore } from 'vuex';
import { namespace } from '@/store/modules/workflow-execution-record';
import { IOpenTaskLogForm, IOpenWebhookLogForm, IState } from '@/model/modules/workflow-execution-record';
import TaskLog from '@/views/workflow-execution-record/task-log.vue';
import ProcessLog from '@/views/workflow-execution-record/process-log.vue';
import WebhookLog from '@/views/workflow-execution-record/webhook-log.vue';
import { ITaskExecutionRecordVo, IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { useRouter } from 'vue-router';
import { INodeDefVo, IProjectDetailVo } from '@/api/dto/project';
import { NodeToolbarTabTypeEnum } from '@/components/workflow/workflow-viewer/model/data/enumeration';
import { IRootState } from '@/model';
import { ignoreTask, retryTask } from '@/api/workflow-execution-record';

export default defineComponent({
  components: { TaskLog, ProcessLog, WebhookLog },
  setup() {
    const loadData = inject('loadData') as (refreshing?: boolean) => void;
    const workflowRef = ref<HTMLElement>();
    const router = useRouter();
    const store = useStore();
    const rootState = store.state as IRootState;
    const state = store.state[namespace] as IState;
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

    const data = computed<{
      project?: IProjectDetailVo;
      allRecords: IWorkflowExecutionRecordVo[];
      record?: IWorkflowExecutionRecordVo;
      taskRecords: ITaskExecutionRecordVo[];
    }>(() => state.recordDetail);

    return {
      workflowRef,
      data,
      dslSourceCode: computed<string | undefined>(
        () => state.recordDetail.recordDsl,
      ),
      nodeInfos: computed<INodeDefVo[]>(() => state.recordDetail.nodeInfos),
      taskLogForm,
      webhookLogForm,
      close: () => {
        router.push(rootState.fromRoute.fullPath);
      },
      openTaskLog: async (nodeId: string, tabType: NodeToolbarTabTypeEnum) => {
        if ([NodeToolbarTabTypeEnum.RETRY, NodeToolbarTabTypeEnum.IGNORE].includes(tabType)) {
          const { nodeName } = data.value.taskRecords.find(({ businessId }) => businessId === nodeId)!;
          await (tabType === NodeToolbarTabTypeEnum.RETRY ? retryTask : ignoreTask)(data.value.record!.id, nodeName);
          await loadData();
          return;
        }

        taskLogForm.value.drawerVisible = true;
        taskLogForm.value.id = nodeId;
        taskLogForm.value.tabType = tabType;
      },
      processLogDrawer,
      openProcessLog: () => {
        processLogDrawer.value = true;
      },
      openWebhookLog: (nodeId: string, tabType: NodeToolbarTabTypeEnum) => {
        webhookLogForm.value.drawerVisible = true;
        webhookLogForm.value.nodeName = nodeId;
        webhookLogForm.value.tabType = tabType;
        webhookLogForm.value.triggerId = data.value.record?.triggerId;
        webhookLogForm.value.triggerType = data.value.record?.triggerType;
      },
    };
  },
});
</script>

<style scoped lang="less">
.workflow-execution-record-workflow {
  background-color: #ffffff;
  //height: calc(100vh - 384px);

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
