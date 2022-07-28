<template>
  <div class="jm-workflow-detail-record-info">
    <div>启动时间：<span>{{ datetimeFormatter(record.startTime) }}</span></div>
    <div class="vertical-divider"></div>
    <div>
      {{ isSuspended? '挂起':'执行' }}时长：
    </div>
    <div class="record-time" :class="{uninit: record.status==='' || record.status===WorkflowExecutionRecordStatusEnum.INIT}">
      <jm-timer v-if="isSuspended" :start-time="record.suspendedTime"/>
      <jm-timer v-else :start-time="record.startTime" :end-time="record.endTime"/>
    </div>
    <div class="vertical-divider"></div>
    <div>状态：<span class="status" :class="{[(record.status || WorkflowExecutionRecordStatusEnum.INIT).toLowerCase()]: true}">{{ statusTranslate(record.status) }}</span></div>
    <button v-if="checkWorkflowRunning(record.status, false)" @click="handleTerminate" class="jm-icon-button-stop terminate-button">终止</button>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, PropType } from 'vue';
import { IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { datetimeFormatter } from '@/utils/formatter';
import { WorkflowExecutionRecordStatusEnum } from '@/api/dto/enumeration';
import { computed } from '@vue/reactivity';
import { RecordInfo, statusTranslate } from '../../model/record-info';
import { checkWorkflowRunning } from '../../model/util/workflow';
export default defineComponent({
  props: {
    record: {
      type: Object as PropType<IWorkflowExecutionRecordVo>,
      required: true,
    },
  },
  emits: ['terminate'],
  setup(props, { emit }) {
    const { proxy } = getCurrentInstance() as any;
    const isSuspended = computed(()=>props.record.status === WorkflowExecutionRecordStatusEnum.SUSPENDED);
    const recordInfo = new RecordInfo(props.record.id);
    return {
      datetimeFormatter,
      isSuspended,
      checkWorkflowRunning,
      statusTranslate,
      WorkflowExecutionRecordStatusEnum,
      handleTerminate() {
        proxy.$confirm('确定要终止吗?', '终止项目执行', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info',
        }).then(async () => {
          if (!props.record) {
            return;
          }
          try {
            await recordInfo.terminate(props.record.id);
            proxy.$success('终止成功');
            // 刷新 list graph
            emit('terminate');
          } catch (error) {
            proxy.$throw(error, proxy);
          }
        }).catch(() => {});
      },
    };
  },
});
</script>

<style lang="less">
@import '../../vars.less';
.jm-workflow-detail-record-info {
  position: absolute;
  top: 139px;
  left: 50%;
  width: 700px;
  margin-left: -350px;
  display: flex;
  align-items: center;
  justify-content: center;
  height: @record-info-height;
  color: @shallow-black-color;
  z-index: 3;
  font-size: 16px;
  & > div > span {
    color: @default-black-color;
  }
  & > div > span.status {
    margin-right: 14px;
    &.init {
      color: #979797;
    }

    &.running {
      color: #10c2c2;
    }

    &.finished {
      color: #3ebb03;
    }

    &.terminated {
      color: #cf1524;
    }

    &.suspended {
      color: #7986cb;
    }
  }
  .vertical-divider {
    width: 1px;
    height: 14px;
    margin: 0 20px;
    background-color:#CDD1E3;
  }
  .record-time {
    width: 88px;
    height: 40px;
    line-height: 40px;
    color: @default-black-color;
    &.uninit {
      width: 16px;
    }
  }
  .terminate-button{
    padding-right: 5px;
    width: 82px;
    height: 36px;
    background: @default-background-color;
    border-radius: 2px;
    border: 1px solid rgb(202 214 238 / 54%);
    font-size: 14px;
    color: #116ed2;
    cursor: pointer;

    &::before {
      font-size: 16px;
      margin-right: 6px;
    }

    &:hover {
      background-color: #EFF7FF;
      color: @primary-color;
    }
  }
}
</style>