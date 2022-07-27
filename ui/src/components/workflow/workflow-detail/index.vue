<template>
  <div class="jm-workflow-detail">
    <topbar
      v-if="recordDetail.project"
      :project="recordDetail.project"
      :session="session"
      :entry="modelValue.entry"
      @jump="groupId=>$emit('jump', groupId)"
      @back="$emit('back')"
      @logout="$emit('logout')"
      @trigger="trigger"
    />
    <record-list
      ref="recordList"
      v-if="recordDetail.project"
      :param="param"
      :project="recordDetail.project"
      @change-record="handleChangeRecord"
    />
    <record-info
      v-if="recordDetail.record && modelValue.viewMode===ViewModeEnum.GRAPHIC"
      :record="recordDetail.record"
      @terminate="terminate"
    />
    <graph-panel
      ref="graphPanel"
      v-if="recordDetail.record"
      :entry="modelValue.entry"
      :record="recordDetail.record"
      :currentRecordStatus="recordList.currentRecordStatus"
      :viewMode="modelValue.viewMode"
      @change-view-mode="handleChangeDslMode"
      @trigger-refresh="recordListRefreshSuspended"
    />
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, PropType, ref } from 'vue';
import { IWorkflowExecutionRecordVo } from '@/api/dto/workflow-execution-record';
import { IWorkflowDetailParam, IRecordDetail, IRecordListParam } from './model/data/common';
import Topbar from './layout/top/topbar.vue';
import RecordList from './layout/main/record-list.vue';
import RecordInfo from './layout/main/record-info.vue';
import GraphPanel from './layout/main/graph-panel.vue';
import { ISessionVo } from '@/api/dto/session';
import { ViewModeEnum } from '@/api/dto/enumeration';
import { fetchProjectDetail } from '@/api/view-no-auth';
import { IProjectDetailVo } from '@/api/dto/project';

export default defineComponent({
  name: 'jm-workflow-detail',
  components: { Topbar, RecordList, RecordInfo, GraphPanel },
  props: {
    modelValue: {
      type: Object as PropType<IWorkflowDetailParam>,
      required: true,
    },
    session: Object as PropType<ISessionVo>,
  },
  emits: ['back', 'jump', 'update:model-value', 'logout', 'trigger'],
  setup(props, { emit }) {
    const recordDetail = ref<IRecordDetail>({});
    // list组件需要的参数
    const param =  computed(():IRecordListParam => ({
      workflowRef: recordDetail.value.project?.workflowRef || '',
      triggerId: props.modelValue.triggerId,
    }));
    // record-list.vue组件
    const recordList = ref<any>();
    // graph-panel.vue组件
    const graphPanel = ref<any>();
    onMounted(async ()=>{
      // 获取项目详情
      recordDetail.value.project = await fetchProjectDetail(props.modelValue.projectId);
    });

    return {
      recordDetail,
      recordList,
      graphPanel,
      param,
      ViewModeEnum,
      handleChangeRecord(record: IWorkflowExecutionRecordVo) {
        const { name: projectGroupName, triggerId } = record;
        // entry -> false 建木CI的topbar 更改项目名
        if (!props.modelValue.entry) {
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
      // ui/src/components/workflow/workflow-detail
      handleChangeDslMode(viewMode: ViewModeEnum) {
        // graph-panel 同步modelValue数据/地址栏
        emit('update:model-value', { ...props.modelValue, viewMode });
      },
      trigger(msg: any) {
        emit('trigger', msg);
        // undefined 触发成功之后刷新record列表 msg -> undefined为成功
        if(!msg) {
          emit('update:model-value', { ...props.modelValue, triggerId: '' });
          recordList.value.refreshRecordList();
          graphPanel.value.refreshGraphPanel();
        }
      },
      terminate() {
        // 终止成功之后刷新列表
        recordList.value.refreshRecordList();
        // 终止成功之后刷新graph
        graphPanel.value.refreshGraphPanel();
      },
      recordListRefreshSuspended() {
        // 开启 挂起刷新开关
        recordList.value.refreshSuspended();
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
