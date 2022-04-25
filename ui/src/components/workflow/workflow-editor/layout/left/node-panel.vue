<template>
  <div class="jm-workflow-editor-node-panel" ref="container">
    <div class="collapse" @click="collapse"></div>
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
  setup(props, context) {
    const workflowNode = new WorkflowNode();
    const container = ref<HTMLElement>();
    const containerWidth = ref<number>(0);
    const getGraph = inject('getGraph') as () => Graph;
    // 初始化dnd
    const dnd = new WorkflowDnd(getGraph());

    // 确定容器宽度
    onMounted(() => (containerWidth.value = container.value!.offsetWidth));

    return {
      nodes: ref<INodeData[]>(workflowNode.search().flat(Infinity)),
      container,
      drag: (data: INodeData, event: Event) => {
        dnd.drag(data, event);
      },
      collapse: () => {
        const panel = container.value!;
        const collapse = panel.querySelector('.collapse');
        if (panel.offsetWidth > 0) {
          panel.style.width = '0px';
          collapse.style.display = 'block';
          return;
        }
        panel.style.width = `${containerWidth.value}px`;
        collapse.style.display = '';
      },
    };
  },
});
</script>

<style scoped lang="less">
@import '../../vars';

.jm-workflow-editor-node-panel {
  // 折叠动画
  transition: width 0.3s;
  width: @node-panel-width;
  border-right: 1px solid #8e9ded;
  background: #FFFFFF;
  position: relative;

  .collapse {
    display: none;
    position: absolute;
    z-index: 1;
    top: calc((100% - 50px) / 2);
    right: -50px;

    width: 50px;
    height: 50px;
    background: red;
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