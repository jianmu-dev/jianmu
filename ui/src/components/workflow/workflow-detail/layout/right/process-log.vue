<template>
  <div class="workflow-execution-record-process-log">
    <div class="basic-section">
      <div class="item">
        <div class="param-key">流程名称</div>
        <div class="param-value">
          <jm-text-viewer :threshold="0" :value="record.name" :tip-append-to-body="false" />
        </div>
      </div>
      <div class="item">
        <div class="param-key">启动时间</div>
        <div class="param-value">
          <jm-text-viewer :threshold="0" :value="datetimeFormatter(record.startTime)" :tip-append-to-body="false" />
        </div>
      </div>
      <div class="item">
        <div class="param-key">最后完成时间</div>
        <div class="param-value">
          <jm-text-viewer :threshold="0" :value="datetimeFormatter(record.endTime)" :tip-append-to-body="false" />
        </div>
      </div>
      <div class="item">
        <div class="param-key">{{ isSuspended ? '挂起时长' : '执行时长' }}</div>
        <div class="param-value">
          <jm-timer v-if="isSuspended" :start-time="record.suspendedTime" :tip-append-to-body="false" />
          <jm-timer v-else :start-time="record.startTime" :end-time="record.endTime" :tip-append-to-body="false" />
        </div>
      </div>
      <div class="item">
        <div class="param-key">流程实例ID</div>
        <div class="param-value">
          <jm-text-viewer :threshold="0" :value="record.id" :tip-append-to-body="false" />
        </div>
      </div>
      <div class="item">
        <div class="param-key">流程版本号</div>
        <div class="param-value">
          <jm-text-viewer :threshold="0" :value="record.workflowVersion" :tip-append-to-body="false" />
        </div>
      </div>
    </div>

    <div class="process-log">
      <div class="log">
        <div class="loading" v-if="executing">
          <jm-button type="text" size="small" :loading="executing"> 加载中... </jm-button>
        </div>
        <jm-log-viewer
          :filename="`${record.name}.txt`"
          :url="`${API_PREFIX}/view/logs/workflow/subscribe/${record.triggerId}?size=`"
          :download="download"
          v-model:more="moreLog"
          v-if="record.triggerId"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, PropType, ref } from 'vue';
import { IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { datetimeFormatter } from '@/utils/formatter';
import { WorkflowExecutionRecordStatusEnum } from '@/api/dto/enumeration';
import { downloadWorkflowLogs } from '@/api/workflow-execution-record';
import { API_PREFIX } from '@/utils/constants';

export default defineComponent({
  props: {
    record: {
      type: Object as PropType<IWorkflowExecutionRecordVo>,
      required: true,
    },
  },
  setup(props) {
    const { proxy } = getCurrentInstance() as any;
    const process = computed<IWorkflowExecutionRecordVo>(() => props.record as IWorkflowExecutionRecordVo);
    const executing = computed<boolean>(
      () => WorkflowExecutionRecordStatusEnum.RUNNING === (props.record.status as WorkflowExecutionRecordStatusEnum),
    );
    const isSuspended = computed<boolean>(
      () => WorkflowExecutionRecordStatusEnum.SUSPENDED === (props.record.status as WorkflowExecutionRecordStatusEnum),
    );
    const processLog = ref<string>('');
    const moreLog = ref<boolean>(false);

    // 下载日志
    const download = async () => {
      try {
        return await downloadWorkflowLogs(props.record.triggerId);
      } catch (err) {
        proxy.$throw(err, proxy);
      }
    };

    return {
      API_PREFIX,
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
