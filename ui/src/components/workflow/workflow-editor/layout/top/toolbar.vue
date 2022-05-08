<template>
  <div class="jm-workflow-editor-toolbar">
    <div class="left">
      <button class="jm-icon-workflow-back" @click="goBack"></button>
      <div class="title">{{ workflowForm.name }}</div>
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
        <jm-tooltip v-if="zoomPercentage === '100%'" content="适屏" placement="bottom" :appendToBody="false">
          <button class="jm-icon-workflow-zoom-fit" @click="zoom(ZoomTypeEnum.FIT)"></button>
        </jm-tooltip>
        <jm-tooltip v-else content="原始大小" placement="bottom" :appendToBody="false">
          <button class="jm-icon-workflow-zoom-original" @click="zoom(ZoomTypeEnum.ORIGINAL)"></button>
        </jm-tooltip>
      </div>
      <div class="configs">
        <div>并发执行</div>
        <div>
          <jm-switch v-model="workflowForm.global.concurrent"></jm-switch>
        </div>
      </div>
      <div class="operations">
        <jm-button @click="save(true)">保存并返回</jm-button>
        <jm-button type="primary" @click="save(false)">保存</jm-button>
      </div>
    </div>
    <project-panel v-if="projectPanelVisible"
                   v-model="projectPanelVisible"/>
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
      goBack: () => {
        emit('back');
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

          proxy.$confirm(' ', '保存此次修改', {
            confirmButtonText: '保存',
            cancelButtonText: '不保存',
            type: 'info',
          }).then(async () => {
            emit('save', back, workflowTool.toDsl(workflowForm.value));
          }).catch(() => {
          });
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

  button[class^="jm-icon-workflow-"] {
    border-radius: 2px;
    border-width: 0;
    background-color: transparent;
    color: #526579;
    cursor: pointer;
    text-align: center;
    width: 24px;
    height: 24px;
    font-size: 18px;

    &::before {
      font-weight: bold;
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
    }
  }
}
</style>