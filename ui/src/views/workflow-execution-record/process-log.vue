<template>
  <div class="workflow-execution-record-process-log">
    <div class="basic-section">
      <div>
        <div class="param-key">流程名称：</div>
        <jm-tooltip :content="workflowName" placement="bottom" effect="light">
          <div class="param-value">{{ workflowName }}</div>
        </jm-tooltip>
      </div>
      <div>
        <div class="param-key">启动时间：</div>
        <jm-tooltip :content="datetimeFormatter(process.startTime)" placement="bottom" effect="light">
          <div class="param-value">{{ datetimeFormatter(process.startTime) }}</div>
        </jm-tooltip>
      </div>
      <div>
        <div class="param-key">最后执行时间：</div>
        <jm-tooltip :content="datetimeFormatter(process.endTime)" placement="bottom" effect="light">
          <div class="param-value">{{ datetimeFormatter(process.endTime) }}</div>
        </jm-tooltip>
      </div>
      <div>
        <div class="param-key ">执行时长：</div>
        <jm-tooltip :content="executionTime" placement="bottom" effect="light">
          <div class="param-value">{{ executionTime }}</div>
        </jm-tooltip>
      </div>
      <div>
        <div class="param-key">流程实例ID：</div>
        <jm-tooltip :content="process.id" placement="bottom" effect="light">
          <div class="param-value ellipsis">{{ process.id }}</div>
        </jm-tooltip>
      </div>
      <div>
        <div class="param-key">流程版本号：</div>
        <jm-tooltip :content="process.workflowVersion" placement="bottom" effect="light">
          <div class="param-value ellipsis">{{ process.workflowVersion }}</div>
        </jm-tooltip>
      </div>
    </div>

    <div class="process-log">
      <div class="log">
        <div class="loading" v-if="executing">
          <jm-button type="text" size="small" :loading="executing">
            加载中...
          </jm-button>
        </div>
        <jm-log-viewer id="process-log" :filename="`${process.name}.txt`" :value="processLog"/>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, onBeforeMount, onBeforeUnmount, ref } from 'vue';
import { useStore } from 'vuex';
import { namespace } from '@/store/modules/workflow-execution-record';
import { IState } from '@/model/modules/workflow-execution-record';
import { IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { datetimeFormatter, executionTimeFormatter } from '@/utils/formatter';
import { checkProcessLog, fetchProcessLog } from '@/api/view-no-auth';
import sleep from '@/utils/sleep';
import { WorkflowExecutionRecordStatusEnum } from '@/api/dto/enumeration';
import { adaptHeight, IAutoHeight } from '@/utils/auto-height';
import { HttpError, TimeoutError } from '@/utils/rest/error';

const autoHeights: {
  [key: string]: IAutoHeight;
} = {
  log: {
    elementId: 'process-log',
    offsetTop: 250,
  },
};

export default defineComponent({
  setup() {
    const { proxy } = getCurrentInstance() as any;
    const state = useStore().state[namespace] as IState;
    const process = computed<IWorkflowExecutionRecordVo>(() => state.recordDetail.record as IWorkflowExecutionRecordVo);
    const executing = computed<boolean>(() => WorkflowExecutionRecordStatusEnum.RUNNING === (process.value.status as WorkflowExecutionRecordStatusEnum));
    const executionTime = computed<string>(() => executionTimeFormatter(process.value.startTime, process.value.endTime, executing.value));
    const processLog = ref<string>('');

    proxy.$nextTick(() => adaptHeight(autoHeights.log));

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
      executionTime,
      datetimeFormatter,
      processLog,
      executing,
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
    padding: 16px 20px 0;
    display: flex;
    justify-content: space-between;
    box-shadow: 0 0 8px 0 #9EB1C5;

    .ellipsis {
      display: inline-block;
      width: 80px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      vertical-align: bottom;
      cursor: default;
    }

    > div {
      margin-bottom: 16px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      cursor: default;

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
      }
    }
  }

  .process-log {
    margin: 0 20px;
    border: 1px solid #EEF0F7;

    .log {
      margin: 16px;
      position: relative;

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
