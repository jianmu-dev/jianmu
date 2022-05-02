<template>
  <jm-drawer
    title="节点配置面板"
    :size="500"
    direction="rtl"
    destroy-on-close
  >
    <template #title>
      {{ nodeData.getName() }}
      <a :href="nodeData.getDocUrl()" target="_blank" class="jm-icon-button-help"/>
    </template>
    <div class="jm-workflow-editor-node-config-panel">
      <div class="panel-container">
        <cron-panel v-if="nodeData.getType() === NodeTypeEnum.CRON" :node-data="nodeData"/>
        <webhook-panel v-else-if="nodeData.getType() === NodeTypeEnum.WEBHOOK" :node-data="nodeData"/>
        <shell-panel v-else-if="nodeData.getType() === NodeTypeEnum.SHELL" :node-data="nodeData"/>
        <async-task-panel v-else-if="nodeData.getType() === NodeTypeEnum.ASYNC_TASK" :node-data="nodeData"/>
      </div>
      <div class="footer">
        <jm-button @click="cancel">取消</jm-button>
        <jm-button type="primary" @click="save">保存</jm-button>
      </div>
    </div>
  </jm-drawer>
</template>

<script lang="ts">
import { defineComponent, inject } from 'vue';
import { NodeTypeEnum } from '../../model/data/enumeration';
import CronPanel from './cron-panel.vue';
import WebhookPanel from './webhook-panel.vue';
import ShellPanel from './shell-panel.vue';
import AsyncTaskPanel from './async-task-panel.vue';
import { Graph } from '@antv/x6';
import { CustomX6NodeProxy } from '../../model/data/custom-x6-node-proxy';

export default defineComponent({
  components: { CronPanel, WebhookPanel, ShellPanel, AsyncTaskPanel },
  props: {
    nodeId: {
      type: String,
      required: true,
    },
  },
  setup(props, { emit }) {
    const getGraph = inject('getGraph') as () => Graph;
    const node = getGraph().getNodes().find(({ id }) => props.nodeId === id)!;
    const proxy = new CustomX6NodeProxy(node);
    const nodeData = proxy.getData();

    return {
      NodeTypeEnum,
      nodeData,
      cancel: () => {
        // 关闭抽屉
        emit('update:model-value', false);
      },
      save: () => {
        proxy.setData(nodeData);

        // 关闭抽屉
        emit('update:model-value', false);
      },
    };
  },
  emits: ['update:model-value'],
});
</script>

<style scoped lang="less">
.jm-workflow-editor-node-config-panel {
  height: 100%;
  display: flex;
  flex-direction: column;

  .panel-container {
    // 铺满剩余高度
    flex-grow: 1;
    background-color: #8e9ded;
  }

  .footer {
    box-sizing: border-box;
    width: 100%;
    height: 60px;
    display: flex;
    border-top: 1px solid #6b7b8d;
    justify-content: center;
    align-items: center;
  }
}
</style>