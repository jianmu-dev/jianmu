<template>
  <div class="jm-workflow-editor-node-panel" ref="container">
    <div class="collapse" @click="collapse"></div>
    <div class="groups">
      <div class="group">
        <x6-vue-shape
          v-for="item in arr"
          :key="item.nodeRef"
          :node-data="item"
          @mousedown="(e) => drag(item, e)"/>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject, onMounted, ref } from 'vue';
import { Graph } from '@antv/x6';
import WorkflowDnd from '../../model/workflow-dnd';
import { NodeTypeEnum } from '../../model/enumeration';
import X6VueShape from '../../shape/x6-vue-shape.vue';
import { INodeData } from '../../model/data';

export default defineComponent({
  components: { X6VueShape },
  setup(props, context) {
    const arr: INodeData[] = [{
      nodeRef: 'git_clone',
      nodeType: NodeTypeEnum.ASYNC_TASK,
      image: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FikR5g_gILRZjr-olpMqypjhfuj3',
      text: '克隆建木CI代码',
    }, {
      nodeRef: 'node_build',
      nodeType: NodeTypeEnum.ASYNC_TASK,
      image: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FpON0edVLhS5j3Kgvs9i-rwljruu',
      text: 'NodeJs构建前端项目',
    }, {
      nodeRef: 'docker_build',
      nodeType: NodeTypeEnum.ASYNC_TASK,
      image: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FvWtndEdOK9WmEc8WCmvKLYpy2Xv',
      text: 'docker镜像构建',
    }, {
      nodeRef: 'npm_publish',
      nodeType: NodeTypeEnum.ASYNC_TASK,
      image: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FtRbpLVb0vl5qURYdyxMAHE8c7tT',
      text: '发布npm依赖包',
    }, {
      nodeRef: 'org_gov',
      nodeType: NodeTypeEnum.ASYNC_TASK,
      image: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FlENvzR04GwGJMgUvC_UGadygwXl',
      text: '组织治理',
    }];
    const container = ref<HTMLElement>();
    const containerWidth = ref<number>(0);
    const getGraph = inject('getGraph') as () => Graph;
    // 初始化dnd
    const dnd = new WorkflowDnd(getGraph());

    onMounted(() => {
      containerWidth.value = container.value!.offsetWidth;
    });

    // TODO 异步加载触发器、内置/本地/官方/社区节点列表

    return {
      arr,
      container,
      drag: (data: INodeData, event: Event) => {
        dnd.drag(data, event);
      },
      collapse: _this => {
        console.log(_this);
        const panel = container.value!;
        const collapse = panel.querySelector('.collapse');
        if (panel.offsetWidth > 0) {
          panel.style.width = '0px';
          collapse.style.display = 'block';
        } else {
          panel.style.width = `${containerWidth.value}px`;
          collapse.style.display = '';
        }
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
  background: #6F8794;
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
      //justify-content: left;
    }
  }
}
</style>