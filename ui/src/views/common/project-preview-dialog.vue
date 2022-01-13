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
          :trigger-type="TriggerTypeEnum.MANUAL"/>
      </div>
    </jm-dialog>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, onBeforeMount, ref, SetupContext } from 'vue';
import { DslTypeEnum, TriggerTypeEnum } from '@/api/dto/enumeration';
import { fetchProjectDetail, fetchWorkflow } from '@/api/view-no-auth';
import { INodeDefVo } from '@/api/dto/project';

export default defineComponent({
  props: {
    projectId: {
      type: String,
      required: true,
    },
    dslType: {
      type: String,
      required: true,
    },
  },
  // 覆盖dialog的close事件
  emits: ['close'],
  setup(props: any, { emit }: SetupContext) {
    const { proxy } = getCurrentInstance() as any;
    const dialogVisible = ref<boolean>(true);
    const loading = ref<boolean>(false);
    const dsl = ref<string>();
    const nodeDefs = ref<INodeDefVo[]>([]);
    const close = () => emit('close');

    const loadDsl = async () => {
      if (dsl.value) {
        return;
      }

      try {
        loading.value = true;

        const { workflowRef, workflowVersion } = await fetchProjectDetail(props.projectId);
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
      title: computed<string>(() => {
        let t = '预览';
        switch (props.dslType) {
          case DslTypeEnum.WORKFLOW:
            t += '流程';
            break;
          case DslTypeEnum.PIPELINE:
            t += '管道';
            break;
        }
        return t;
      }),
      dialogVisible,
      loading,
      dsl,
      nodeDefs,
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