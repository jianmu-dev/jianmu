<template>
  <div class="workflow-execution-record-process-log">
    <div class="basic-section">
      <div class="item">
        <div class="param-key">流程名称</div>
        <div class="param-value">
          <jm-text-viewer :value="workflowName" :tip-append-to-body="false" />
        </div>
      </div>
      <div class="item">
        <div class="param-key">启动时间</div>
        <div class="param-value">
          <jm-text-viewer :value="datetimeFormatter(process.startTime)" :tip-append-to-body="false" />
        </div>
      </div>
      <div class="item">
        <div class="param-key">完成时间</div>
        <div class="param-value">
          <jm-text-viewer :value="datetimeFormatter(process.endTime)" :tip-append-to-body="false" />
        </div>
      </div>
      <div class="item">
        <div class="param-key">{{ isSuspended ? '挂起时长' : '执行时长' }}</div>
        <div class="param-value">
          <jm-timer v-if="isSuspended" :start-time="process.suspendedTime" :tip-append-to-body="false" />
          <jm-timer v-else :start-time="process.startTime" :end-time="process.endTime" :tip-append-to-body="false" />
        </div>
      </div>
      <div class="item">
        <div class="param-key">流程实例ID</div>
        <div class="param-value">
          <jm-text-viewer :value="process.id" :tip-append-to-body="false" />
        </div>
      </div>
      <div class="item">
        <div class="param-key">流程版本号</div>
        <div class="param-value">
          <jm-text-viewer :value="process.workflowVersion" :tip-append-to-body="false" />
        </div>
      </div>
    </div>

    <div class="process-log">
      <div class="log">
        <div class="loading" v-if="executing">
          <jm-button type="text" size="small" :loading="executing"> 加载中... </jm-button>
        </div>
        <jm-log-viewer
          :filename="`${process.name}.txt`"
          :url="`/view/logs/workflow/subscribe/${process.triggerId}?size=`"
          :download="download"
          v-model:more="moreLog"
          v-if="process.triggerId"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, ref } from 'vue';
import { useStore } from 'vuex';
import { namespace } from '@/store/modules/workflow-execution-record';
import { IState } from '@/model/modules/workflow-execution-record';
import { IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { datetimeFormatter } from '@/utils/formatter';
import { TriggerTypeEnum, WorkflowExecutionRecordStatusEnum } from '@/api/dto/enumeration';
import { downloadWorkflowLogs, randomWorkflowLogs } from '@/api/workflow-execution-record';

export default defineComponent({
  setup() {
    const { proxy } = getCurrentInstance() as any;
    const state = useStore().state[namespace] as IState;
    const process = computed<IWorkflowExecutionRecordVo>(
      () =>
        (state.recordDetail.record || {
          id: '',
          serialNo: '',
          name: '',
          workflowRef: '',
          workflowVersion: '',
          startTime: '',
          status: '',
          triggerId: '',
          triggerType: TriggerTypeEnum.MANUAL,
        }) as IWorkflowExecutionRecordVo,
    );
    const executing = computed<boolean>(
      () => WorkflowExecutionRecordStatusEnum.RUNNING === (process.value.status as WorkflowExecutionRecordStatusEnum),
    );
    const isSuspended = computed<boolean>(
      () => WorkflowExecutionRecordStatusEnum.SUSPENDED === (process.value.status as WorkflowExecutionRecordStatusEnum),
    );
    const processLog = ref<string>('');
    const moreLog = ref<boolean>(false);

    // 下载日志
    const download = async () => {
      try {
        return await downloadWorkflowLogs(process.value.triggerId);
      } catch (err) {
        proxy.$throw(err, proxy);
      }
    };

    return {
      workflowName: state.recordDetail.record?.name,
      process,
      executing,
      isSuspended,
      datetimeFormatter,
      processLog,
      WorkflowExecutionRecordStatusEnum,
      download,
      moreLog,
    };
  },
});
</script>
<style scoped lang="less">
.workflow-execution-record-process-log {
  font-size: 14px;
  color: #333333;
  margin-bottom: 25px;
  background-color: #ffffff;
  height: 100%;

  .basic-section {
    margin: 20px;
    //padding: 16px 20px 0;
    padding: 16px 0 0 20px;
    display: flex;
    justify-content: space-between;
    box-shadow: 0 0 8px 0 #9eb1c5;

    > div {
      margin-bottom: 16px;

      &.item {
        flex: 1;
      }

      .param-key {
        color: #6b7b8d;
        margin-bottom: 8px;
      }

      .param-value,
      ::v-deep(.jm-state) {
        color: #082340;
      }

      .param-value {
        line-height: 25px;
        margin-right: 10px;
      }
    }
  }

  .process-log {
    margin: 0 20px;
    border: 1px solid #eef0f7;

    .log {
      margin: 16px;
      position: relative;
      height: calc(100vh - 250px);

      .loading {
        position: absolute;
        top: 0;
        right: 0;
        z-index: 1;

        ::v-deep(.el-button) {
          padding-left: 5px;
          padding-right: 5px;
        }
      }
    }
  }
}
</style>
