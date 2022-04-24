<template>
  <jm-drawer
    title="节点配置面板"
    :size="400"
    direction="rtl"
    destroy-on-close
  >
    <div class="jm-workflow-editor-node-config-panel">
      <div>This is node config panel</div>
      <div class="footer">
        <jm-button @click="cancel">取消</jm-button>
        <jm-button type="primary" @click="save">保存</jm-button>
      </div>
    </div>
  </jm-drawer>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  emits: ['update:model-value'],
  setup(props, { emit }) {
    return {
      cancel: () => {
        emit('update:model-value', false);
      },
      save: () => {
        emit('update:model-value', false);
      },
    };
  },
});
</script>

<style scoped lang="less">
.jm-workflow-editor-node-config-panel {
  .footer {
    box-sizing: border-box;
    position: absolute;
    left: 0;
    bottom: 0;
    width: 100%;
    height: 60px;
    display: flex;
    border-top: 1px solid #6b7b8d;
    justify-content: center;
    align-items: center;
  }
}
</style>