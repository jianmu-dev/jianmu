<template>
  <jm-drawer
    title="节点配置面板"
    :size="380"
    direction="rtl"
    :modal="false"
    destroy-on-close
  >
    <template #title>
      <div>
        <span>{{ nodeData.getName() }}</span>
        <a :href="nodeData.getDocUrl()" target="_blank" class="jm-icon-button-help"/>
      </div>
    </template>
    <div class="jm-workflow-editor-node-config-panel">
      <jm-scrollbar class="panel-container">
        <cron-panel v-if="nodeData.getType() === NodeTypeEnum.CRON"
                    :node-data="nodeData" @form-created="handleFormCreated"/>
        <webhook-panel v-else-if="nodeData.getType() === NodeTypeEnum.WEBHOOK"
                       :node-data="nodeData" @form-created="handleFormCreated"/>
        <shell-panel v-else-if="nodeData.getType() === NodeTypeEnum.SHELL"
                     :node-data="nodeData" @form-created="handleFormCreated"/>
        <async-task-panel v-else-if="nodeData.getType() === NodeTypeEnum.ASYNC_TASK"
                          :node-data="nodeData" @form-created="handleFormCreated"/>
      </jm-scrollbar>
      <div class="footer">
        <jm-button @click="cancel">取消</jm-button>
        <jm-button type="primary" @click="save">保存</jm-button>
      </div>
    </div>
  </jm-drawer>
</template>

<script lang="ts">
import { defineComponent, inject, ref } from 'vue';
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
    nodeWaringClicked: {
      type: Boolean,
      required: true,
    },
  },
  setup(props, { emit }) {
    const getGraph = inject('getGraph') as () => Graph;
    const node = getGraph().getNodes().find(({ id }) => props.nodeId === id)!;
    const proxy = new CustomX6NodeProxy(node);
    // 不能为ref，否则，表单内容的变化影响数据绑定
    const nodeData = proxy.getData();
    const formRef = ref();

    return {
      NodeTypeEnum,
      nodeData,
      handleFormCreated: (ref: any) => {
        formRef.value = ref;

        if (!props.nodeWaringClicked) {
          return;
        }
        // 点击节点警告按钮打开配置面板时，自动校验一次表单，实现自动展示错误信息
        formRef.value.validate().catch(() => {
        });
      },
      cancel: () => {
        // 关闭抽屉
        emit('update:model-value', false);
      },
      save: () => {
        formRef.value.validate((valid: boolean) => {
          if (!valid) {
            return;
          }
          proxy.setData(nodeData);
          // 关闭抽屉
          emit('update:model-value', false);

          // 通过校验时，删除警告
          node.removeTool('button');
        });
      },
    };
  },
  emits: ['update:model-value'],
});
</script>

<style scoped lang="less">
.el-drawer {
  .jm-icon-button-help {
    display: inline-block;
    width: 20px;
    height: 20px;
    text-align: center;
    line-height: 20px;
    font-size: 14px;
    margin-left: 7px;

    &:hover {
      background: #EFF7FF;
      color: #086CD8;
    }
  }

  .jm-workflow-editor-node-config-panel {
    height: 100%;
    display: flex;
    flex-direction: column;

    .panel-container {
      height: calc(100vh - 210px);
      padding: 10px 20px 0;
    }

    .footer {
      box-sizing: border-box;
      width: 100%;
      height: 75px;
      display: flex;
      border-top: 1px solid #E6EBF2;
      justify-content: center;
      align-items: center;
    }
  }
}
</style>