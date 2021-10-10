<template>
  <div class="workflow-definition-detail" v-loading="loading">
    <div class="right-top-btn">
      <router-link to="/workflow-definition">
        <jm-button type="primary" class="jm-icon-button-cancel" size="small">关闭</jm-button>
      </router-link>
    </div>
    <jm-tabs v-model="activatedTab" class="tabs" @tab-click="handleTabClick">
      <jm-tab-pane name="dsl" lazy>
        <template #label><i class="jm-icon-tab-dsl"></i> DSL模式</template>
        <jm-dsl-editor id="workflow-definition-detail-dsl" class="dsl-editor" :value="dslSourceCode" readonly/>
      </jm-tab-pane>
      <jm-tab-pane name="workflow" lazy>
        <template #label><i class="jm-icon-tab-dsl"></i> 流程图模式</template>
        <jm-workflow-viewer id="workflow-definition-detail-workflow" :dsl="dslSourceCode"/>
      </jm-tab-pane>
    </jm-tabs>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, onMounted, ref } from 'vue';
import { fetchSource } from '@/api/workflow-definition';
import { adaptHeight, IAutoHeight } from '@/utils/auto-height';

const autoHeight: {
  [key: string]: IAutoHeight,
} = {
  dsl: {
    elementId: 'workflow-definition-detail-dsl',
    offsetTop: 214,
  },
  workflow: {
    elementId: 'workflow-definition-detail-workflow',
    offsetTop: 214,
  },
};

export default defineComponent({
  props: {
    workflowRef: {
      type: String,
      required: true,
    },
    workflowVersion: {
      type: String,
      required: true,
    },
  },
  setup(props: any) {
    const { proxy } = getCurrentInstance() as any;
    const loading = ref<boolean>(false);
    const activatedTab = ref<string>('workflow');
    const dslSourceCode = ref<string>('');

    onMounted(() => {
      adaptHeight(autoHeight[activatedTab.value]);

      loading.value = !loading.value;
      fetchSource(props.workflowRef, props.workflowVersion).then(({ dslText }) => {
        dslSourceCode.value = dslText;

        loading.value = !loading.value;
      }).catch((err: Error) => proxy.$throw(err, proxy));
    });

    return {
      loading,
      activatedTab,
      dslSourceCode,
      handleTabClick: ({ props: { name } }: any) => {
        proxy.$nextTick(() => adaptHeight(autoHeight[name]));
      },
    };
  },
});
</script>

<style scoped lang="less">
.workflow-definition-detail {
  font-size: 14px;
  color: #333333;
  margin-bottom: 25px;

  .right-top-btn {
    position: fixed;
    right: 20px;
    top: 78px;

    .jm-icon-button-add::before {
      font-weight: bold;
    }
  }

  .tabs {
    background-color: #FFFFFF;
    border-radius: 4px 4px 0 0;

    .dsl-editor {
      z-index: 1;
    }
  }
}
</style>
