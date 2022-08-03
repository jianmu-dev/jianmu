<template>
  <div class="project-preview-dialog">
    <jm-dialog
      :title="title"
      v-model="dialogVisible"
      :width="dialogWidth"
      @close="close"
    >
      <div class="content" v-loading="loading">
        <jm-workflow-viewer
          v-if="!loading"
          :dsl="dsl"
          readonly
          :viewMode="viewMode"
          :node-infos="nodeDefs"
          :trigger-type="triggerType"
          @change-view-mode="mode=>viewMode=mode"
        />
        <div class="overflow-bottom">
          <div @click="prevProject" class="button-left" :class="{disabled: prevDis}"><i class="jm-icon-button-left"/>上一个</div>
          <div @click="nextProject" class="button-right" :class="{disabled: nextDis}">下一个<i class="jm-icon-button-right"/></div>
        </div>
      </div>
    </jm-dialog>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, onBeforeMount, ref, SetupContext } from 'vue';
import { TriggerTypeEnum, ViewModeEnum } from '@/api/dto/enumeration';
import { fetchProjectDetail, fetchWorkflow } from '@/api/view-no-auth';
import { INodeDefVo } from '@/api/dto/project';
import { useStore } from 'vuex';

export default defineComponent({
  props: {
    projectId: {
      type: String,
      required: true,
    },
    projects: {
      type: Array,
      required: true,
    },
  },
  // 覆盖dialog的close事件
  emits: ['close', 'prev-project', 'next-project'],
  setup(props: any, { emit }: SetupContext) {
    const { proxy } = getCurrentInstance() as any;
    const store = useStore();
    const entry = store.state.entry;
    const dialogVisible = ref<boolean>(true);
    const dialogWidth = computed<string>(() => entry ? '1000px' : '1200px');
    const title = ref<string>('');
    const loading = ref<boolean>(false);
    const dsl = ref<string>();
    const nodeDefs = ref<INodeDefVo[]>([]);
    const triggerType = ref<TriggerTypeEnum>();
    const viewMode = ref<string>(ViewModeEnum.GRAPHIC);
    const close = () => emit('close');
    let previewId = ref(props.projectId);
    // 上一个按钮禁止
    const prevDis = computed<boolean>(()=>{
      return props.projects.findIndex((e:any)=>e.id===previewId.value) === 0;
    });
    // 下一个按钮禁止
    const nextDis = computed<boolean>(()=>{
      return props.projects.findIndex((e:any)=>e.id===previewId.value) === props.projects.length-1;
    });
    const loadDsl = async () => {
      // if (dsl.value) {
      //   return;
      // }

      try {
        loading.value = true;

        const {
          workflowName,
          workflowRef,
          workflowVersion,
          triggerType: _triggerType,
        } = await fetchProjectDetail(previewId.value);
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
      prevDis,
      nextDis,
      dialogWidth,
      TriggerTypeEnum,
      dialogVisible,
      title,
      loading,
      dsl,
      nodeDefs,
      triggerType,
      viewMode,
      close,
      prevProject() {
        if (prevDis.value) {
          return;
        }
        const currentPreviewIdIndex = props.projects.findIndex((e:any)=>e.id === previewId.value);
        previewId.value = props.projects[currentPreviewIdIndex-1].id;
        loadDsl();
      },
      nextProject() {
        if (nextDis.value) {
          return;
        }
        const currentPreviewIdIndex = props.projects.findIndex((e:any)=>e.id === previewId.value);
        previewId.value = props.projects[currentPreviewIdIndex+1].id;
        loadDsl();
      },
    };
  },
});
</script>

<style scoped lang="less">
.project-preview-dialog {
  ::v-deep(.el-dialog) {
    box-shadow: none;
    background-color: transparent;
    // 图标
    .el-dialog__header {
      padding: 0;
      color: #082340;
      height: 40px;
      // margin-bottom: 20px;
      box-sizing: border-box;
      .el-dialog__title {
        font-size: 16px;
        color: #ffffff;
      }
      // .el-dialog__title::before {
      //   font-family: 'jm-icon-input';
      //   content: '\e803';
      //   margin-right: 10px;
      //   color: #6b7b8d;
      //   font-size: 20px;
      //   vertical-align: bottom;
      //   position: relative;
      //   top: 1px;
      // }
      .el-dialog__close::before {
        font-size: 20px;
      }
      .el-dialog__headerbtn {
        top: 65px;
        right: 30px;
        z-index: 10;
      }
    }

    .el-dialog__body {
      padding: 0;
      background-color: #ffffff;
    }
  }

  .content {
    position: relative;
    height: 70vh;
    .overflow-bottom {
      position: absolute;
      left: 0;
      bottom: 0;
      display: flex;
      height: 70px;
      width: 100%;
      background-color: #ffffff;
      align-items: center;
      .button-left {
        margin-left: 30px;
        line-height: 36px;
        width: 110px;
        font-size: 16px;
        text-align: center;
        border: 0.5px solid #CAD6EE;
        border-radius: 2px;
        color: #082340;
        cursor: pointer;
        .jm-icon-button-left:before {
          margin-left: -4px;
          margin-right: 8px;
          font-size: 14px;
          vertical-align: 1px;
        }
        &:hover {
          color: #096DD9;
          background-color: #EFF7FF;
        }
      }
      .button-right {
        margin-left: 20px;
        line-height: 36px;
        width: 110px;
        font-size: 16px;
        text-align: center;
        border: 0.5px solid #CAD6EE;
        border-radius: 2px;
        color: #082340;
        cursor: pointer;
        .jm-icon-button-right:before {
          margin-left: 8px;
          margin-right: -4px;
          font-size: 14px;
          vertical-align: 1px;
        }
        &:hover {
          color: #096DD9;
          background-color: #EFF7FF;
        }
      }
      .disabled {
        color: #979797;
        cursor: not-allowed;
        &:hover {
          color: #979797;
          background-color: #ffffff;
        }
      }
    }
  }
}
</style>
