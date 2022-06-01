<template>
  <div class="project-preview-dialog">
    <jm-dialog
      :title="title"
      v-model="dialogVisible"
      width="1200px"
      @close="close"
    >
      <div class="content" v-loading="loading">
        <jm-workflow-viewer
          :dsl="dsl"
          readonly
          :node-infos="nodeDefs"
          :trigger-type="triggerType"/>
      </div>
    </jm-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, onBeforeMount, ref, SetupContext } from 'vue';
import { TriggerTypeEnum } from '@/api/dto/enumeration';
import { fetchProjectDetail, fetchWorkflow } from '@/api/view-no-auth';
import { INodeDefVo } from '@/api/dto/project';

export default defineComponent({
  props: {
    projectId: {
      type: String,
      required: true,
    },
  },
  // 覆盖dialog的close事件
  emits: ['close'],
  setup(props: any, { emit }: SetupContext) {
    const { proxy } = getCurrentInstance() as any;
    const dialogVisible = ref<boolean>(true);
    const title = ref<string>('');
    const loading = ref<boolean>(false);
    const dsl = ref<string>();
    const nodeDefs = ref<INodeDefVo[]>([]);
    const triggerType = ref<TriggerTypeEnum>();
    const close = () => emit('close');

    const loadDsl = async () => {
      if (dsl.value) {
        return;
      }

      try {
        loading.value = true;

        const {
          workflowName,
          workflowRef,
          workflowVersion,
          triggerType: _triggerType,
        } = await fetchProjectDetail(props.projectId);
        title.value = workflowName;
        triggerType.value = _triggerType;

        const { nodes, dslText } = await fetchWorkflow(workflowRef, workflowVersion);
        dsl.value = dslText;
        nodeDefs.value = nodes
          .filter(({ metadata }) => metadata)
          .map(({ metadata }) => JSON.parse(metadata as string));
      } catch (err) {
        close();

        proxy.$throw(err, proxy);
      } finally {
        loading.value = false;
      }
    };

    onBeforeMount(() => loadDsl());

    return {
      TriggerTypeEnum,
      dialogVisible,
      title,
      loading,
      dsl,
      nodeDefs,
      triggerType,
      close,
    };
  },
});
</script>

<style scoped lang="less">
.project-preview-dialog {
  ::v-deep(.el-dialog) {
    // 图标
    .el-dialog__header {
      .el-dialog__title::before {
        font-family: 'jm-icon-input';
        content: '\e803';
        margin-right: 10px;
        color: #6b7b8d;
        font-size: 20px;
        vertical-align: bottom;
        position: relative;
        top: 1px;
      }
    }

    .el-dialog__body {
      padding: 0;
    }
  }

  .content {
    height: 60vh;
  }
}
</style>