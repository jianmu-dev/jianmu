<template>
  <div :class="['project-item', isMove ? 'move' : '']" v-if="isMoveMode">
    <div
      :class="{
        'state-bar': true,
        [project.status.toLowerCase()]: true,
      }"
    ></div>
    <div class="content">
      <router-link
        :to="{
          name: 'workflow-execution-record-detail',
          query: { projectId: project.id },
        }"
      >
        <jm-text-viewer :value="project.name" class="title" tipPlacement="top"/>
      </router-link>
      <div class="time">
        <span v-if="project.status === ProjectStatusEnum.RUNNING"
        >执行时长：{{
            executionTimeFormatter(project.startTime, undefined, true)
          }}</span
        >
        <span v-else
        >最后完成时间：{{ datetimeFormatter(project.latestTime) }}</span
        >
      </div>
      <div class="time">
        下次执行时间：{{ datetimeFormatter(project.nextTime) }}
      </div>
      <div class="operation">
        <div class="top"></div>
        <div class="bottom"></div>
      </div>
    </div>
    <div class="cover"></div>
  </div>
  <div class="project-item" v-else>
    <div
      :class="{
        'state-bar': true,
        [project.status.toLowerCase()]: true,
      }"
    ></div>
    <div class="content">
      <router-link
        :to="{
          name: 'workflow-execution-record-detail',
          query: { projectId: project.id },
        }"
      >
        <jm-text-viewer :value="project.name" :class="{title:true,disabled:!enabled}" tipPlacement="top" />
      </router-link>
      <div :class="{
        time: true,
        disabled: !enabled,
      }">
        <span v-if="project.status === ProjectStatusEnum.RUNNING"
        >执行时长：{{
            executionTimeFormatter(project.startTime, undefined, true)
          }}</span
        >
        <span v-else
        >最后完成时间：{{ datetimeFormatter(project.latestTime) }}</span
        >
      </div>
      <div :class="{
        time: true,
        disabled: !enabled,
      }">
        下次执行时间：{{ datetimeFormatter(project.nextTime) }}
      </div>
      <div class="operation">
        <jm-tooltip :content="`${enabled ? '' : '已禁用，不可'}触发`" placement="bottom">
          <button
            :class="{ execute: true, doing: !enabled || executing }"
            @click="execute(project.id)"
          ></button>
        </jm-tooltip>
        <jm-tooltip placement="bottom">
          <template #content>
            <div>
              <span>{{ enabled ? '已启用' : '已禁用' }}</span>
              <a href="https://docs.jianmu.dev/guide/global.html"
                 target="_blank"
                 class="jm-icon-button-help"
                 style="color: #ffffff;"
              ></a>
            </div>
            <template v-if="!project.mutable">
              <div style="margin-top: 10px;">若要修改，请通过DSL更新</div>
            </template>
          </template>
          <button
            :class="{ able: true, disabled: project.mutable && !enabled, doing: !project.mutable || abling }"
            @click="able(project.id)"
          ></button>
        </jm-tooltip>
        <jm-tooltip
          v-if="project.triggerType === TriggerTypeEnum.WEBHOOK"
          content="Webhook"
          placement="bottom"
        >
          <button class="webhook" @click="webhookDrawerFlag = true"></button>
        </jm-tooltip>
        <jm-tooltip
          v-if="project.source === DslSourceEnum.LOCAL"
          content="编辑"
          placement="bottom"
        >
          <button class="edit" @click="edit(project.id)"></button>
        </jm-tooltip>
        <jm-tooltip v-else content="同步DSL" placement="bottom">
          <button
            :class="{ sync: true, doing: synchronizing }"
            @click="sync(project.id)"
          ></button>
        </jm-tooltip>
        <jm-tooltip
          v-if="project.source === DslSourceEnum.GIT"
          content="打开git仓库"
          placement="bottom"
        >
          <button class="git-label" @click="openGit(project.gitRepoId)"></button>
        </jm-tooltip>
        <jm-tooltip
          v-if="project.dslType === DslTypeEnum.WORKFLOW"
          content="预览流程"
          placement="bottom"
        >
          <button class="workflow-label" @click="dslDialogFlag = true"></button>
        </jm-tooltip>
        <jm-tooltip
          v-else-if="project.dslType === DslTypeEnum.PIPELINE"
          content="预览管道"
          placement="bottom"
        >
          <button class="pipeline-label" @click="dslDialogFlag = true"></button>
        </jm-tooltip>
        <jm-tooltip content="删除" placement="bottom">
          <button
            :class="{ del: true, doing: deleting }"
            @click="del(project.id)"
          ></button>
        </jm-tooltip>
      </div>
    </div>
    <webhook-drawer
      :current-project-id="project.id"
      :current-project-name="project.name"
      v-model:webhookVisible="webhookDrawerFlag"
    ></webhook-drawer>
    <project-preview-dialog
      v-if="dslDialogFlag"
      :project-id="project.id"
      :dsl-type="project.dslType"
      @close="dslDialogFlag = false"
    />
    <div class="cover"></div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, PropType, ref, SetupContext } from 'vue';
import { DslSourceEnum, DslTypeEnum, ProjectStatusEnum, TriggerTypeEnum } from '@/api/dto/enumeration';
import { IProjectVo } from '@/api/dto/project';
import { active, del, executeImmediately, synchronize } from '@/api/project';
import router from '@/router';
import { datetimeFormatter, executionTimeFormatter } from '@/utils/formatter';
import ProjectPreviewDialog from './project-preview-dialog.vue';
import WebhookDrawer from './webhook-drawer.vue';

export default defineComponent({
  components: { ProjectPreviewDialog, WebhookDrawer },
  props: {
    project: {
      type: Object as PropType<IProjectVo>,
      required: true,
    },
    // 控制item是否加上hover样式，根据对比id值判断
    move: {
      type: Boolean,
      default: false,
    },
    // 控制是否处于拖拽模式
    moveMode: {
      type: Boolean,
      default: false,
    },
  },
  emits: ['running', 'synchronized', 'deleted'],
  setup(props: any, { emit }: SetupContext) {
    const { proxy } = getCurrentInstance() as any;
    const isMove = computed<boolean>(() => props.move);
    const isMoveMode = computed<boolean>(() => props.moveMode);
    const executing = ref<boolean>(false);
    const abling = ref<boolean>(false);
    const synchronizing = ref<boolean>(false);
    const deleting = ref<boolean>(false);
    const enabled = ref<boolean>(props.project.enabled);
    const dslDialogFlag = ref<boolean>(false);
    const webhookDrawerFlag = ref<boolean>(false);
    return {
      isMoveMode,
      isMove,
      DslSourceEnum,
      DslTypeEnum,
      ProjectStatusEnum,
      TriggerTypeEnum,
      datetimeFormatter,
      executionTimeFormatter,
      executing,
      abling,
      synchronizing,
      deleting,
      enabled,
      dslDialogFlag,
      webhookDrawerFlag,
      execute: (id: string) => {
        if (!enabled.value || executing.value) {
          return;
        }

        const { triggerType } = props.project;
        const isWarning = triggerType === TriggerTypeEnum.WEBHOOK;

        let msg = '<div>确定要触发吗?</div>';
        if (isWarning) {
          msg +=
            '<div style="color: red; margin-top: 5px; font-size: 12px; line-height: normal;">注意：项目已配置webhook，手动触发可能会导致不可预知的结果，请慎重操作。</div>';
        }

        proxy
          .$confirm(msg, '触发项目执行', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: isWarning ? 'warning' : 'info',
            dangerouslyUseHTMLString: true,
          })
          .then(() => {
            executing.value = true;

            executeImmediately(id)
              .then(() => {
                proxy.$success('操作成功');
                executing.value = false;

                emit('running', id);
              })
              .catch((err: Error) => {
                proxy.$throw(err, proxy);
                executing.value = false;
              });
          })
          .catch(() => {
          });
      },
      able: (id: string) => {
        if (!props.project.mutable || abling.value) {
          return;
        }

        const str = enabled.value ? '禁用' : '启用';
        proxy.$confirm(`确定要${str}吗?`, `${str}项目`, {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info',
        }).then(async () => {
          abling.value = true;
          try {
            await active(id, !enabled.value);
            enabled.value = !enabled.value;

            proxy.$success(enabled.value ? '已启用' : '已禁用');
          } catch (err) {
            proxy.$throw(err, proxy);
          } finally {
            abling.value = false;
          }
        }).catch(() => {
        });
      },
      edit: (id: string) => {
        router.push({ name: 'update-project', params: { id } });
      },
      sync: (id: string) => {
        if (synchronizing.value) {
          return;
        }

        proxy
          .$confirm('确定要同步吗?', '同步DSL', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'info',
          })
          .then(() => {
            synchronizing.value = true;

            synchronize(id)
              .then(() => {
                proxy.$success('同步成功');
                synchronizing.value = false;

                emit('synchronized', id);
              })
              .catch((err: Error) => {
                proxy.$throw(err, proxy);
                synchronizing.value = false;
              });
          })
          .catch(() => {
          });
      },
      del: (id: string) => {
        if (deleting.value) {
          return;
        }

        const { name } = props.project;

        let msg = '<div>确定要删除项目吗?</div>';
        msg += `<div style="margin-top: 5px; font-size: 12px; line-height: normal;">名称：${name}</div>`;

        proxy
          .$confirm(msg, '删除项目', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
            dangerouslyUseHTMLString: true,
          })
          .then(() => {
            deleting.value = true;

            del(id)
              .then(() => {
                proxy.$success('删除成功');
                deleting.value = false;

                emit('deleted', id);
              })
              .catch((err: Error) => {
                proxy.$throw(err, proxy);
                deleting.value = false;
              });
          })
          .catch(() => {
          });
      },
      openGit: (gitRepoId: string) => {
        window.open(`/view/repo/${gitRepoId}`);
      },
    };
  },
});
</script>

<style scoped lang="less">
@keyframes workflow-running {
  0% {
    background-position-x: -53.5px;
  }
  100% {
    background-position-x: 0;
  }
}

@-webkit-keyframes workflow-running {
  0% {
    background-position-x: -53.5px;
  }
  100% {
    background-position-x: 0;
  }
}

.project-item {
  margin: 1%;
  margin-bottom: 0px;
  width: 19.2%;
  min-width: 260px;
  background-color: #ffffff;
  box-shadow: 0px 0px 8px 4px #eff4f9;
  min-height: 166px;

  &.move {
    position: relative;
    cursor: move;

    .cover {
      display: block;
      position: absolute;
      box-sizing: border-box;
      width: 100%;
      height: 100%;
      border: 2px solid #096dd9;
      background-color: rgba(140, 140, 140, 0.3);
      top: 0;
      left: 0;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        right: 0;
        display: inline-block;
        width: 30px;
        height: 30px;
        background-image: url('@/assets/svgs/sort/drag.svg');
        background-repeat: no-repeat;
      }
    }
  }

  .cover {
    display: none;
  }

  //&:hover {
  //  .content {
  //    .operation {
  //      button.del {
  //        display: block;
  //      }
  //    }
  //  }
  //}
  &:hover {
    box-shadow: 0px 6px 16px 4px #e6eef6;
  }

  .state-bar {
    height: 8px;
    overflow: hidden;

    &.init {
      background-color: #979797;
    }

    &.running {
      background-image: repeating-linear-gradient(115deg,
      #10c2c2 0px,
      #58d4d4 1px,
      #58d4d4 10px,
      #10c2c2 11px,
      #10c2c2 16px);
      background-size: 106px 114px;
      animation: 3s linear 0s infinite normal none running workflow-running;
    }

    &.succeeded {
      background-color: #3ebb03;
    }

    &.failed {
      background-color: #cf1524;
    }
  }

  .content {
    min-height: 116px;
    position: relative;
    padding: 20px 20px 16px 20px;

    .title {
      font-size: 16px;
      color: #082340;

      &.disabled {
        color: #979797;
      }

      &:hover {
        color: #096dd9;
      }
    }

    .time {
      margin-top: 10px;
      font-size: 13px;
      color: #6b7b8d;
      white-space: nowrap;

      &.disabled {
        color: #979797;
      }
    }

    .operation {
      margin-top: 18px;
      min-height: 26px;
      display: flex;
      align-items: center;

      button + button {
        margin-left: 12px;
      }

      button {
        width: 26px;
        height: 26px;
        background-color: transparent;
        border: 0;
        background-position: center center;
        background-repeat: no-repeat;
        cursor: pointer;
        outline: none;

        &:active {
          background-color: #eff7ff;
          border-radius: 4px;
        }

        &.execute {
          background-image: url('@/assets/svgs/btn/execute.svg');
        }

        &.able {
          background-image: url('@/assets/svgs/btn/disable.svg');

          &.disabled {
            background-color: #eff7ff;
            border-radius: 4px;
          }
        }

        &.edit {
          background-image: url('@/assets/svgs/btn/edit.svg');
        }

        &.sync {
          background-image: url('@/assets/svgs/btn/sync.svg');

          &.doing {
            animation: rotating 2s linear infinite;
          }
        }

        &.webhook {
          background-image: url('@/assets/svgs/btn/hook.svg');
        }

        &.del {
          position: absolute;
          right: 7px;
          top: 7px;
          width: 22px;
          height: 22px;
          //display: none;
          background-image: url('@/assets/svgs/btn/del.svg');
          background-size: contain;
          opacity: 0.65;
          padding: 2px;

          &:hover {
            opacity: 1;
          }
        }

        &.git-label {
          background-image: url('@/assets/svgs/index/git-label.svg');
        }

        &.workflow-label {
          background-image: url('@/assets/svgs/index/workflow-label.svg');
        }

        &.pipeline-label {
          background-image: url('@/assets/svgs/index/pipeline-label.svg');
        }

        &.doing {
          opacity: 0.5;
          cursor: not-allowed;

          &:active {
            background-color: transparent;
          }
        }
      }
    }
  }
}

.project-item {
  margin-left: 0px;
}

.project-item:nth-child(5n) {
  margin-right: 0px;
}
</style>
