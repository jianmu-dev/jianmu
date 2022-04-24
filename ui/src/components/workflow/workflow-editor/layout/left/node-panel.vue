<template>
  <div class="jm-workflow-editor-node-panel">
    <span @mousedown="drag">ttttt</span>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject, onMounted } from 'vue';
import { Graph } from '@antv/x6';
import WorkflowDnd from '../../model/workflow-dnd';
import { NodeTypeEnum } from '../../model/enumeration';

export default defineComponent({
  setup(props, context) {
    const arr = [{
      image: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FikR5g_gILRZjr-olpMqypjhfuj3',
      text: '克隆建木CI代码',
    }, {
      image: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FpON0edVLhS5j3Kgvs9i-rwljruu',
      text: 'NodeJs构建前端项目',
    }, {
      image: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FvWtndEdOK9WmEc8WCmvKLYpy2Xv',
      text: 'docker镜像构建',
    }, {
      image: 'https://jianmuhub.img.dghub.cn/node-definition/icon/FtRbpLVb0vl5qURYdyxMAHE8c7tT',
      text: '发布npm依赖包',
    }, {
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
      drag: (event: Event) => {
        dnd.drag({
          nodeType: NodeTypeEnum.ASYNC_TASK,
          ...arr[1],
        }, event);
      },
    };
  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-node-panel {
  width: 300px;
  position: relative;
}
</style>