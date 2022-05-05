<template>
  <div :class="{ 'jm-workflow-editor-node-panel': true, collapsed }" ref="container">
    <div class="collapse-btn jm-icon-workflow-back" @click="collapse"/>
    <jm-scrollbar>
      <div class="groups">
        <div class="group">
          <x6-vue-shape
            v-for="item in nodes"
            :key="item.ref"
            :node-data="item"
            @mousedown="(e) => drag(item, e)"/>
        </div>
      </div>
    </jm-scrollbar>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject, onMounted, ref } from 'vue';
import { Graph } from '@antv/x6';
import WorkflowDnd from '../../model/workflow-dnd';
import X6VueShape from '../../shape/x6-vue-shape.vue';
import { IWorkflowNode } from '../../model/data/common';
import WorkflowNode from '../../model/workflow-node';
import { WorkflowValidator } from '../../model/workflow-validator';

export default defineComponent({
  components: { X6VueShape },
  emits: ['node-selected'],
  setup(props, { emit }) {
    const workflowNode = new WorkflowNode();
    const collapsed = ref<boolean>(false);
    const container = ref<HTMLElement>();
    const getGraph = inject('getGraph') as () => Graph;
    const getWorkflowValidator = inject('getWorkflowValidator') as () => WorkflowValidator;
    let workflowDnd: WorkflowDnd;

    // 确定容器宽度
    onMounted(() => {
      // 初始化dnd
      workflowDnd = new WorkflowDnd(
        getGraph(),
        getWorkflowValidator(),
        container.value! as HTMLElement,
        (nodeId: IWorkflowNode) => emit('node-selected', nodeId));
    });

    return {
      collapsed,
      nodes: ref<IWorkflowNode[]>(workflowNode.search().flat(Infinity)),
      container,
      drag: (data: IWorkflowNode, event: MouseEvent) => {
        if (!workflowDnd) {
          return;
        }

        workflowDnd.drag(data, event);
      },
      collapse: () => {
        collapsed.value = container.value!.clientWidth > 0;
      },
    };
  },
});
</script>

<style scoped lang="less">
@import '../../vars';

@node-panel-top: 20px;
@collapse-btn-width: 40px;

.jm-workflow-editor-node-panel {
  // 折叠动画
  transition: width 0.3s ease-in-out;
  width: @node-panel-width;
  height: calc(100% - @node-panel-top);
  border: 1px solid #E6EBF2;
  background: #FFFFFF;
  position: absolute;
  left: 0;
  top: @node-panel-top;
  z-index: 2;

  &.collapsed {
    width: 0;

    .collapse-btn {
      display: block;
      // 反转
      transform: scaleX(-1);
      border-radius: 10px 0 0 10px;
    }
  }

  .collapse-btn {
    display: none;
    position: absolute;
    top: -1px;
    right: calc(-1px - @collapse-btn-width);

    width: @collapse-btn-width;
    height: 40px;
    line-height: 40px;
    text-align: center;
    color: #FFFFFF;
    font-size: 20px;
    background-color: #082340;
    border-radius: 0 10px 10px 0;
    cursor: pointer;

    // 反转动画
    transition: transform 0.5s ease-in-out;
  }

  &:hover {
    .collapse-btn {
      display: block;
    }
  }

  .groups {
    width: @node-panel-width;

    .group {
      display: flex;
      flex-wrap: wrap;

      ::v-deep(.jm-workflow-x6-vue-shape) {
        margin: 10px;
        width: 64px;

        .x6-vue-shape-icon {
          width: 64px;
          height: 64px;
        }
      }
    }
  }
}
</style>