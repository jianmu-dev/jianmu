<template>
  <div class="jm-workflow-editor-toolbar">
    <div class="left">
      <button class="jm-icon-workflow-back" @click="goBack"></button>
      <div class="title">未命名项目</div>
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
        <!--        <jm-tooltip v-if="zoomPercentage === '100%'" content="适屏" placement="bottom" :appendToBody="false">-->
        <jm-tooltip v-if="true" content="适屏" placement="bottom" :appendToBody="false">
          <button class="jm-icon-workflow-zoom-fit" @click="zoom(ZoomTypeEnum.FIT)"></button>
        </jm-tooltip>
        <jm-tooltip v-else content="原始大小" placement="bottom" :appendToBody="false">
          <button class="jm-icon-workflow-zoom-original" @click="zoom(ZoomTypeEnum.ORIGINAL)"></button>
        </jm-tooltip>
      </div>
      <div class="configs">
        <div>并发执行</div>
        <div>
          <jm-switch></jm-switch>
        </div>
      </div>
      <div class="operations">
        <jm-button @click="save(true)">保存并返回</jm-button>
        <jm-button type="primary" @click="save(false)">保存</jm-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, inject, ref } from 'vue';
import { Graph } from '@antv/x6';
import { ZoomTypeEnum } from '../../model/enumeration';
import { WorkflowTool } from '../../model/workflow-tool';

export default defineComponent({
  emits: ['back', 'save'],
  setup(props, context) {
    const getGraph = inject('getGraph') as () => Graph;
    const graph = getGraph();
    const zoomVal = ref<number>(graph.zoom());

    const workflowTool: WorkflowTool = new WorkflowTool(graph);

    return {
      ZoomTypeEnum,
      zoomPercentage: computed<string>(() => `${Math.round(zoomVal.value * 100)}%`),
      goBack: () => {
        console.log('go back');
      },
      edit: () => {
        console.log('edit');
      },
      zoom: async (type: ZoomTypeEnum) => {
        workflowTool.zoom(type);
        zoomVal.value = graph.zoom();
      },
      save: (back: boolean) => {
        console.log('save', back);
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
  z-index: 1;
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
      color: #096DD9;
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
      display: flex;
      align-items: center;

      .el-button {
        padding: 8px 24px;
        min-height: 36px;

        & + .el-button {
          margin-left: 20px;
        }
      }
    }
  }
}
</style>