<template>
  <div class="index" v-loading="loading">
    <div class="main">
      <div class="menu-bar">
        <div class="left-area">
          <router-link :to="{name: 'process-templates'}">
            <jm-tooltip content="新增项目" placement="top">
              <button class="add"></button>
            </jm-tooltip>
          </router-link>
          <router-link :to="{name: 'import-project'}">
            <jm-tooltip content="导入项目" placement="top">
              <button class="git"></button>
            </jm-tooltip>
          </router-link>
        </div>
        <div class="right-area">
          <router-link :to="{name: 'event-bridge'}">
            <jm-tooltip content="事件桥接器" placement="top">
              <button class="event-bridge"></button>
            </jm-tooltip>
          </router-link>
          <router-link :to="{name: 'node-library'}">
            <jm-tooltip content="本地节点库" placement="top">
              <button class="node-library"></button>
            </jm-tooltip>
          </router-link>
          <router-link :to="{name: 'secret-key'}">
            <jm-tooltip content="密钥管理" placement="top">
              <button class="secret-key"></button>
            </jm-tooltip>
          </router-link>
        </div>
      </div>
      <div class="separator">全部项目</div>
      <div class="group">
        <div class="name">
          <span>默认分组</span>
          <span class="desc">（共有 {{ projects.length }} 个项目）</span>
        </div>
        <div class="projects">
          <jm-empty v-if="projects.length === 0"/>
          <div v-else v-for="project of projects" :key="project.id" class="item">
            <div :class="{'state-bar': true, [project.status.toLowerCase()]: true}"></div>
            <div class="content">
              <jm-tooltip v-if="project.source === DslSourceEnum.GIT" content="打开git仓库" placement="bottom">
                <a :class="{'git-label': true, [project.status === ProjectStatusEnum.INIT? 'init': 'normal']: true}"
                   :href="`/view/repo/${project.gitRepoId}`"
                   target="_blank"></a>
              </jm-tooltip>
              <router-link :to="{name: 'workflow-execution-record-detail', query: { projectId: project.id }}">
                <jm-tooltip :content="project.name" placement="top">
                  <div class="title ellipsis">{{ project.name }}</div>
                </jm-tooltip>
              </router-link>
              <div class="time">最后执行时间：{{ datetimeFormatter(project.latestTime) }}</div>
              <div class="time">下次执行时间：{{ datetimeFormatter(project.nextTime) }}</div>
              <div class="operation">
                <jm-tooltip content="触发" placement="bottom">
                  <button :class="{execute: true, doing: executings[project.id]}" @click="execute(project.id)"></button>
                </jm-tooltip>
                <jm-tooltip v-if="project.eventBridgeId" content="Webhook" placement="bottom">
                  <button class="webhook" @click="selectedWebhookId = project.eventBridgeId"></button>
                </jm-tooltip>
                <!--                <jm-tooltip v-if="project.eventBridgeId" content="Webhook" placement="bottom">-->
                <!--                  <button class="webhook"-->
                <!--                          @click="$router.push({name: 'event-bridge-detail', params: {id: project.eventBridgeId}})"></button>-->
                <!--                </jm-tooltip>-->
                <jm-tooltip v-if="project.source === DslSourceEnum.LOCAL" content="编辑" placement="bottom">
                  <button class="edit" @click="edit(project.id)"></button>
                </jm-tooltip>
                <jm-tooltip v-else content="同步DSL" placement="bottom">
                  <button :class="{sync: true, doing: synchronizings[project.id]}" @click="sync(project.id)"></button>
                </jm-tooltip>
                <jm-tooltip v-if="project.dslType === DslTypeEnum.WORKFLOW" content="查看流程DSL" placement="bottom">
                  <button class="workflow-label" @click="viewDsl(project)"></button>
                </jm-tooltip>
                <jm-tooltip v-else-if="project.dslType === DslTypeEnum.PIPELINE" content="查看管道DSL" placement="bottom">
                  <button class="pipeline-label" @click="viewDsl(project)"></button>
                </jm-tooltip>
                <jm-tooltip content="删除" placement="top">
                  <button :class="{del: true, doing: deletings[project.id]}" @click="del(project.id)"></button>
                </jm-tooltip>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <bottom-nav/>
    <webhook-dialog v-if="selectedWebhookId" :event-bridge-id="selectedWebhookId"
                    @close="selectedWebhookId = undefined"/>
    <dsl-dialog v-if="selectedDslId && selectedDslType"
                :project-id="selectedDslId"
                :dsl-type="selectedDslType"
                @close="selectedDslId = undefined; selectedDslType = undefined;"/>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, onBeforeMount, onBeforeUnmount, ref } from 'vue';
import { IProjectVo } from '@/api/dto/project';
import { queryProject } from '@/api/view-no-auth';
import { IQueryForm } from '@/model/modules/project';
import { DslSourceEnum, DslTypeEnum, ProjectStatusEnum } from '@/api/dto/enumeration';
import { del, executeImmediately, synchronize } from '@/api/project';
import router from '@/router';
import { datetimeFormatter } from '@/utils/formatter';
import WebhookDialog from './webhook-dialog.vue';
import DslDialog from './dsl-dialog.vue';
import BottomNav from '@/views/nav/bottom2.vue';
import { HttpError, TimeoutError } from '@/utils/rest/error';

const MAX_AUTO_REFRESHING_OF_NO_RUNNING_COUNT = 5;

export default defineComponent({
  components: { BottomNav, WebhookDialog, DslDialog },
  setup() {
    const { proxy } = getCurrentInstance() as any;
    const loading = ref<boolean>(false);
    const projects = ref<IProjectVo[]>([]);
    const queryForm = ref<IQueryForm>({});
    const executings = ref<{ [projectId: string]: boolean }>({});
    const synchronizings = ref<{ [projectId: string]: boolean }>({});
    const deletings = ref<{ [projectId: string]: boolean }>({});
    const autoRefreshingOfNoRunningCount = ref<number>(0);
    const selectedWebhookId = ref<string>();
    const selectedDslId = ref<string>();
    const selectedDslType = ref<DslTypeEnum>();

    console.log('开启自动刷新项目列表');
    const autoRefreshingInterval = setInterval(async () => {
      if (!projects.value.find(item => item.status === ProjectStatusEnum.RUNNING)) {
        // 不存在running场景
        if (autoRefreshingOfNoRunningCount.value < MAX_AUTO_REFRESHING_OF_NO_RUNNING_COUNT) {
          autoRefreshingOfNoRunningCount.value++;
          return;
        } else {
          console.debug('刷新项目列表，检查是否存在running中的项目');
        }
      } else {
        console.debug('存在running中的项目，刷新项目列表');
      }
      autoRefreshingOfNoRunningCount.value = 0;

      try {
        projects.value = await queryProject({ ...queryForm.value });
      } catch (err) {
        if (err instanceof TimeoutError) {
          // 忽略超时错误
          console.warn(err.message);
        } else if (err instanceof HttpError) {
          const { response } = err as HttpError;

          if (response && response.status !== 502) {
            throw err;
          }

          // 忽略错误
          console.warn(err.message);
        }
      }
    }, 3000);

    const loadProject = async () => {
      try {
        loading.value = true;
        projects.value = await queryProject({ ...queryForm.value });
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        loading.value = false;
      }
    };

    // 初始化项目列表
    onBeforeMount(() => loadProject());

    onBeforeUnmount(() => {
      console.log('终止自动刷新项目列表');
      clearInterval(autoRefreshingInterval);
    });

    return {
      loading,
      DslSourceEnum,
      DslTypeEnum,
      ProjectStatusEnum,
      datetimeFormatter,
      projects,
      queryForm,
      executings,
      synchronizings,
      deletings,
      selectedWebhookId,
      selectedDslId,
      selectedDslType,
      execute: (id: string) => {
        if (executings.value[id]) {
          return;
        }

        const { eventBridgeId } = projects.value.find(item => item.id === id) as IProjectVo;
        const isWarning = !!eventBridgeId;

        let msg = '<div>确定要触发吗?</div>';
        if (isWarning) {
          msg += '<div style="color: red; margin-top: 5px; font-size: 12px; line-height: normal;">注意：项目已关联事件桥接器，手动触发可能会导致不可预知的结果，请慎重操作。</div>';
        }

        proxy.$confirm(msg, '触发项目执行', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: isWarning ? 'warning' : 'info',
          dangerouslyUseHTMLString: true,
        }).then(() => {
          executings.value[id] = true;

          executeImmediately(id).then(() => {
            proxy.$success('操作成功');

            delete executings.value[id];

            const index = projects.value.findIndex(item => item.id === id);
            projects.value[index] = {
              ...projects.value[index],
              status: ProjectStatusEnum.RUNNING,
            };
          }).catch((err: Error) => {
            proxy.$throw(err, proxy);

            delete executings.value[id];
          });
        }).catch(() => {
        });
      },
      edit: (id: string) => {
        router.push({ name: 'update-project', params: { id } });
      },
      sync: (id: string) => {
        if (synchronizings.value[id]) {
          return;
        }

        proxy.$confirm('确定要同步吗?', '同步DSL', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info',
        }).then(() => {
          synchronizings.value[id] = true;

          synchronize(id).then(() => {
            proxy.$success('同步成功');

            delete synchronizings.value[id];

            // 刷新项目列表，保留查询状态
            loadProject();
          }).catch((err: Error) => {
            proxy.$throw(err, proxy);

            delete synchronizings.value[id];
          });
        }).catch(() => {
        });
      },
      del: (id: string) => {
        if (deletings.value[id]) {
          return;
        }

        const { name } = projects.value.find(item => item.id === id) as IProjectVo;

        let msg = '<div>确定要删除项目吗?</div>';
        msg += `<div style="margin-top: 5px; font-size: 12px; line-height: normal;">名称：${name}</div>`;

        proxy.$confirm(msg, '删除项目', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: true,
        }).then(() => {
          deletings.value[id] = true;

          del(id).then(() => {
            proxy.$success('删除成功');

            delete deletings.value[id];

            const index = projects.value.findIndex(item => item.id === id);
            projects.value.splice(index, 1);
          }).catch((err: Error) => {
            proxy.$throw(err, proxy);

            delete deletings.value[id];
          });
        }).catch(() => {
        });
      },
      viewDsl: ({ id, dslType }: IProjectVo) => {
        selectedDslId.value = id;
        selectedDslType.value = dslType;
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

.index {
  .main {
    min-height: calc(100vh - 125px);

    .menu-bar {
      padding: 45px 0;
      display: flex;
      justify-content: space-between;
      align-items: center;

      .left-area {
        white-space: nowrap;

        button {
          width: 186px;
          height: 64px;
          background-color: #FFFFFF;
          box-shadow: 0 6px 14px 0 #ACC3EE;
          border-radius: 4px;
          border: 0;
          background-position: center center;
          background-repeat: no-repeat;
          cursor: pointer;

          &:active {
            opacity: 0.8;
          }

          &.add {
            margin-left: 14px;
            background-image: url('@/assets/svgs/index/add-btn.svg');
          }

          &.git {
            margin-left: 40px;
            background-image: url('@/assets/svgs/index/git-btn.svg');
          }
        }
      }

      .right-area {
        margin-right: 0.5%;

        button {
          width: 48px;
          height: 48px;
          background-color: transparent;
          border: 0;
          background-position: center center;
          background-repeat: no-repeat;
          cursor: pointer;

          &:active {
            opacity: 0.8;
          }

          &.hub {
            background-image: url('@/assets/svgs/index/hub-btn.svg');
          }

          &.event-bridge {
            background-image: url('@/assets/svgs/index/event-bridge-btn.svg');
          }

          &.node-library {
            margin-left: 40px;
            background-image: url('@/assets/svgs/index/node-library-btn.svg');
          }

          &.secret-key {
            margin-left: 40px;
            background-image: url('@/assets/svgs/index/secret-key-btn.svg');
          }
        }
      }
    }

    .separator {
      margin-left: 0.5%;
      font-size: 24px;
      font-weight: bold;
      color: #082340;
      opacity: 0.5;
    }

    .group {
      margin-top: 30px;

      .name {
        margin-left: 0.5%;
        margin-bottom: 20px;
        font-size: 18px;
        font-weight: bold;
        color: #082340;

        .desc {
          margin-left: 12px;
          font-size: 14px;
          color: #082340;
          opacity: 0.46;
        }
      }

      .projects {
        display: flex;
        flex-wrap: wrap;

        .item {
          margin: 0.5%;
          width: 19%;
          min-width: 260px;
          background-color: #FFFFFF;
          box-shadow: 0 0 8px 0 #9EB1C5;

          //&:hover {
          //  .content {
          //    .operation {
          //      button.del {
          //        display: block;
          //      }
          //    }
          //  }
          //}

          .state-bar {
            height: 8px;
            overflow: hidden;

            &.init {
              background-color: #979797;
            }

            &.running {
              background-image: repeating-linear-gradient(115deg, #10C2C2 0px, #58D4D4 1px, #58D4D4 10px, #10C2C2 11px, #10C2C2 16px);
              background-size: 106px 114px;
              animation: 3s linear 0s infinite normal none running workflow-running;
            }

            &.succeeded {
              background-color: #3EBB03;
            }

            &.failed {
              background-color: #CF1524;
            }
          }

          .content {
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
              font-size: 20px;
              font-weight: bold;
              color: #082340;

              &:hover {
                text-decoration: underline;
              }
            }

            .time {
              margin-top: 6px;
              font-size: 13px;
              color: #6B7B8D;
            }

            .operation {
              margin-top: 10px;
              display: flex;
              align-items: center;

              button + button {
                margin-left: 10px;
              }

              button {
                width: 30px;
                height: 30px;
                background-color: transparent;
                border: 0;
                background-position: center center;
                background-repeat: no-repeat;
                cursor: pointer;

                &:active {
                  background-color: #EFF7FF;
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

                &.del {
                  position: absolute;
                  right: 3px;
                  top: 5px;
                  width: 22px;
                  height: 22px;
                  //display: none;
                  background-image: url('@/assets/svgs/btn/del.svg');
                  background-size: contain;
                  opacity: 0.65;

                  &:hover {
                    opacity: 1;
                  }
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

            .ellipsis {
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }
          }
        }
      }
    }
  }
}
</style>