<template>
  <div class="jm-workflow-editor-node-panel">
    <div class="group">
      <x6-vue-shape
        v-for="item in arr"
        :key="item.nodeRef"
        :node-data="item"
        @mousedown="(e) => drag(item, e)"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject, onMounted } from 'vue';
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
    let dnd: WorkflowDnd;
    onMounted(() => {
      const getGraph = inject('getGraph') as () => Graph;

      // 初始化dnd
      dnd = new WorkflowDnd(getGraph());
    });

    return {
      arr,
      drag: (data: INodeData, event: Event) => {
        dnd.drag(data, event);
      },
    };
  },
});
</script>

<style scoped lang="less">
@import '../../vars';

.jm-workflow-editor-node-panel {
  width: @node-panel-width;
  //position: relative;

  .group {
    display: flex;
    flex-wrap: wrap;
    //justify-content: left;
  }
}
</style>