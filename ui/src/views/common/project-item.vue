<template>
  <div :class="['project-item', isMove ? 'move' : '']" v-if="isMoveMode">
    <div
      :class="{
        'state-bar': true,
        [project.status.toLowerCase()]: true,
      }"
    ></div>
    <div class="content">
      <jm-tooltip
        v-if="project.source === DslSourceEnum.GIT"
        content="打开git仓库"
        placement="bottom"
      >
        <a
          :class="{
            'git-label': true,
            [project.status === ProjectStatusEnum.INIT
              ? 'init'
              : 'normal']: true,
          }"
          :href="`/view/repo/${project.gitRepoId}`"
          target="_blank"
        ></a>
      </jm-tooltip>
      <router-link
        :to="{
          name: 'workflow-execution-record-detail',
          query: { projectId: project.id },
        }"
      >
        <jm-tooltip :content="project.name" placement="top">
          <div class="title ellipsis">{{ project.name }}</div>
        </jm-tooltip>
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
      <div class="operation" v-if="!isMoveMode">
        <jm-tooltip content="触发" placement="bottom">
          <button
            :class="{ execute: true, doing: executing }"
            @click="execute(project.id)"
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
          v-if="project.dslType === DslTypeEnum.WORKFLOW"
          content="查看流程DSL"
          placement="bottom"
        >
          <button class="workflow-label" @click="dslDialogFlag = true"></button>
        </jm-tooltip>
        <jm-tooltip
          v-else-if="project.dslType === DslTypeEnum.PIPELINE"
          content="查看管道DSL"
          placement="bottom"
        >
          <button class="pipeline-label" @click="dslDialogFlag = true"></button>
        </jm-tooltip>
        <jm-tooltip content="删除" placement="top">
          <button
            :class="{ del: true, doing: deleting }"
            @click="del(project.id)"
          ></button>
        </jm-tooltip>
      </div>
    </div>
    <webhook-drawer
      :current-project-id="project.id"
      v-model:webhookVisible="webhookDrawerFlag"
    ></webhook-drawer>
    <dsl-dialog
      v-if="dslDialogFlag"
      :project-id="project.id"
      :dsl-type="project.dslType"
      @close="dslDialogFlag = false"
    />
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
      <jm-tooltip
        v-if="project.source === DslSourceEnum.GIT"
        content="打开git仓库"
        placement="bottom"
      >
        <a
          :class="{
            'git-label': true,
            [project.status === ProjectStatusEnum.INIT
              ? 'init'
              : 'normal']: true,
          }"
          :href="`/view/repo/${project.gitRepoId}`"
          target="_blank"
        ></a>
      </jm-tooltip>
      <router-link
        :to="{
          name: 'workflow-execution-record-detail',
          query: { projectId: project.id },
        }"
      >
        <jm-tooltip :content="project.name" placement="top">
          <div class="title ellipsis">{{ project.name }}</div>
        </jm-tooltip>
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
        <jm-tooltip content="触发" placement="bottom">
          <button
            :class="{ execute: true, doing: executing }"
            @click="execute(project.id)"
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
          v-if="project.dslType === DslTypeEnum.WORKFLOW"
          content="查看流程DSL"
          placement="bottom"
        >
          <button class="workflow-label" @click="dslDialogFlag = true"></button>
        </jm-tooltip>
        <jm-tooltip
          v-else-if="project.dslType === DslTypeEnum.PIPELINE"
          content="查看管道DSL"
          placement="bottom"
        >
          <button class="pipeline-label" @click="dslDialogFlag = true"></button>
        </jm-tooltip>
        <jm-tooltip content="删除" placement="top">
          <button
            :class="{ del: true, doing: deleting }"
            @click="del(project.id)"
          ></button>
        </jm-tooltip>
      </div>
    </div>
    <webhook-drawer
      :current-project-id="project.id"
      v-model:webhookVisible="webhookDrawerFlag"
    ></webhook-drawer>
    <dsl-dialog
      v-if="dslDialogFlag"
      :project-id="project.id"
      :dsl-type="project.dslType"
      @close="dslDialogFlag = false"
    />
    <div class="cover"></div>
  </div>
</template>

<script lang="ts">
import {
  defineComponent,
  getCurrentInstance,
  PropType,
  ref,
  SetupContext,
  computed,
} from 'vue';
import {
  DslSourceEnum,
  DslTypeEnum,
  ProjectStatusEnum,
  TriggerTypeEnum,
} from '@/api/dto/enumeration';
import { IProjectVo } from '@/api/dto/project';
import { del, executeImmediately, synchronize } from '@/api/project';
import router from '@/router';
import { datetimeFormatter, executionTimeFormatter } from '@/utils/formatter';
import DslDialog from './dsl-dialog.vue';
import WebhookDrawer from './webhook-drawer.vue';

export default defineComponent({
  components: { DslDialog, WebhookDrawer },
  props: {
    project: {
      type: Object as PropType<IProjectVo>,
      required: true,
    },
    //控制item是否加上hover样式，根据对比id值判断
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
    const synchronizing = ref<boolean>(false);
    const deleting = ref<boolean>(false);
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
      synchronizing,
      deleting,
      dslDialogFlag,
      webhookDrawerFlag,
      execute: (id: string) => {
        if (executing.value) {
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
          .catch(() => {});
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
          .catch(() => {});
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
          .catch(() => {});
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
  margin: 1.1%;
  margin-bottom: 0px;
  width: 19%;
  min-width: 260px;
  background-color: #ffffff;
  box-shadow: 0px 0px 12px 4px #edf1f8;

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
      background-image: repeating-linear-gradient(
        115deg,
        #10c2c2 0px,
        #58d4d4 1px,
        #58d4d4 10px,
        #10c2c2 11px,
        #10c2c2 16px
      );
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
    padding: 16px 20px;

    .git-label {
      position: absolute;
      bottom: 0;
      right: 0;
      width: 40px;
      height: 40px;
      overflow: hidden;
      background-position: center center;
      background-repeat: no-repeat;
      cursor: pointer;

      &:active {
        opacity: 0.8;
      }

      &.normal {
        background-image: url('@/assets/svgs/index/git-label.svg');
      }

      &.init {
        background-image: url('@/assets/svgs/index/git-label2.svg');
      }
    }

    a {
      text-decoration: none;
    }

    .title {
      width: 90%;
      font-size: 16px;
      color: #082340;

      &:hover {
        text-decoration: underline;
      }
    }

    .time {
      margin-top: 6px;
      font-size: 13px;
      color: #6b7b8d;
    }

    .operation {
      margin-top: 20px;
      display: flex;
      align-items: center;

      button + button {
        margin-left: 20px;
      }

      button {
        width: 30px;
        height: 30px;
        padding: 3px;
        border: 0;
        background-repeat: no-repeat;
        cursor: pointer;
        outline: none;

        &:active {
          background-color: #eff7ff;
          border-radius: 4px;
        }

        &.execute {
          background: url('@/assets/svgs/btn/execute.svg') no-repeat;
          background-position: center center;
          border-radius: 2px;
          &:hover {
            background: url('@/assets/svgs/btn/execute.svg') no-repeat #eff7ff;
            background-position: center center;
            border-radius: 2px;
          }
        }

        &.edit {
          background: url('@/assets/svgs/btn/edit.svg') no-repeat;
          background-position: center center;
          border-radius: 2px;
          &:hover {
            background: url('@/assets/svgs/btn/edit.svg') no-repeat #eff7ff;
            background-position: center center;
            border-radius: 2px;
          }
        }

        &.sync {
          background: url('@/assets/svgs/btn/sync.svg') no-repeat;
          background-position: center center;
          border-radius: 2px;
          &:hover {
            background: url('@/assets/svgs/btn/sync.svg') no-repeat #eff7ff;
            background-position: center center;
            border-radius: 2px;
          }

          &.doing {
            animation: rotating 2s linear infinite;
          }
        }

        &.webhook {
          width: 24px;
          height: 24px;
          background: url('@/assets/svgs/btn/hook.svg') no-repeat;
          background-position: center center;
          border-radius: 2px;
          &:hover {
            background: url('@/assets/svgs/btn/hook.svg') no-repeat #eff7ff;
            background-position: center center;
            border-radius: 2px;
          }
        }

        &.del {
          position: absolute;
          right: 7px;
          top: 7px;
          background: url('@/assets/svgs/btn/del.svg') no-repeat;
          background-position: center center;
          border-radius: 2px;
          &:hover {
            background: url('@/assets/svgs/btn/del-active.svg') no-repeat
              #eff7ff;
            background-position: center center;
            border-radius: 2px;
          }

          &:hover {
            opacity: 1;
          }
        }

        &.workflow-label {
          background: url('@/assets/svgs/index/workflow-label.svg') no-repeat;
          background-position: center center;
          border-radius: 2px;
          &:hover {
            background: url('@/assets/svgs/index/workflow-label.svg') no-repeat
              #eff7ff;
            background-position: center center;
            border-radius: 2px;
          }
        }

        &.pipeline-label {
          background: url('@/assets/svgs/index/pipeline-label.svg') no-repeat;
          background-position: center center;
          border-radius: 2px;
          &:hover {
            background: url('@/assets/svgs/index/pipeline-label.svg') no-repeat
              #eff7ff;
            background-position: center center;
            border-radius: 2px;
          }
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

    .ellipsis {
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
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
