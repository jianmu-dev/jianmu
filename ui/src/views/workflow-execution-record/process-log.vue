<template>
  <div class="workflow-execution-record-process-log">
    <div class="basic-section">
      <div class="item">
        <div class="param-key">流程名称</div>
        <div class="param-value">
          <jm-text-viewer :value="workflowName" :tip-append-to-body="false"/>
        </div>
      </div>
      <div class="item">
        <div class="param-key">启动时间</div>
        <div class="param-value">
          <jm-text-viewer :value="datetimeFormatter(process.startTime)" :tip-append-to-body="false"/>
        </div>
      </div>
      <div class="item">
        <div class="param-key">最后完成时间</div>
        <div class="param-value">
          <jm-text-viewer :value="datetimeFormatter(process.endTime)" :tip-append-to-body="false"/>
        </div>
      </div>
      <div class="item">
        <div class="param-key ">{{ isSuspended ? '挂起时长' : '执行时长' }}</div>
        <div class="param-value">
          <jm-timer v-if="isSuspended" :start-time="process.suspendedTime" :tip-append-to-body="false"></jm-timer>
          <jm-timer v-else :start-time="process.startTime" :end-time="process.endTime"
                    :tip-append-to-body="false"></jm-timer>
        </div>
      </div>
      <div class="item">
        <div class="param-key">流程实例ID</div>
        <div class="param-value">
          <jm-text-viewer :value="process.id" :tip-append-to-body="false"/>
        </div>
      </div>
      <div class="item">
        <div class="param-key">流程版本号</div>
        <div class="param-value">
          <jm-text-viewer :value="process.workflowVersion" :tip-append-to-body="false"/>
        </div>
      </div>
    </div>

    <div class="process-log">
      <div class="log">
        <div class="loading" v-if="executing">
          <jm-button type="text" size="small" :loading="executing">
            加载中...
          </jm-button>
        </div>
        <jm-log-viewer :filename="`${process.name}.txt`" :value="processLog"/>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onBeforeMount, onBeforeUnmount, ref } from 'vue';
import { useStore } from 'vuex';
import { namespace } from '@/store/modules/workflow-execution-record';
import { IState } from '@/model/modules/workflow-execution-record';
import { IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { datetimeFormatter, executionTimeFormatter } from '@/utils/formatter';
import { checkProcessLog, fetchProcessLog } from '@/api/view-no-auth';
import sleep from '@/utils/sleep';
import { TriggerTypeEnum, WorkflowExecutionRecordStatusEnum } from '@/api/dto/enumeration';
import { HttpError, TimeoutError } from '@/utils/rest/error';

export default defineComponent({
  setup() {
    const state = useStore().state[namespace] as IState;
    const process = computed<IWorkflowExecutionRecordVo>(() => (state.recordDetail.record || {
      id: '',
      serialNo: '',
      name: '',
      workflowRef: '',
      workflowVersion: '',
      startTime: '',
      status: '',
      triggerId: '',
      triggerType: TriggerTypeEnum.MANUAL,
    }) as IWorkflowExecutionRecordVo);
    const executing = computed<boolean>(() => WorkflowExecutionRecordStatusEnum.RUNNING === (process.value.status as WorkflowExecutionRecordStatusEnum));
    const executionTime = computed<string>(() => executionTimeFormatter(process.value.startTime, process.value.endTime, executing.value));
    const isSuspended = computed<boolean>(() => WorkflowExecutionRecordStatusEnum.SUSPENDED === (process.value.status as WorkflowExecutionRecordStatusEnum));
    const suspendedTime = computed<string>(() =>
      executionTimeFormatter(process.value.suspendedTime, undefined, true));
    const processLog = ref<string>('');

    let terminateLogLoad = false;
    const loadLog = async (retry: number) => {
      if (terminateLogLoad) {
        console.debug('组件已卸载，终止加载流程');
        return;
      }

      try {
        const headers: any = await checkProcessLog(process.value.triggerId);
        const contentLength = +headers['content-length'] as number;

        if (contentLength > processLog.value.length) {
          // 存在更多日志
          processLog.value = await fetchProcessLog(process.value.triggerId);
        } else {
          console.debug('暂无更多日志');
        }
      } catch (err) {
        if (err instanceof TimeoutError) {
          // 忽略超时错误
          console.warn(err.message);
        } else if (err instanceof HttpError) {
          const { response } = err as HttpError;

          if (response && response.status !== 502) {
            throw err;
          }

          // 忽略错误
          console.warn(err.message);
        }
      }

      if (!executing.value) {
        console.debug('流程已完成，终止获取日志');

        return;
      }

      retry++;

      console.debug(`3秒后重试第${retry}次`);
      await sleep(3000);

      await loadLog(retry);
    };

    // 初始化
    onBeforeMount(() => loadLog(0));

    onBeforeUnmount(() => (terminateLogLoad = true));
    return {
      workflowName: state.recordDetail.record?.name,
      process,
      executing,
      executionTime,
      isSuspended,
      suspendedTime,
      datetimeFormatter,
      processLog,
    };
  },
});
</script>
<style scoped lang="less">
.workflow-execution-record-process-log {
  font-size: 14px;
  color: #333333;
  margin-bottom: 25px;
  background-color: #FFFFFF;
  height: 100%;

  .basic-section {
    margin: 20px;
    //padding: 16px 20px 0;
    padding: 16px 0 0 20px;
    display: flex;
    justify-content: space-between;
    box-shadow: 0 0 8px 0 #9EB1C5;

    > div {
      margin-bottom: 16px;

      &.item {
        flex: 1;
      }

      .param-key {
        color: #6B7B8D;
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
    border: 1px solid #EEF0F7;

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
