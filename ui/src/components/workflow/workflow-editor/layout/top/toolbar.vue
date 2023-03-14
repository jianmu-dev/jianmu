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
      <div class="cache" @click="openCachePanel">
        <i class="jm-icon-workflow-cache" />
        <span>缓存</span>
        <i class="cache-icon" v-if="cacheIconVisible" />
      </div>
      <div class="configs">
        最大并发数
        <i class="jm-icon-button-help" @mouseover="tooltipVisible = true" @mouseout="tooltipVisible = false"></i>
        <jm-select
          ref="concurrentRef"
          :model-value="concurrentVal"
          filterable
          allow-create
          @keyup.enter="enterConcurrent"
          @change="changeConcurrent"
          @blur="blurConcurrent"
        >
          <jm-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
        </jm-select>
        <div class="tooltip-popper" v-show="tooltipVisible">
          <span class="popper-description">单个流程中可同时执行/挂起的实例数</span>
          <img src="../../svgs/concurrent-example.svg" class="concurrent-example" />
        </div>
      </div>
      <div class="operations">
        <jm-button class="save-return" @click="save(true)" @keypress.enter.prevent>保存并返回</jm-button>
        <jm-button type="primary" @click="save(false)" @keypress.enter.prevent>保存</jm-button>
      </div>
    </div>
    <project-panel v-if="projectPanelVisible" v-model="projectPanelVisible" :workflow-data="workflowData" />
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, inject, onMounted, PropType, ref } from 'vue';
import { Graph } from '@antv/x6';
import { ZoomTypeEnum } from '../../model/data/enumeration';
import { WorkflowTool } from '../../model/workflow-tool';
import ProjectPanel from './project-panel.vue';
import { IWorkflow } from '../../model/data/common';
import { WorkflowValidator } from '../../model/workflow-validator';
import { cloneDeep } from 'lodash';
import { compare } from '../../model/util/object';
import { Global } from '../../model/data/global';
import { v4 as uuidv4 } from 'uuid';

export default defineComponent({
  components: { ProjectPanel },
  props: {
    workflowData: {
      type: Object as PropType<IWorkflow>,
      required: true,
    },
  },
  emits: ['back', 'save', 'open-cache-panel'],
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
    const cacheIconVisible = ref<boolean>(false);
    const options = ref([
      {
        value: '1',
        label: '1',
      },
      {
        value: '10',
        label: '10',
      },
      {
        value: '30',
        label: '30',
      },
      {
        value: '50',
        label: '50',
      },
      {
        value: '70',
        label: '70',
      },
      {
        value: '100',
        label: '100',
      },
    ]);
    const tooltipVisible = ref<boolean>(false);
    const concurrentVal = ref<string>();
    const concurrentRef = ref();
    onMounted(() => {
      if (workflowForm.value.global.concurrent === true) {
        concurrentVal.value = '9';
        return;
      } else if (workflowForm.value.global.concurrent === false) {
        concurrentVal.value = '1';
        return;
      }
      concurrentVal.value = workflowForm.value.global.concurrent.toString();
    });

    const workflowTool = new WorkflowTool(graph);

    const changeConcurrent = (val: string) => {
      if (!val) {
        return;
      }
      const reg = /^[1-9][0-9]{0,3}$/;
      if (Number(val) > 9999 || !reg.test(val)) {
        concurrentVal.value = workflowForm.value.global.concurrent.toString();
      } else {
        concurrentVal.value = val;
        workflowForm.value.global.concurrent = Number(concurrentVal.value);
      }
    };

    // 缓存校验图标
    const checkCache = async (): Promise<void> => {
      if (workflowForm.value.global.caches && typeof workflowForm.value.global.caches === 'string') {
        workflowForm.value.global.caches = [{ ref: workflowForm.value.global.caches, key: uuidv4() }];
      }
      const { global } = workflowForm.value;
      // 如果没有值直接忽略
      if (global && (global.caches?.length === 0 || !global.caches)) {
        cacheIconVisible.value = false;
        return;
      }
      try {
        await new Global(global).validateCache();
        cacheIconVisible.value = false;
      } catch (err) {
        cacheIconVisible.value = true;
      }
    };

    return {
      ZoomTypeEnum,
      workflowForm,
      projectPanelVisible,
      zoomPercentage: computed<string>(() => `${Math.round(zoomVal.value * 100)}%`),
      goBack: async () => {
        const originData = workflowBackUp.data ? JSON.parse(workflowBackUp.data) : {};
        const targetData: any = graph.toJSON();
        if (targetData.cells.length === 0) {
          delete targetData.cells;
        }
        workflowTool.slimGraphData(originData);
        workflowTool.slimGraphData(targetData);
        if (
          workflowBackUp.name !== workflowForm.value.name ||
          workflowBackUp.description !== workflowForm.value.description ||
          workflowBackUp.groupId !== workflowForm.value.groupId ||
          !compare(originData, targetData) ||
          !compare(JSON.stringify(workflowBackUp.global), JSON.stringify(workflowForm.value.global))
        ) {
          proxy
            .$confirm(' ', '保存此次修改', {
              confirmButtonText: '保存',
              cancelButtonText: '不保存',
              distinguishCancelAndClose: true,
              type: 'info',
            })
            .then(async () => {
              try {
                await workflowValidator.checkNodes();
              } catch ({ message }) {
                proxy.$error(message);
                return;
              }
              workflowForm.value.data = JSON.stringify(targetData);
              emit('save', true, workflowTool.toDsl(workflowForm.value));
            })
            .catch((action: string) => {
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

          const graphData = graph.toJSON();
          workflowTool.slimGraphData(graphData);
          workflowForm.value.data = JSON.stringify(graphData);
          emit('save', back, workflowTool.toDsl(workflowForm.value));
          workflowBackUp = cloneDeep(workflowForm.value);
        } catch ({ message }) {
          proxy.$error(message);
        }
      },
      openCachePanel: () => {
        checkCache();
        emit('open-cache-panel', checkCache);
      },
      cacheIconVisible,
      tooltipVisible,
      options,
      concurrentVal,
      concurrentRef,
      changeConcurrent,
      enterConcurrent: (e: any) => {
        changeConcurrent(e.target.value);
        concurrentRef.value.blur();
      },
      blurConcurrent: (e: any) => {
        changeConcurrent(e.target.value);
      },
    };
  },
});
</script>

<style scoped lang="less">
@import '../../vars';

.jm-workflow-editor-toolbar {
  height: @tool-bar-height;
  background: #ffffff;
  z-index: 3;
  font-size: 14px;
  color: #042749;
  padding: 0 30px;

  display: flex;
  justify-content: space-between;
  align-items: center;

  button[class^='jm-icon-'] {
    border-radius: 2px;
    border-width: 0;
    background-color: transparent;
    color: #6b7b8d;
    cursor: pointer;
    text-align: center;
    width: 24px;
    height: 24px;
    font-size: 18px;

    &::before {
      font-weight: 500;
    }

    &:hover {
      background-color: #eff7ff;
      color: @primary-color;
    }
  }

  .left {
    display: flex;
    align-items: center;

    .title {
      margin-left: 20px;
      margin-right: 10px;
      max-width: 253px;
      text-overflow: ellipsis;
      overflow-x: hidden;
      white-space: nowrap;
    }
  }

  .right {
    display: flex;
    justify-content: right;
    align-items: center;

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

    .cache {
      height: 20px;
      font-weight: 400;
      font-size: 14px;
      line-height: 20px;
      color: #042749;
      display: flex;
      align-items: center;
      margin: 0 0 0 50px;
      cursor: pointer;
      position: relative;

      .jm-icon-workflow-cache {
        margin-right: 6px;

        &::before {
          color: #6b7b8d;
        }
      }

      .cache-icon {
        display: flex;
        width: 12px;
        height: 12px;
        background: url('../../svgs/cache-waring.svg');
        position: absolute;
        right: -8px;
        top: -4px;
      }
    }

    .configs {
      display: flex;
      align-items: center;
      margin: 0 60px 0 44px;
      position: relative;

      ::v-deep(.el-select) {
        width: 88px;
        height: 36px;

        .el-input__icon {
          display: none;
        }

        .el-input {
          &.is-focus {
            .el-input__inner {
              border-color: #096dd9;
            }
          }

          .el-input__inner {
            border-color: #dde3ee;
          }
        }
      }

      .jm-icon-button-help {
        width: 24px;
        height: 24px;
        margin-right: 8px;
        color: #6b7b8d;
        text-align: center;
        line-height: 24px;
        font-size: 14px;
      }

      .tooltip-popper {
        width: 436px;
        height: 295px;
        padding: 16px;
        background: #ffffff;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        border-radius: 4px;
        box-sizing: border-box;

        position: absolute;
        top: 51px;
        right: 100px;

        .popper-description {
          font-weight: 500;
          font-size: 18px;
          color: #042749;
        }

        .concurrent-example {
          border: 10px solid #f0f2f5;
          margin-top: 8px;
        }
      }

      > div + div {
        margin-left: 10px;
      }
    }

    .operations {
      .save-return {
        background: #f5f5f5;
        border-radius: 2px;
        color: #082340;
        border: none;
        box-shadow: none;

        &:hover {
          background: #d9d9d9;
        }
      }
    }
  }
}
</style>
