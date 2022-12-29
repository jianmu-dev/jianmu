<template>
  <div class="jm-workflow-detail">
    <topbar
      v-if="recordDetail.project"
      :project="recordDetail.project"
      :session="session"
      :showStopAll="showStopAll"
      @jump="groupId => $emit('jump', groupId)"
      @back="$emit('back')"
      @logout="$emit('logout')"
      @trigger="trigger"
    />
    <record-list
      ref="recordList"
      v-if="recordDetail.project"
      :param="param"
      :project="recordDetail.project"
      :event="listEvent"
      @change-record="handleChangeRecord"
    />
    <record-info
      v-if="recordDetail.record && modelValue.viewMode === ViewModeEnum.GRAPHIC"
      :record="recordDetail.record"
      @terminate="terminate"
    />
    <log-toolbar
      v-if="recordDetail.project && recordDetail.record"
      :entry="entry"
      :showLogBtn="recordDetail.record.status !== ''"
      :showParamBtn="globalParamBtn"
      :dslType="recordDetail.project.dslType"
      :dslMode="modelValue.viewMode === ViewModeEnum.YAML"
      @click-param-log="openParamLog"
      @click-process-log="openProcessLog"
    />
    <graph-panel
      ref="graphPanel"
      v-if="recordDetail.record"
      :entry="entry"
      :record="recordDetail.record"
      :currentRecordStatus="recordList.currentRecordStatus"
      :viewMode="modelValue.viewMode"
      :event="taskEvent"
      @has-global-param="bool => (globalParamBtn = bool)"
      @change-view-mode="handleChangeDslMode"
    />
  </div>
</template>

<script lang="ts">
import { getCurrentInstance, computed, defineComponent, onMounted, onUnmounted, PropType, ref } from 'vue';
import { IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { IWorkflowDetailParam, IRecordDetail, IRecordListParam } from './model/data/common';
import Topbar from './layout/top/topbar.vue';
import RecordList from './layout/main/record-list.vue';
import RecordInfo from './layout/main/record-info.vue';
import LogToolbar from './layout/main/log-toolbar.vue';
import GraphPanel from './layout/main/graph-panel.vue';
import { ISession } from '@/model/modules/session';
import { ViewModeEnum, WorkflowExecutionRecordStatusEnum as SE } from '@/api/dto/enumeration';
import { fetchProjectDetail } from '@/api/view-no-auth';
import { IProjectDetailVo } from '@/api/dto/project';
import { IEventType } from '@/api/dto/enumeration';
import { WorkflowDetail } from './model/detail';
import { IEvent } from '@/api/event/common';
import { IAsyncTaskInstanceStatusUpdatedEvent } from '@/api/event/workflow-execution-record';

export default defineComponent({
  name: 'jm-workflow-detail',
  components: { Topbar, RecordList, RecordInfo, GraphPanel, LogToolbar },
  props: {
    modelValue: {
      type: Object as PropType<IWorkflowDetailParam>,
      required: true,
    },
    session: Object as PropType<ISession>,
    entry: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['back', 'jump', 'update:model-value', 'logout', 'trigger'],
  setup(props, { emit }) {
    const recordDetail = ref<IRecordDetail>({});
    const { proxy } = getCurrentInstance() as any;
    // list组件需要的参数
    const param = computed(
      (): IRecordListParam => ({
        workflowRef: recordDetail.value.project?.workflowRef || '',
        triggerId: props.modelValue.triggerId,
      }),
    );
    // record-list.vue组件
    const recordList = ref<any>();
    // 新增更新实例 event
    const listEvent = ref<IEvent>();
    // graph-panel.vue组件
    const graphPanel = ref<any>();
    // 更新任务状态 event
    const taskEvent = ref<IAsyncTaskInstanceStatusUpdatedEvent>();
    //  全局参数按钮
    const globalParamBtn = ref<boolean>(false);
    let detailSse: any;
    let firstLoad = true;
    onMounted(async () => {
      // 获取项目详情
      recordDetail.value.project = await fetchProjectDetail(props.modelValue.projectId);
      // 详情页的SSE数据推送
      detailSse = new WorkflowDetail(
        recordDetail.value.project.workflowRef,
        event => {
          // 异步任务推送事件
          if (event.eventName === IEventType.AsyncTaskInstanceStatusUpdatedEvent) {
            // 必须是推送的是当前页面停留实例的任务数据
            if (
              recordDetail.value.record &&
              recordDetail.value.record?.id === (event as IAsyncTaskInstanceStatusUpdatedEvent).workflowInstanceId
            ) {
              taskEvent.value = event as IAsyncTaskInstanceStatusUpdatedEvent;
            }
            // 实例新增更新推送事件
          } else {
            listEvent.value = event;
          }
        },
        () => {
          if (!firstLoad) {
            const i = recordList.value.allRecords.findIndex((e: any) => {
              return SE.RUNNING === e.status || SE.SUSPENDED === e.status || false;
            });
            // 所有实例中不存在运行和挂起状态数据 直接返回不刷新record
            if (i === -1) return;
            // SSE重连之前请求最新数据
            recordList.value.refreshRecordList();
            // 当前停留实例状态非挂起和运行 直接返回不刷新graph
            const s = recordDetail.value.record?.status || SE.INIT;
            if (s !== SE.RUNNING && s !== SE.SUSPENDED) return;
            graphPanel.value.refreshGraphPanel();
            return;
          }
          firstLoad = false;
        },
      );
    });

    onUnmounted(() => {
      detailSse.destroy();
    });

    // 运行中加挂起数量 大于等于2 显示终止全部按钮
    const showStopAll = computed<boolean>(() =>
      recordList.value
        ? recordList.value.allRecords.filter(
          (e: IWorkflowExecutionRecordVo) => e.status === SE.RUNNING || e.status === SE.SUSPENDED,
        ).length >= 2
        : false,
    );
    return {
      recordDetail,
      recordList,
      showStopAll,
      listEvent,
      graphPanel,
      taskEvent,
      param,
      globalParamBtn,
      ViewModeEnum,
      handleChangeRecord(record: IWorkflowExecutionRecordVo) {
        const { name: projectGroupName, triggerId } = record;
        // entry -> false 建木的topbar 更改项目名
        if (!props.entry) {
          recordDetail.value.project = {
            ...recordDetail.value.project,
            projectGroupName,
          } as IProjectDetailVo;
        }
        // record-info 修改实例信息 graph-panel组件会随record变化而请求数据
        recordDetail.value.record = record;
        // record-list 同步modelValue数据/地址栏
        emit('update:model-value', { ...props.modelValue, triggerId });
      },
      handleChangeDslMode(viewMode: ViewModeEnum) {
        // graph-panel 同步modelValue数据/地址栏
        emit('update:model-value', { ...props.modelValue, viewMode });
      },
      trigger(msg: string, err?: Error) {
        // undefined 触发成功之后刷新record列表 msg -> undefined为成功
        if (!err) {
          recordList.value.refreshRecordList();
          emit('trigger', msg, err);
          setTimeout(() => {
            graphPanel.value.refreshGraphPanel();
            // TODO 触发之后第一时间task为0 导致不能进入状态实时更新(延后500毫秒)
          }, 500);
        } else {
          proxy.$throw(err, proxy);
        }
      },
      async terminate() {
        // 终止成功之后刷新列表
        recordList.value.refreshRecordList();
        setTimeout(() => {
          // 终止成功之后刷新graph
          graphPanel.value.refreshGraphPanel();
          // TODO 终止之后第一时间有时候 task对应状态没有改变(延后200豪秒)
        }, 200);
      },
      // 打开全局参数弹窗
      openParamLog() {
        graphPanel.value.openParamLog();
      },
      // 打开日志参数弹窗
      openProcessLog() {
        graphPanel.value.openProcessLog();
      },
    };
  },
});
</script>

<style lang="less">
.jm-workflow-detail {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  user-select: none;
  -moz-user-select: none;
  -webkit-user-select: none;
  -ms-user-select: none;
}
</style>
