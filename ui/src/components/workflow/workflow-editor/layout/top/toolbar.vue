<template>
  <div class="jm-workflow-editor-toolbar">
    <div class="left">
      <button class="jm-icon-button-left" @click="goBack"></button>
      <div class="title">{{ workflowData.name }}</div>
      <button class="jm-icon-workflow-edit" @click="edit"></button>
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
import { ZoomTypeEnum } from '../../model/data/enumeration';
import { WorkflowTool } from '../../model/workflow-tool';
import ProjectPanel from './project-panel.vue';
import { IWorkflow } from '../../model/data/common';
import { WorkflowValidator } from '../../model/workflow-validator';
import { cloneDeep } from 'lodash';
import { compare } from '../../model/util/object';

export default defineComponent({
  components: { ProjectPanel },
  props: {
    workflowData: {
      type: Object as PropType<IWorkflow>,
      required: true,
    },
  },
  emits: ['back', 'save'],
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

    const workflowTool = new WorkflowTool(graph);

    return {
      ZoomTypeEnum,
      workflowForm,
      projectPanelVisible,
      zoomPercentage: computed<string>(() => `${Math.round(zoomVal.value * 100)}%`),
      goBack: async () => {
        const originData = workflowBackUp.data ? JSON.parse(workflowBackUp.data) : {};
        let targetData = graph.toJSON();
        if (targetData.cells.length === 0) {
          delete targetData.cells;
        }
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
              await workflowValidator.checkNodes();
            } catch ({ message }) {
              proxy.$error(message);
              return;
            }
            emit('save', true, workflowTool.toDsl(workflowForm.value, targetData));
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
          await workflowValidator.checkNodes();

          emit('save', back, workflowTool.toDsl(workflowForm.value, graph.toJSON()));
          workflowBackUp = cloneDeep(workflowForm.value);
        } catch ({ message }) {
          proxy.$error(message);
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
    align-items: center;

    .title {
      margin-left: 20px;
      margin-right: 10px;
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

    .configs {
      display: flex;
      align-items: center;
      margin: 0 50px;

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
          background: #D9D9D9;
        }
      }
    }
  }
}
</style>