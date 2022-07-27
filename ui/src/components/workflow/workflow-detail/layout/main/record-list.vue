<template>
  <jm-scrollbar style="height: 70px">
    <div class="jm-workflow-detail-record-list">
      <div
      class="tab"
      :class="{
        [(record.status || WorkflowExecutionRecordStatusEnum.INIT).toLowerCase()]: true,
        [record.triggerId === param.triggerId ? 'selected' : 'unselected']: true
      }"
      v-for="(record, i) in allRecords"
      :key="i"
      @click="handleChange(record)"
      >
        <div v-if="record.triggerId === param.triggerId" class="left-horn"/>
        <div v-if="record.triggerId === param.triggerId" class="right-horn"/>
        <div class="label">{{ record.serialNo || '-' }}</div>
      </div>
    </div>
  </jm-scrollbar>
  <div class="record-list-bottom-line" v-if="allRecords.length" :class="{[currentRecordStatus.toLowerCase()]: true}">
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onBeforeUnmount, onMounted, PropType, ref } from 'vue';
import { IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { IRecordListParam } from '../../model/data/common';
import { RecordList } from '../../model/record-list';
import { IProjectDetailVo } from '@/api/dto/project';
import { WorkflowExecutionRecordStatusEnum } from '@/api/dto/enumeration';
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
  },
  emits: ['change-record'],
  setup(props, { emit }) {
    // 所有record数据列表
    const allRecords = ref<IWorkflowExecutionRecordVo[]>([]);
    // RecordList组件实例
    let recordList:RecordList;
    // 更改当前record
    const handleChange = (record: IWorkflowExecutionRecordVo) => {
      console.log('选择record', record.triggerId);
      emit('change-record', record);
      recordList.resetSuspended();
    };
    // 当前record的状态
    const currentRecordStatus = computed(()=>{
      return allRecords.value.find(e=>e.triggerId===props.param.triggerId)?.status || WorkflowExecutionRecordStatusEnum.INIT;
    });
    onMounted(async ()=>{
      // 实例化RecordList 传入项目的workflowRef，传入回调->获取allRecords并主动选择当前record
      recordList = new RecordList(props.param.workflowRef, (data: IWorkflowExecutionRecordVo[]):void=>{
        allRecords.value = data.length? data:[{
          endTime: '',
          id: '',
          serialNo: 0,
          name: props.project.projectGroupName,
          startTime: '',
          status: '',
          triggerId: '',
          triggerType: props.project.triggerType,
          workflowRef: props.project.workflowRef,
          workflowVersion: props.project.workflowVersion,
        } as IWorkflowExecutionRecordVo];
        if (props.param.triggerId && allRecords.value.length) {
          handleChange(allRecords.value.find(e => e.triggerId === props.param.triggerId) as IWorkflowExecutionRecordVo);
        } else if (allRecords.value.length){
          handleChange(allRecords.value[0]);
        }
      });
      // 获取allRecords方法
      await recordList.initAllRecords();
      // 开启record-list数据监听
      recordList.listen();
    });

    onBeforeUnmount(()=>{
      // 卸载record-list数据监听
      recordList.destroy();
    });
    return {
      allRecords,
      handleChange,
      currentRecordStatus,
      WorkflowExecutionRecordStatusEnum,
      refreshRecordList() {
        recordList.initAllRecords();
      },
      refreshSuspended() {
        recordList.refreshSuspended();
      },
    };
  },
});
</script>

<style lang="less">
.jm-workflow-detail-record-list {
  display: flex;
  padding: 18px 30px 0;
  background-color: #ffffff;
  .tab {
    position: relative;
    min-width: 50px;
    padding: 0 6px 0 4px;
    height: 50px;
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
        font-size: 36px;
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
  height: 4px;
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