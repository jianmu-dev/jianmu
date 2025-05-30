<template>
  <div :class="['project-item', isMove ? 'move' : '']" v-if="isMoveMode">
    <div
      :class="{
        'state-bar': true,
        [project.status.toLowerCase()]: true,
      }"
    ></div>
    <div class="content">
      <div class="content-top">
        <span class="concurrent" v-if="concurrentVal">{{ t('projectItem.concurrent') }}</span>
        <router-link
          :to="{
            name: 'workflow-execution-record-detail',
            query: { projectId: project.id },
          }"
        >
          <jm-text-viewer :value="project.name" :class="{ title: true, disabled: !enabled }" />
        </router-link>
        <jm-tooltip v-if="project.triggerType === TriggerTypeEnum.CRON" :content="alarmTip" placement="top">
          <i class="alarm" />
        </jm-tooltip>
      </div>
      <div class="content-center">
        <div class="status">
          <div class="left">
            <div
              :class="{
                bar: true,
                [project.status.toLowerCase()]: true,
              }"
            ></div>
            <div class="desc">
              {{ statusDesc }}
            </div>
            <jm-tooltip
              placement="top"
              popper-class="tip"
              :append-to-body="false"
              :content="
                project.status === ProjectStatusEnum.INIT ? t('projectItem.tipInit') : t('projectItem.tipSuspended')
              "
              v-if="
                (project.status === ProjectStatusEnum.INIT && project.serialNo !== 0 && !concurrentVal) ||
                project.status === ProjectStatusEnum.SUSPENDED
              "
            >
              <i class="jm-icon-button-help"></i>
            </jm-tooltip>
            <div class="count" v-if="project.serialNo !== 0">#{{ executeCount }}</div>
          </div>
          <span class="stop-btn" v-show="isShowStopBtn" @click="stopProcess(project.workflowInstanceId)">{{
            t('projectItem.terminate')
          }}</span>
        </div>
        <div class="time">
          <div class="init" v-if="isShowNextTime">
            <span>{{ t('projectItem.nextTime') }}</span>
            <jm-timer :abbr="true" :end-time="project.nextTime" class="timer" />
          </div>
          <div class="executed" v-else-if="project.status !== ProjectStatusEnum.INIT">
            <span class="start-to-current" v-if="project.startTime">
              <jm-time-viewer :value="project.startTime" />
            </span>
            <span class="duration">
              <span
                >{{
                  project.status === ProjectStatusEnum.SUSPENDED
                    ? t('projectItem.suspended')
                    : t('projectItem.running')
                }}{{ t('projectItem.duration') }}</span
              >
              <jm-timer
                :abbr="true"
                :start-time="project.startTime"
                v-if="project.status === ProjectStatusEnum.RUNNING"
              />
              <jm-timer
                :abbr="true"
                :start-time="project.suspendedTime"
                v-else-if="project.status === ProjectStatusEnum.SUSPENDED"
              />
              <jm-timer
                :abbr="true"
                :start-time="project.startTime"
                :end-time="project.startTime ? project.latestTime : project.nextTime"
                v-else-if="project.status === ProjectStatusEnum.FAILED"
              ></jm-timer>
              <jm-timer
                :abbr="true"
                :start-time="project.startTime"
                :end-time="project.latestTime"
                v-else-if="project.status === ProjectStatusEnum.SUCCEEDED"
              />
            </span>
          </div>
          <div class="empty" v-else></div>
        </div>
      </div>
      <div class="content-bottom">
        <div class="operation">
          <jm-tooltip
            :content="`${enabled ? '' : t('projectItem.disabled')}${t('projectItem.trigger')}`"
            placement="bottom"
          >
            <button
              :class="{ execute: true, doing: !enabled || executing }"
              @click="execute(project.id)"
              @keypress.enter.prevent
            ></button>
          </jm-tooltip>
          <jm-tooltip v-if="project.source === DslSourceEnum.LOCAL" :content="t('projectItem.edit')" placement="bottom">
            <button class="edit" @click="edit(project.id)"></button>
          </jm-tooltip>
          <jm-tooltip v-else :content="t('projectItem.sync')" placement="bottom">
            <button
              :class="{ sync: true, doing: synchronizing }"
              @click="sync(project.id)"
              @keypress.enter.prevent
            ></button>
          </jm-tooltip>
          <jm-tooltip
            v-if="project.source === DslSourceEnum.GIT"
            :content="t('projectItem.openGit')"
            placement="bottom"
          >
            <button class="git-label" @click="openGit(project.gitRepoId)"></button>
          </jm-tooltip>
          <jm-tooltip
            v-if="project.dslType === DslTypeEnum.WORKFLOW"
            :content="t('projectItem.previewWorkflow')"
            placement="bottom"
          >
            <button class="workflow-label" @click="dslDialogFlag = true"></button>
          </jm-tooltip>
          <jm-tooltip
            v-else-if="project.dslType === DslTypeEnum.PIPELINE"
            :content="t('projectItem.previewPipeline')"
            placement="bottom"
          >
            <button class="pipeline-label" @click="dslDialogFlag = true"></button>
          </jm-tooltip>
          <jm-tooltip v-if="project.triggerType === TriggerTypeEnum.WEBHOOK" content="Webhook" placement="bottom">
            <button class="webhook" @click="webhookDrawerFlag = true"></button>
          </jm-tooltip>
        </div>
        <div class="more">
          <jm-dropdown trigger="click" placement="bottom-start">
            <span class="el-dropdown-link">
              <div class="btn-group"></div>
            </span>
            <template #dropdown>
              <jm-dropdown-menu>
                <jm-dropdown-item :disabled="abling" @click="able(project.id)">
                  <a
                    href="javascript: void(0)"
                    :class="enabled ? 'jm-icon-button-disable' : 'jm-icon-button-off'"
                    style="width: 90px; display: inline-block"
                    >{{ enabled ? t('projectItem.disable') : t('projectItem.enable') }}</a
                  >
                </jm-dropdown-item>
                <jm-dropdown-item :disabled="deleting" @click="del(project.id)">
                  <a
                    href="javascript: void(0)"
                    class="jm-icon-button-delete"
                    style="width: 90px; display: inline-block"
                    >{{ t('projectItem.delete') }}</a
                  >
                </jm-dropdown-item>
              </jm-dropdown-menu>
            </template>
          </jm-dropdown>
        </div>
      </div>
    </div>
    <div class="cover"></div>
    <webhook-setting-dialog
      v-model="visible"
      v-if="visible"
      :project-id="project.id"
      @submit="payload => manualTriggerWebhook(payload)"
      ref="webhookSettingDialog"
    />
  </div>
  <div class="project-item" v-else>
    <div
      :class="{
        'state-bar': true,
        [project.status.toLowerCase()]: true,
      }"
    ></div>
    <div class="content">
      <div class="content-top">
        <span class="concurrent" v-if="concurrentVal">{{ t('projectItem.concurrent') }}</span>
        <router-link
          :to="{
            name: 'workflow-execution-record-detail',
            query: { projectId: project.id },
          }"
        >
          <jm-text-viewer :value="project.name" :class="{ title: true, disabled: !enabled }" />
        </router-link>
        <jm-tooltip v-if="project.triggerType === TriggerTypeEnum.CRON" :content="alarmTip" placement="top">
          <i class="alarm" />
        </jm-tooltip>
      </div>
      <div class="content-center">
        <div class="status">
          <div class="left">
            <div
              :class="{
                bar: true,
                [project.status.toLowerCase()]: true,
              }"
            ></div>
            <div class="desc">
              {{ statusDesc }}
            </div>
            <jm-tooltip
              placement="top"
              popper-class="tip"
              :append-to-body="false"
              :content="
                project.status === ProjectStatusEnum.INIT ? t('projectItem.tipInit') : t('projectItem.tipSuspended')
              "
              v-if="
                (project.status === ProjectStatusEnum.INIT && project.serialNo !== 0 && !concurrentVal) ||
                project.status === ProjectStatusEnum.SUSPENDED
              "
            >
              <i class="jm-icon-button-help"></i>
            </jm-tooltip>
            <div class="count" v-if="project.serialNo !== 0">#{{ executeCount }}</div>
          </div>
          <span class="stop-btn" v-show="isShowStopBtn" @click="stopProcess(project.workflowInstanceId)">{{
            t('projectItem.terminate')
          }}</span>
        </div>
        <div class="time">
          <div class="init" v-if="isShowNextTime">
            <span>{{ t('projectItem.nextTime') }}</span>
            <jm-timer :abbr="true" :end-time="project.nextTime" class="timer" />
          </div>
          <div class="executed" v-else-if="project.status !== ProjectStatusEnum.INIT">
            <span class="start-to-current" v-if="project.startTime">
              <jm-time-viewer :value="startTime" />
            </span>
            <span class="duration">
              <span
                >{{
                  project.status === ProjectStatusEnum.SUSPENDED
                    ? t('projectItem.suspended')
                    : t('projectItem.running')
                }}{{ t('projectItem.duration') }}</span
              >
              <jm-timer
                :abbr="true"
                :start-time="project.startTime"
                v-if="project.status === ProjectStatusEnum.RUNNING"
              />
              <jm-timer
                :abbr="true"
                :start-time="project.suspendedTime"
                v-else-if="project.status === ProjectStatusEnum.SUSPENDED"
              />
              <jm-timer
                :abbr="true"
                :start-time="project.startTime"
                :end-time="project.startTime ? project.latestTime : project.nextTime"
                v-else-if="project.status === ProjectStatusEnum.FAILED"
              ></jm-timer>
              <jm-timer
                :abbr="true"
                :start-time="project.startTime"
                :end-time="project.latestTime"
                v-else-if="project.status === ProjectStatusEnum.SUCCEEDED"
              />
            </span>
          </div>
          <div class="empty" v-else></div>
        </div>
      </div>
      <div class="content-bottom">
        <div class="operation">
          <jm-tooltip
            :content="`${enabled ? '' : t('projectItem.disabled')}${t('projectItem.trigger')}`"
            placement="bottom"
          >
            <button
              :class="{ execute: true, doing: !enabled || executing }"
              @click="execute(project.id)"
              @keypress.enter.prevent
            ></button>
          </jm-tooltip>
          <jm-tooltip v-if="project.source === DslSourceEnum.LOCAL" :content="t('projectItem.edit')" placement="bottom">
            <button class="edit" @click="edit(project.id)"></button>
          </jm-tooltip>
          <jm-tooltip v-else :content="t('projectItem.sync')" placement="bottom">
            <button
              :class="{ sync: true, doing: synchronizing }"
              @click="sync(project.id)"
              @keypress.enter.prevent
            ></button>
          </jm-tooltip>
          <jm-tooltip
            v-if="project.source === DslSourceEnum.GIT"
            :content="t('projectItem.openGit')"
            placement="bottom"
          >
            <button class="git-label" @click="openGit(project.gitRepoId)"></button>
          </jm-tooltip>
          <jm-tooltip
            v-if="project.dslType === DslTypeEnum.WORKFLOW"
            :content="t('projectItem.previewWorkflow')"
            placement="bottom"
          >
            <button class="workflow-label" @click="dslDialogFlag = true"></button>
          </jm-tooltip>
          <jm-tooltip
            v-else-if="project.dslType === DslTypeEnum.PIPELINE"
            :content="t('projectItem.previewPipeline')"
            placement="bottom"
          >
            <button class="pipeline-label" @click="dslDialogFlag = true"></button>
          </jm-tooltip>
          <jm-tooltip v-if="project.triggerType === TriggerTypeEnum.WEBHOOK" content="Webhook" placement="bottom">
            <button class="webhook" @click="webhookDrawerFlag = true"></button>
          </jm-tooltip>
        </div>
        <div class="more">
          <jm-dropdown trigger="click" placement="bottom-start">
            <span class="el-dropdown-link">
              <div class="btn-group"></div>
            </span>
            <template #dropdown>
              <jm-dropdown-menu>
                <jm-dropdown-item v-if="project.caches" @click="() => (cacheDrawerFlag = true)">
                  <a
                    href="javascript: void(0)"
                    class="jm-icon-workflow-cache"
                    style="width: 90px; display: inline-block"
                    >{{ t('projectItem.cache') }}</a
                  >
                </jm-dropdown-item>
                <jm-dropdown-item :disabled="abling" @click="able(project.id)">
                  <a
                    href="javascript: void(0)"
                    :class="enabled ? 'jm-icon-button-disable' : 'jm-icon-button-off'"
                    style="width: 90px; display: inline-block"
                    >{{ enabled ? t('projectItem.disable') : t('projectItem.enable') }}</a
                  >
                </jm-dropdown-item>
                <jm-dropdown-item :disabled="deleting" @click="del(project.id)">
                  <a
                    href="javascript: void(0)"
                    class="jm-icon-button-delete"
                    style="width: 90px; display: inline-block"
                    >{{ t('projectItem.delete') }}</a
                  >
                </jm-dropdown-item>
              </jm-dropdown-menu>
            </template>
          </jm-dropdown>
        </div>
      </div>
    </div>
    <webhook-drawer
      :current-project-id="project.id"
      :current-project-name="project.name"
      v-model:webhookVisible="webhookDrawerFlag"
    ></webhook-drawer>
    <cache-drawer
      v-if="cacheDrawerFlag"
      v-model="cacheDrawerFlag"
      :current-project-name="project.name"
      :current-project-workflow-ref="project.workflowRef"
    ></cache-drawer>
    <project-preview-dialog v-if="dslDialogFlag" :project-id="project.id" @close="dslDialogFlag = false" />
    <div class="cover"></div>
    <webhook-setting-dialog
      v-model="visible"
      v-if="visible"
      :project-id="project.id"
      :webhook-definition="webhookDefinition"
      @submit="payload => manualTriggerWebhook(payload)"
    />
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, PropType, ref, SetupContext } from 'vue';
import { DslSourceEnum, DslTypeEnum, ProjectStatusEnum, TriggerTypeEnum } from '@/api/dto/enumeration';
import { IProjectTriggeringDto, IProjectVo, ITriggerDefinitionVo } from '@/api/dto/project';
import { active, del, executeImmediately, fetchWebhookDefinition, synchronize } from '@/api/project';
import { datetimeFormatter } from '@/utils/formatter';
import ProjectPreviewDialog from './project-preview-dialog.vue';
import WebhookDrawer from './webhook-drawer.vue';
import CacheDrawer from '@/views/common/cache-drawer.vue';
import { useRouter } from 'vue-router';
import { terminate } from '@/api/workflow-execution-record';
import dayjs from 'dayjs';
import WebhookSettingDialog from '@/views/common/webhook-setting-dialog.vue';
import { useLocale } from '@/utils/i18n';

export default defineComponent({
  components: { WebhookSettingDialog, ProjectPreviewDialog, WebhookDrawer, CacheDrawer },
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
    // 控制项目是否展示可并发
    concurrent: {
      type: [Boolean, Number],
      default: false,
    },
  },
  emits: ['triggered', 'synchronized', 'deleted', 'terminated'],
  setup(props: any, { emit }: SetupContext) {
    const { t } = useLocale();
    const { proxy } = getCurrentInstance() as any;
    const router = useRouter();
    const isMove = computed<boolean>(() => props.move);
    const isMoveMode = computed<boolean>(() => props.moveMode);
    const executing = ref<boolean>(false);
    const abling = ref<boolean>(false);
    const synchronizing = ref<boolean>(false);
    const deleting = ref<boolean>(false);
    const enabled = ref<boolean>(props.project.enabled);
    const dslDialogFlag = ref<boolean>(false);
    const webhookDrawerFlag = ref<boolean>(false);
    const cacheDrawerFlag = ref<boolean>(false);
    const concurrentVal = computed(() => {
      if (typeof props.concurrent === 'number') {
        return props.concurrent !== 1;
      } else {
        return props.concurrent;
      }
    });
    // 控制终止按钮显隐
    const isShowStopBtn = computed<boolean>(
      () =>
        props.project.status === ProjectStatusEnum.RUNNING ||
        props.project.status === ProjectStatusEnum.SUSPENDED ||
        (props.project.status === ProjectStatusEnum.INIT && props.project.serialNo !== 0),
    );
    // 状态描述
    const statusDesc = computed<string>(() => {
      switch (props.project.status) {
        case ProjectStatusEnum.SUSPENDED:
          return t('projectItem.suspended');
        case ProjectStatusEnum.FAILED:
          return t('projectItem.failed');
        case ProjectStatusEnum.RUNNING:
          return t('projectItem.Running');
        case ProjectStatusEnum.SUCCEEDED:
          return t('projectItem.succeeded');
        default:
          return props.project.serialNo === 0 ? t('projectItem.notStarted') : t('projectItem.pending');
      }
    });
    // alarm 提示
    const alarmTip = computed<string>(
      () => `${t('projectItem.nextTimeTip')} ${dayjs(props.project.nextTime).format('MM-DD HH:mm')}`,
    );
    // 控制是否显示下一次执行时间
    const isShowNextTime = computed<boolean>(() => {
      const { status, nextTime } = props.project;
      return (
        nextTime &&
        (status === ProjectStatusEnum.FAILED ||
          status === ProjectStatusEnum.SUCCEEDED ||
          status === ProjectStatusEnum.INIT)
      );
    });
    const stopProcess = (id: string) => {
      proxy
        .$confirm(t('projectItem.confirmTerminate'), t('projectItem.terminateTitle'), {
          confirmButtonText: t('projectItem.confirm'),
          cancelButtonText: t('projectItem.cancel'),
          type: 'info',
        })
        .then(() => {
          terminate(id)
            .then(() => {
              proxy.$success(t('projectItem.terminateSuccess'));
              // 终止项目
              emit('terminated', id);
            })
            .catch((err: Error) => proxy.$throw(err, proxy));
        });
    };
    const startTime = computed<string>(() => props.project.startTime);
    // 项目执行次数
    const executeCount = computed<number>(() => props.project.serialNo);
    const visible = ref<boolean>(false);
    const webhookDefinition = ref<ITriggerDefinitionVo>();
    const manualTriggerWebhook = async (payload: IProjectTriggeringDto) => {
      try {
        executing.value = true;
        await executeImmediately(props.project.id, payload);
        proxy.$success(t('projectItem.operationSuccess'));
        visible.value = false;
        emit('triggered', props.project.id);
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        executing.value = false;
      }
    };
    return {
      t,
      visible,
      webhookDefinition,
      executeCount,
      startTime,
      stopProcess,
      isShowNextTime,
      alarmTip,
      statusDesc,
      isShowStopBtn,
      isMoveMode,
      isMove,
      DslSourceEnum,
      DslTypeEnum,
      ProjectStatusEnum,
      TriggerTypeEnum,
      datetimeFormatter,
      executing,
      abling,
      synchronizing,
      deleting,
      enabled,
      dslDialogFlag,
      webhookDrawerFlag,
      cacheDrawerFlag,
      concurrentVal,
      manualTriggerWebhook,
      execute: async (id: string) => {
        if (!enabled.value || executing.value) {
          return;
        }

        const { triggerType } = props.project;
        const isWarning = triggerType === TriggerTypeEnum.WEBHOOK;

        const msg = `<div>${t('projectItem.confirmTrigger')}</div>`;
        if (isWarning) {
          // 获取webhook触发器定义
          webhookDefinition.value = await fetchWebhookDefinition(id);
          // 如果没有webhook触发器参数定义则可直接触发
          if (!webhookDefinition.value.params) {
            proxy
              .$confirm(msg, t('projectItem.triggerTitle'), {
                confirmButtonText: t('projectItem.confirm'),
                cancelButtonText: t('projectItem.cancel'),
                type: 'info',
                dangerouslyUseHTMLString: true,
              })
              .then(() => {
                executing.value = true;

                executeImmediately(id)
                  .then(() => {
                    proxy.$success(t('projectItem.operationSuccess'));
                    executing.value = false;
                    emit('triggered', id);
                  })
                  .catch((err: Error) => {
                    proxy.$throw(err, proxy);
                    executing.value = false;
                  });
              });
          } else {
            // 打开webhook配置弹窗
            visible.value = true;
          }
        } else {
          proxy
            .$confirm(msg, t('projectItem.triggerTitle'), {
              confirmButtonText: t('projectItem.confirm'),
              cancelButtonText: t('projectItem.cancel'),
              type: isWarning ? 'warning' : 'info',
              dangerouslyUseHTMLString: true,
            })
            .then(() => {
              executing.value = true;

              executeImmediately(id)
                .then(() => {
                  proxy.$success(t('projectItem.operationSuccess'));
                  executing.value = false;
                  emit('triggered', id);
                })
                .catch((err: Error) => {
                  proxy.$throw(err, proxy);
                  executing.value = false;
                });
            });
        }
      },
      able: (id: string) => {
        if (abling.value) {
          return;
        }

        const str = enabled.value ? t('projectItem.disable') : t('projectItem.enable');
        const msg = props.project.mutable
          ? `
          <div>
            <span>${t('projectItem.confirm')}${str}?</span>
            <a href="https://v2.jianmu.dev/guide/global.html" target="_blank" class="jm-icon-button-help"></a>
          </div>
        `
          : `
          <div>
            <span>${enabled.value ? t('projectItem.enabled') : t('projectItem.Disabled')}，${t(
  'projectItem.notModifiable',
)}</span>
            <a href="https://v2.jianmu.dev/guide/global.html" target="_blank" class="jm-icon-button-help"></a>
          </div>
          <div style="color: red; margin-top: 5px; font-size: 12px; line-height: normal;">${t(
    'projectItem.dslUpdateTip',
  )}</div>
        `;

        proxy
          .$confirm(msg, `${str}${t('projectItem.project')}`, {
            showConfirmButton: props.project.mutable,
            confirmButtonText: t('projectItem.confirm'),
            cancelButtonText: props.project.mutable ? t('projectItem.cancel') : t('projectItem.close'),
            type: 'info',
            dangerouslyUseHTMLString: true,
          })
          .then(async () => {
            abling.value = true;
            try {
              await active(id, !enabled.value);
              enabled.value = !enabled.value;

              proxy.$success(enabled.value ? t('projectItem.enabled') : t('projectItem.Disabled'));
            } catch (err) {
              proxy.$throw(err, proxy);
            } finally {
              abling.value = false;
            }
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
          .$confirm(t('projectItem.confirmSync'), t('projectItem.syncTitle'), {
            confirmButtonText: t('projectItem.confirm'),
            cancelButtonText: t('projectItem.cancel'),
            type: 'info',
          })
          .then(() => {
            synchronizing.value = true;

            synchronize(id)
              .then(() => {
                proxy.$success(t('projectItem.syncSuccess'));
                synchronizing.value = false;

                emit('synchronized', id);
              })
              .catch((err: Error) => {
                proxy.$throw(err, proxy);
                synchronizing.value = false;
              });
          });
      },
      del: (id: string) => {
        if (deleting.value) {
          return;
        }

        const { name } = props.project;

        let msg = `<div>${t('projectItem.confirmDelete')}</div>`;
        msg += `<div style="...">${t('projectItem.name')}${name}</div>`;
        proxy
          .$confirm(msg, t('projectItem.deleteTitle'), {
            confirmButtonText: t('projectItem.confirm'),
            cancelButtonText: t('projectItem.cancel'),
            type: 'warning',
            dangerouslyUseHTMLString: true,
          })
          .then(() => {
            deleting.value = true;

            del(id)
              .then(() => {
                proxy.$success(t('projectItem.deleteSuccess'));
                deleting.value = false;

                emit('deleted', id);
              })
              .catch((err: Error) => {
                proxy.$throw(err, proxy);
                deleting.value = false;
              });
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
  box-sizing: border-box;
  margin: 0.8% 0.8% 0 0;
  width: 19.2%;
  min-width: 260px;
  background-color: #ffffff;
  min-height: 166px;
  border-radius: 0px 0px 4px 4px;

  &:hover {
    box-shadow: 0px 0px 12px 4px #edf1f8;

    .content {
      border: 1px solid transparent;
      border-top: none;
    }
  }

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

  .state-bar {
    height: 6px;
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

    &.suspended {
      background-color: #7986cb;
    }
  }

  .content {
    min-height: 116px;
    position: relative;
    padding: 16px 20px 10px 20px;
    border: 1px solid #dee4eb;
    border-top: none;
    border-radius: 0px 0px 4px 4px;

    .content-top {
      min-height: 24px;
      display: flex;
      align-items: center;

      a {
        flex: 1;
      }

      .concurrent {
        height: 20px;
        line-height: 20px;
        background: #fff7e6;
        border-radius: 2px;
        padding: 3px;
        font-size: 12px;
        font-weight: 400;
        color: #6d4c41;
        margin-right: 5px;
      }

      .alarm {
        width: 24px;
        height: 24px;
        background: url('@/assets/svgs/index/alarm.svg') 100% no-repeat;
      }
    }

    .content-center {
      .status {
        margin-top: 10px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        font-size: 14px;
        color: #082340;
        font-weight: 400;

        .left {
          display: flex;
          align-items: center;

          .bar {
            width: 4px;
            height: 12px;
            border-radius: 2px;

            &.succeeded {
              background-color: #3ebb03;
            }

            &.failed {
              background-color: #cf1524;
            }

            &.suspended {
              background-color: #7986cb;
            }

            &.init {
              background-color: #979797;
            }

            &.running {
              background-color: #10c2c2;
            }
          }

          .desc {
            margin: 0 0 0 6px;
          }

          ::v-deep(.el-popper) {
            &.tip {
              width: 250px;
              line-height: 22px;
            }
          }

          .jm-icon-button-help {
            margin-left: 6px;

            &::before {
              color: #6b7b8d;
              margin: 0;
            }
          }

          .count {
            margin-left: 6px;
          }
        }

        .stop-btn {
          cursor: pointer;

          &:hover {
            color: #096dd9;
          }
        }
      }

      .time {
        margin: 10px 0 15px;
        color: #6b7b8d;
        font-size: 14px;

        .init {
          display: flex;

          .timer {
            flex: 1;
            display: inline-block;
          }
        }

        .executed {
          display: flex;

          .duration {
            display: flex;
            margin-left: 10px;
            flex: 1;

            span {
              margin-right: 5px;
            }

            .jm-timer {
              flex: 1;
            }
          }

          .jm-timer {
            display: inline-block;
          }
        }

        .empty {
          min-height: 20px;
        }
      }
    }

    .content-bottom {
      padding: 10px 0 0;
      border-top: 1px solid #dee4eb;
      display: flex;
      align-items: center;
      justify-content: space-between;

      .operation {
        min-height: 26px;
        display: flex;
        align-items: center;

        button + button {
          margin-left: 20px;
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

      .more {
        opacity: 0.65;
        cursor: pointer;

        &:hover {
          opacity: 1;
        }

        .el-dropdown-link {
          .btn-group {
            &:active {
              background-color: #eff7ff;
              border-radius: 4px;
            }

            width: 24px;
            height: 24px;
            background-image: url('@/assets/svgs/btn/more2.svg');
            transform: rotate(90deg);
          }
        }
      }
    }
  }
}
</style>
