<template>
  <jm-scrollbar style="height: 70px">
    <div class="jm-workflow-detail-record-list">
      <div
        class="tab"
        :class="{
          [(record.status || WorkflowExecutionRecordStatusEnum.INIT).toLowerCase()]: true,
          [record.triggerId === param.triggerId ? 'selected' : 'unselected']: true,
        }"
        v-for="(record, i) in allRecords"
        :key="i"
        @click="handleChange(record)"
      >
        <!-- param.triggerId===undefined -->
        <div v-if="record.triggerId === param.triggerId" class="left-horn" />
        <div v-if="record.triggerId === param.triggerId" class="right-horn" />
        <div class="label">{{ record.serialNo || '-' }}</div>
      </div>
    </div>
  </jm-scrollbar>
  <div
    class="record-list-bottom-line"
    v-if="allRecords.length"
    :class="{ [currentRecordStatus.toLowerCase()]: true }"
  ></div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, onUpdated, PropType, ref } from 'vue';
import { IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { IRecordListParam } from '../../model/data/common';
import { RecordList } from '../../model/record-list';
import { IProjectDetailVo } from '@/api/dto/project';
import { IEventType, WorkflowExecutionRecordStatusEnum } from '@/api/dto/enumeration';
import { IWorkflowInstanceStatusUpdatedEvent } from '@/api/event/workflow-execution-record';
export default defineComponent({
  props: {
    param: {
      type: Object as PropType<IRecordListParam>,
      required: true,
    },
    project: {
      type: Object as PropType<IProjectDetailVo>,
      required: true,
    },
    event: {
      type: Object as PropType<IWorkflowInstanceStatusUpdatedEvent>,
    },
  },
  emits: ['change-record'],
  setup(props, { emit }) {
    // 所有record数据列表
    const allRecords = ref<IWorkflowExecutionRecordVo[]>([]);
    // RecordList组件实例
    let recordList: RecordList;
    // 更改当前record
    const handleChange = (record: IWorkflowExecutionRecordVo) => {
      emit('change-record', record);
    };
    // 当前record的状态
    const currentRecordStatus = computed(() => {
      return (
        allRecords.value.find(e => e.triggerId === props.param.triggerId)?.status ||
        WorkflowExecutionRecordStatusEnum.INIT
      );
    });
    // 当前record的id
    const currentRecordId = computed(() => {
      return allRecords.value.find(e => e.triggerId === props.param.triggerId)?.id || '';
    });
    const event = ref<IWorkflowInstanceStatusUpdatedEvent | undefined>(props.event);
    onUpdated(() => {
      const eString = JSON.stringify({ id: event.value?.id, status: event.value?.status });
      const pString = JSON.stringify({ id: props.event?.id, status: props.event?.status });
      if (eString === pString) {
        return;
      }
      event.value = props.event;
      // 监听流程实例新增和修改事件
      if (event.value) {
        // 新增刷新全部
        if (event.value.eventName === IEventType.WorkflowInstanceCreatedEvent) {
          recordList.initAllRecords();
          // 更新某一条实例
        } else if (event.value.eventName === IEventType.WorkflowInstanceStatusUpdatedEvent) {
          // 找出变化那条下标
          const i = allRecords.value.findIndex(e => e.id === event.value?.id);
          // 找不到返回
          if (i === -1) {
            return;
          }
          // 不同状态下的时间处理
          const time = {} as any;
          switch (event.value.status) {
            case WorkflowExecutionRecordStatusEnum.SUSPENDED:
              time.suspendedTime = event.value.suspendedTime || new Date().toJSON();
              break;
            case WorkflowExecutionRecordStatusEnum.RUNNING:
              time.startTime = event.value.startTime || new Date().toJSON();
              break;
            case WorkflowExecutionRecordStatusEnum.TERMINATED:
              time.endTime = event.value.endTime || new Date().toJSON();
              break;
            case WorkflowExecutionRecordStatusEnum.FINISHED:
              time.endTime = event.value.endTime || new Date().toJSON();
              break;
            default:
              break;
          }
          // 改变那条status list组件变化
          allRecords.value.splice(i, 1, {
            ...allRecords.value[i],
            // 更改状态
            status: event.value.status,
            ...time,
          });
          // 如果当前停留的跟改变的是同一条(传递数据给info组件)
          if (currentRecordId.value === allRecords.value[i].id) {
            handleChange(allRecords.value[i]);
          }
        }
      }
    });

    onMounted(async () => {
      // 实例化RecordList 传入项目的workflowRef，传入回调->获取allRecords并主动选择当前record
      recordList = new RecordList(props.param.workflowRef, (data: IWorkflowExecutionRecordVo[]): void => {
        allRecords.value = data.length
          ? data
          : [
              {
                endTime: undefined,
                id: '',
                serialNo: 0,
                name: props.project.projectGroupName,
                startTime: '',
                status: '',
                triggerId: undefined as unknown,
                triggerType: props.project.triggerType,
                workflowRef: props.project.workflowRef,
                workflowVersion: props.project.workflowVersion,
              } as IWorkflowExecutionRecordVo,
          ];
        if (props.param.triggerId && allRecords.value.length) {
          handleChange(
            (allRecords.value.find(e => e.triggerId === props.param.triggerId) as IWorkflowExecutionRecordVo) ||
              allRecords.value[allRecords.value.length - 1],
          );
        } else if (allRecords.value.length) {
          handleChange(allRecords.value[0]);
        }
      });
      // 获取allRecords方法
      recordList.initAllRecords();
    });

    return {
      allRecords,
      handleChange,
      currentRecordStatus,
      WorkflowExecutionRecordStatusEnum,
      refreshRecordList() {
        recordList.initAllRecords();
      },
    };
  },
});
</script>

<style lang="less">
.jm-workflow-detail-record-list {
  display: inline-flex;
  padding: 18px 30px 0;
  background-color: #ffffff;
  .tab {
    position: relative;
    min-width: 50px;
    padding: 0 6px 0 4px;
    height: 48px;
    border-radius: 4px 4px 0px 0px;
    margin-right: 2px;
    color: #ffffff;
    .left-horn,
    .right-horn {
      position: absolute;
      bottom: 0;
      width: 2px;
      height: 2px;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        bottom: 0;
        width: 8px;
        height: 8px;
        overflow: hidden;
        background-color: #fff;
      }
    }

    .left-horn {
      left: -2px;

      &::before {
        right: 0;
        border-bottom-right-radius: 4px;
      }
    }

    .right-horn {
      right: -2px;

      &::before {
        left: 0;
        border-bottom-left-radius: 4px;
      }
    }

    &.init {
      &,
      .left-horn,
      .right-horn {
        background-color: #979797;
      }
    }

    &.running {
      &,
      .left-horn,
      .right-horn {
        background-color: #10c2c2;
      }
    }

    &.finished {
      &,
      .left-horn,
      .right-horn {
        background-color: #3ebb03;
      }
    }

    &.terminated {
      &,
      .left-horn,
      .right-horn {
        background-color: #cf1524;
      }
    }

    &.suspended {
      &,
      .left-horn,
      .right-horn {
        background-color: #7986cb;
      }
    }

    &.unselected {
      cursor: pointer;
      opacity: 0.55;

      .label {
        text-align: right;
        font-size: 20px;
      }
    }

    &.selected {
      cursor: default;
      height: 52px;

      .label {
        padding-left: 4px;
        line-height: 52px;
        font-size: 26px;
        text-align: center;

        &::before {
          content: '';
          position: absolute;
          left: 0px;
          top: 10px;
          width: 2px;
          height: 30px;
          background: rgba(255, 255, 255, 0.8);
          border-radius: 0 100px 100px 0;
          overflow: hidden;
        }
      }
    }
  }
}

.record-list-bottom-line {
  height: 5px;
  width: 100%;
  &.init {
    background-color: #979797;
  }

  &.running {
    background-color: #10c2c2;
  }

  &.finished {
    background-color: #3ebb03;
  }

  &.terminated {
    background-color: #cf1524;
  }

  &.suspended {
    background-color: #7986cb;
  }
}
</style>
