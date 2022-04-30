<template>
  <div class="jm-workflow-editor-node-panel" ref="container">
    <div class="collapse" @click="collapse">
      <div :class="{ icon: true, collapsed }"></div>
    </div>
    <jm-scrollbar>
      <div class="groups">
        <div class="group">
          <x6-vue-shape
            v-for="item in nodes"
            :key="item.nodeRef"
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
import { INodeData } from '../../model/data';
import WorkflowNode from '../../model/workflow-node';

export default defineComponent({
  components: { X6VueShape },
  emits: ['node-selected'],
  setup(props, { emit }) {
    const workflowNode = new WorkflowNode();
    const collapsed = ref<boolean>(false);
    const container = ref<HTMLElement>();
    const containerWidth = ref<number>(0);
    const getGraph = inject('getGraph') as () => Graph;
    let workflowDnd: WorkflowDnd;

    // 确定容器宽度
    onMounted(() => {
      containerWidth.value = container.value!.offsetWidth;

      const graphPanelEl = container.value!.nextElementSibling! as HTMLElement;
      // 初始化dnd
      workflowDnd = new WorkflowDnd(getGraph(), graphPanelEl, (data: INodeData) => {
        emit('node-selected', data);
      });
    });

    return {
      collapsed,
      nodes: ref<INodeData[]>(workflowNode.search().flat(Infinity)),
      container,
      drag: (data: INodeData, event: MouseEvent) => {
        if (!workflowDnd) {
          return;
        }

        workflowDnd.drag(data, event);
      },
      collapse: () => {
        const panel = container.value!;
        const collapse = panel.querySelector('.collapse') as HTMLElement;
        if (panel.clientWidth > 0) {
          panel.style.width = '0px';
          collapse.style.display = 'block';
          collapsed.value = true;
          return;
        }
        panel.style.width = `${containerWidth.value}px`;
        collapse.style.display = '';
        collapsed.value = false;
      },
    };
  },
});
</script>

<style scoped lang="less">
@import '../../vars';

.jm-workflow-editor-node-panel {
  margin-top: 20px;
  // 折叠动画
  transition: width 0.3s;
  width: @node-panel-width;
  border: 1px solid #E6EBF2;
  box-sizing: border-box;
  background: #FFFFFF;
  position: relative;
  z-index: 1;

  .collapse {
    display: none;
    position: absolute;
    top: -1px;
    right: -41px;

    width: 40px;
    height: 40px;
    background-color: #082340;
    border-radius: 0 10px 10px 0;
    cursor: pointer;

    .icon {
      width: 100%;
      height: 100%;
      background-image: url('../../svgs/collapse.svg');
      transition: transform 0.5s ease-in-out;

      &.collapsed {
        transform: scaleX(-1);
      }
    }
  }

  &:hover {
    .collapse {
      display: block;
    }
  }

  .groups {
    width: @node-panel-width;

    .group {
      display: flex;
      flex-wrap: wrap;

      .jm-workflow-x6-vue-shape {
        margin: 10px;
      }
    }
  }
}
</style>