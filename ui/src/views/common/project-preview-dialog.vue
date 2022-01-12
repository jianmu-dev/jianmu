<template>
  <div class="project-preview-dialog">
    <jm-dialog
      title="预览"
      v-model="dialogVisible"
      width="1200px"
      @close="close"
    >
      <jm-tabs v-model="tabActiveName" class="tabs">
        <jm-tab-pane name="workflow">
          <template #label>
            <div class="label">{{ workflowTitle }}图</div>
          </template>
          <div class="tab" v-loading="loading">
            <jm-workflow-viewer
              :dsl="dsl"
              readonly
              :node-infos="nodeDefs"
              :trigger-type="TriggerTypeEnum.MANUAL"/>
          </div>
        </jm-tab-pane>
        <jm-tab-pane name="tab" lazy>
          <template #label>
            <div class="label">DSL</div>
          </template>
          <div class="tab dsl" v-loading="loading">
            <jm-dsl-editor :value="dsl" readonly/>
          </div>
        </jm-tab-pane>
      </jm-tabs>
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
    const tabActiveName = ref<string>('workflow');

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
      workflowTitle: computed<string>(() => {
        let t = '';
        switch (props.dslType) {
          case DslTypeEnum.WORKFLOW:
            t = '流程';
            break;
          case DslTypeEnum.PIPELINE:
            t = '管道';
            break;
        }
        return t;
      }),
      dialogVisible,
      loading,
      dsl,
      nodeDefs,
      close,
      tabActiveName,
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

  .tabs {
    padding: 24px;
  }

  ::v-deep(.el-tabs) {
    .el-tabs__active-bar {
      display: none;
    }

    .el-tabs__item {
      padding: 0;

      .label {
        width: 120px;
        height: 40px;
        text-align: center;
        background-color: #EEF0F3;
        color: #082340;
        border-radius: 6px 6px 0 0;
      }

      &.is-active {
        .label {
          background-color: #082340;
          color: #FFFFFF;
        }
      }
    }

    .el-tabs__item + .el-tabs__item {
      padding-left: 4px;
    }

    .el-tabs__nav-wrap {
      box-shadow: inherit;

      .el-tabs__nav-scroll {
        height: 41px;
        line-height: inherit;
      }
    }
  }

  .tab {
    border: 1px solid #E6EBF2;
    height: 55vh;
    box-sizing: border-box;

    &.dsl {
      padding: 20px;
    }
  }
}
</style>