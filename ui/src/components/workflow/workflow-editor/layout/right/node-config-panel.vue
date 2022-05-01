<template>
  <jm-drawer
    title="节点配置面板"
    :size="500"
    direction="rtl"
    destroy-on-close
  >
    <div class="jm-workflow-editor-node-config-panel">
      <div class="panel-container">
        <cron-panel v-if="panelForm.type === NodeTypeEnum.CRON" v-model="panelForm"/>
        <webhook-panel v-else-if="panelForm.type === NodeTypeEnum.WEBHOOK" v-model="panelForm"/>
        <shell-panel v-else-if="panelForm.type === NodeTypeEnum.SHELL" v-model="panelForm"/>
        <async-task-panel v-else-if="panelForm.type === NodeTypeEnum.ASYNC_TASK" v-model="panelForm"/>
      </div>
      <div class="footer">
        <jm-button @click="cancel">取消</jm-button>
        <jm-button type="primary" @click="save">保存</jm-button>
      </div>
    </div>
  </jm-drawer>
</template>

<script lang="ts">
import { defineComponent, PropType, ref } from 'vue';
import { INodeData } from '../../model/data';
import { NodeTypeEnum } from '../../model/enumeration';
import CronPanel from './cron-panel.vue';
import WebhookPanel from './webhook-panel.vue';
import ShellPanel from './shell-panel.vue';
import AsyncTaskPanel from './async-task-panel.vue';

export default defineComponent({
  components: { CronPanel, WebhookPanel, ShellPanel, AsyncTaskPanel },
  props: {
    nodeData: {
      type: Object as PropType<INodeData>,
      required: true,
    },
  },
  setup(props, { emit }) {
    const panelForm = ref<INodeData>(props.nodeData);

    return {
      NodeTypeEnum,
      panelForm,
      cancel: () => {
        emit('update:model-value', false);
      },
      save: () => {
        console.log('panelForm', {
          ...panelForm.value,
        });

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