<template>
  <div class="jm-workflow-editor-toolbar">
    <div class="left">
      <button class="jm-icon-button-left" @click="goBack"></button>
      <div class="group">
        <div class="title">{{ workflowData.name }}</div>
        <button class="jm-icon-workflow-edit" @click="edit"></button>
      </div>
      <div class="branch" v-if="workflowData.association.entry">
        <img src="~@/assets/svgs/index/branch.svg" alt="">
        {{ workflowData.association.branch }}
      </div>
    </div>
    <div class="right">
      <div class="tools">
        <jm-tooltip content="缩小" placement="bottom" :appendToBody="false">
          <button class="jm-icon-workflow-zoom-out" @click="zoom(ZoomTypeEnum.OUT)"></button>
        </jm-tooltip>
        <div class="ratio">{{ zoomPercentage }}</div>
        <jm-tooltip content="放大" placement="bottom" :appendToBody="false">
          <button class="jm-icon-workflow-zoom-in" @click="zoom(ZoomTypeEnum.IN)"></button>
        </jm-tooltip>
        <jm-tooltip content="居中" placement="bottom" :appendToBody="false">
          <button class="jm-icon-workflow-zoom-center" @click="zoom(ZoomTypeEnum.CENTER)"></button>
        </jm-tooltip>
      </div>
      <div class="global-param" @click="openGlobalDrawer">
        <i class="jm-icon-workflow-param"/>
        全局参数
        <i v-if="globalTip" class="global-icon" @click="openGlobalDrawer"/>
      </div>
      <div class="configs">
        <div>并发执行</div>
        <div>
          <jm-switch v-model="workflowForm.global.concurrent"></jm-switch>
        </div>
      </div>
      <div class="operations">
        <jm-button class="save-return" @click="save(true)" @keypress.enter.prevent>保存并返回</jm-button>
        <jm-button type="primary" @click="save(false)" @keypress.enter.prevent>保存</jm-button>
      </div>
    </div>
    <project-panel
      v-if="projectPanelVisible"
      v-model="projectPanelVisible"
      :workflow-data="workflowData"
    />
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, inject, PropType, ref } from 'vue';
import { Graph } from '@antv/x6';
import { RefTypeEnum, ZoomTypeEnum } from '../../model/data/enumeration';
import { WorkflowTool } from '../../model/workflow-tool';
import ProjectPanel from './project-panel.vue';
import { IWorkflow } from '../../model/data/common';
import { WorkflowValidator } from '../../model/workflow-validator';
import { cloneDeep } from 'lodash';
import { compare } from '../../model/util/object';
import { checkDuplicate } from '../../model/util/reference';

export default defineComponent({
  components: { ProjectPanel },
  props: {
    workflowData: {
      type: Object as PropType<IWorkflow>,
      required: true,
    },
  },
  emits: ['back', 'save', 'open-global-drawer'],
  setup(props, { emit }) {
    const { proxy } = getCurrentInstance() as any;
    let workflowBackUp = cloneDeep(props.workflowData);
    const workflowForm = ref<IWorkflow>(props.workflowData);
    const projectPanelVisible = ref<boolean>(false);
    const getGraph = inject('getGraph') as () => Graph;
    const graph = getGraph();
    const getWorkflowValidator = inject('getWorkflowValidator') as () => WorkflowValidator;
    const workflowValidator = getWorkflowValidator();
    const zoomVal = ref<number>(graph.zoom());
    const globalTip = ref<boolean>(false);
    const paramRefs = computed<string[]>(
      () => workflowForm.value.global.params.map(({ ref }) => ref));
    const checkGlobalParams = async (): Promise<void> => {
      // 表单验证
      for (const param of workflowForm.value.global.params) {
        try {
          await param.validate();
        } catch (err) {
          globalTip.value = true;
          return;
        }
      }
      // 检查是否重复
      try {
        checkDuplicate(paramRefs.value, RefTypeEnum.GLOBAL_PARAM);
      } catch (err) {
        globalTip.value = true;
        return;
      }
      globalTip.value = false;
    };

    const workflowTool = new WorkflowTool(graph);
    // 检查param重复，出报错信息
    const checkParamDuplicate = () => {
      try {
        checkDuplicate(paramRefs.value, RefTypeEnum.GLOBAL_PARAM);
      } catch (err) {
        proxy.$error(err.message);
      }
    };

    return {
      ZoomTypeEnum,
      workflowForm,
      projectPanelVisible,
      globalTip,
      zoomPercentage: computed<string>(() => `${Math.round(zoomVal.value * 100)}%`),
      goBack: async () => {
        const originData = workflowBackUp.data ? JSON.parse(workflowBackUp.data) : {};
        let targetData: any = graph.toJSON();
        if (targetData.cells.length === 0) {
          delete targetData.cells;
        }
        workflowTool.slimGraphData(originData);
        workflowTool.slimGraphData(targetData);
        if (workflowBackUp.name !== workflowForm.value.name ||
          workflowBackUp.description !== workflowForm.value.description ||
          workflowBackUp.groupId !== workflowForm.value.groupId ||
          !compare(originData, targetData) ||
          !compare(JSON.stringify(workflowBackUp.global), JSON.stringify(workflowForm.value.global))
        ) {
          proxy.$confirm(' ', '保存此次修改', {
            confirmButtonText: '保存',
            cancelButtonText: '不保存',
            distinguishCancelAndClose: true,
            type: 'info',
          }).then(async () => {
            try {
              await workflowValidator.check();
            } catch ({ message }) {
              proxy.$error(message);
              return;
            }
            workflowForm.value.data = JSON.stringify(targetData);
            emit('save', true, workflowTool.toDsl(workflowForm.value));
          }).catch((action: string) => {
            if (action === 'cancel') {
              emit('back');
            }
          });
        } else {
          emit('back');
        }
      },
      edit: () => {
        projectPanelVisible.value = true;
      },
      zoom: async (type: ZoomTypeEnum) => {
        workflowTool.zoom(type);
        zoomVal.value = graph.zoom();
      },
      save: async (back: boolean) => {
        try {
          await workflowValidator.check();

          const graphData = graph.toJSON();
          workflowTool.slimGraphData(graphData);
          workflowForm.value.data = JSON.stringify(graphData);
          emit('save', back, workflowTool.toDsl(workflowForm.value));
          workflowBackUp = cloneDeep(workflowForm.value);
        } catch ({ message }) {
          proxy.$error(message);
        }
      },
      openGlobalDrawer: () => {
        emit('open-global-drawer', true, checkGlobalParams);
        if (globalTip.value) {
          checkParamDuplicate();
        }
      },
    };
  },
});
</script>

<style scoped lang="less">
@import '../../vars';

.jm-workflow-editor-toolbar {
  height: @tool-bar-height;
  background: #FFFFFF;
  z-index: 3;
  font-size: 14px;
  color: #042749;
  padding: 0 30px;

  display: flex;
  justify-content: space-between;
  align-items: center;

  button[class^="jm-icon-"] {
    border-radius: 2px;
    border-width: 0;
    background-color: transparent;
    color: #6B7B8D;
    cursor: pointer;
    text-align: center;
    width: 24px;
    height: 24px;
    font-size: 18px;

    &::before {
      font-weight: 500;
    }

    &:hover {
      background-color: #EFF7FF;
      color: @primary-color;
    }
  }

  .left {
    display: flex;

    .group {
      display: flex;
      align-items: center;

      .title {
        margin-left: 20px;
        margin-right: 10px;
      }
    }

    .branch {
      img {
        width: 16px;
        height: 16px;
        margin-right: 6px;
      }

      display: flex;
      align-items: center;

      &:before {
        content: '';
        display: inline-block;
        margin: 0 20px;
        width: 2px;
        height: 16px;
        background-color: #CDD1E3;
      }
    }
  }

  .right {
    display: flex;
    justify-content: right;

    .tools {
      display: flex;
      align-items: center;

      .ratio {
        width: 40px;
        margin: 0 10px;
        text-align: center;
      }

      .jm-icon-workflow-zoom-in {
        margin-right: 10px;
      }
    }

    .global-param {
      display: flex;
      align-items: center;
      justify-content: center;
      margin-left: 60px;
      font-size: 14px;
      padding: 0 8px 0 6px;
      border-radius: 2px;

      position: relative;

      .jm-icon-workflow-param {
        display: flex;
        width: 20px;
        height: 20px;
        align-items: center;
        justify-content: center;
        margin-right: 2px;
        font-size: 18px;
      }

      .global-icon {
        position: absolute;
        top: -2px;
        right: -6px;
        width: 14px;
        height: 14px;
        border-radius: 50%;
        background: url('../../svgs/global-param-warning.svg');
      }

      &:hover {
        background: #EFF7FF;
        color: #096DD9;
        cursor: pointer;
      }
    }

    .configs {
      display: flex;
      align-items: center;
      margin: 0 60px 0 44px;

      > div + div {
        margin-left: 10px;
      }
    }

    .operations {
      .save-return {
        background: #F5F5F5;
        border-radius: 2px;
        color: #082340;
        border: none;
        box-shadow: none;

        &:hover {
          background-color: #EFF7FF;
          color: #096DD9;
        }
      }
    }
  }
}
</style>
