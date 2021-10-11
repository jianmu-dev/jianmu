<template>
  <div class="event-bridge-editor">
    <div class="right-top-btn">
      <jm-button type="primary" class="jm-icon-button-back" size="small" @click="close">关闭</jm-button>
    </div>
    <div v-loading="loading">
      <div class="nav">
        <div class="title">
          <jm-editable-text v-model="bridgeSubForm.name"
                            default-text="点击设置名称"
                            @change="save"/>
        </div>
        <div :class="{'auto-saved-msg': true, 'failed': autoSaveMsg === '保存失败'}">{{ autoSaveMsg }}</div>
      </div>
      <div class="content">
        <div class="sources">
          <div class="subject">来源</div>
          <div class="item">
            <jm-tooltip content="来源名称" placement="left">
              <div class="name">
                <jm-editable-text v-model="sourceSubForm.name"
                                  default-text="点击设置名称"
                                  @change="save"/>
              </div>
            </jm-tooltip>
            <jm-tooltip content="Webhook地址" placement="left">
              <div class="webhook-url">
                <jm-tooltip v-if="link" :content="link" placement="top-start" effect="light">
                  <div class="ellipsis">{{ link }}</div>
                </jm-tooltip>
                <router-link v-else :to="{name: 'login'}">登录后可查看</router-link>
              </div>
            </jm-tooltip>
            <div class="operation">
              <jm-tooltip content="重新生成链接" placement="bottom">
                <button :class="{'refresh': true, 'doing': regeneratingWebhook}" @click="regenerateWebhookUrl"></button>
              </jm-tooltip>
              <jm-tooltip :content="link? '复制链接' : '登录后，可复制链接'" placement="bottom">
                <button :class="{'copy': true, 'disabled': !link}" @click="copy(link)"></button>
              </jm-tooltip>
            </div>
          </div>
        </div>
        <div class="connection">
          <div class="line"></div>
        </div>
        <div class="targets">
          <div class="subject">目标</div>
          <div v-for="({id, ref, relatedProjectId, relatedProjectName}, index) in targetsSubForm.targets"
               class="item" :key="id" @click="showTransformerForm(index)">
            <jm-tooltip content="目标名称" placement="left">
              <div class="name">
                <jm-editable-text v-model="targetsSubForm.targets[index].name"
                                  default-text="点击设置名称"
                                  @change="save"/>
              </div>
            </jm-tooltip>
            <jm-tooltip content="目标唯一标识" placement="left">
              <div class="ref">
                <jm-tooltip v-if="relatedProjectId" effect="light" placement="top">
                  <template #content>
                    <div style="color: red">已关联项目，禁止操作，<br/>若要继续，请先在项目中，<br/>移除关联关系。</div>
                  </template>
                  <span class="setting-forbidden" @click.stop>{{ ref }}</span>
                </jm-tooltip>
                <jm-editable-text v-else
                                  v-model="targetsSubForm.targets[index].ref"
                                  default-text="点击设置唯一标识"
                                  @change="save"/>
              </div>
            </jm-tooltip>
            <div class="project">
              <jm-tooltip content="已关联项目" placement="left">
                <router-link v-if="relatedProjectId"
                             class="ellipsis" @click.stop
                             :to="{name: 'workflow-execution-record-detail', query: {projectId: relatedProjectId}}">
                  {{ relatedProjectName }}
                </router-link>
                <span v-else class="non-associated" @click.stop>尚未关联项目</span>
              </jm-tooltip>
              <div class="operation">
                <!--              <jm-tooltip content="编辑目标" placement="bottom">-->
                <!--                <button class="edit" @click.stop="showEditorForm(SubFormTypeEnum.TARGET, index)"></button>-->
                <!--              </jm-tooltip>-->
                <jm-tooltip :content="ref? '复制唯一标识' : '设置后，可复制唯一标识'" placement="bottom">
                  <button :class="{'copy': true, 'disabled': !ref}" @click.stop="copy(ref)"></button>
                </jm-tooltip>
                <jm-tooltip content="设置转换器" placement="bottom">
                  <button class="setting" @click.stop="showTransformerForm(index)"></button>
                </jm-tooltip>
                <!--            <jm-tooltip content="删除" placement="bottom">-->
                <!--              <button class="del"></button>-->
                <!--            </jm-tooltip>-->
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!--    <editor-form-dialog-->
    <!--      :form-type="editorFormType"-->
    <!--      v-model="editorFormDialogVisible"-->
    <!--      v-model:name="editorForm.name"-->
    <!--      v-model:_ref="editorForm.ref"-->
    <!--      @confirmed="save"-->
    <!--    />-->
    <transformer-form-drawer
      v-model="transformerFormDrawerVisible"
      v-model:transformers="transformerForm.transformers"
      @confirmed="confirmTransformerForm"
    />
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, inject, onBeforeMount, ref } from 'vue';
import { fetchWebhook, regenerateWebhook, saveEventBridge } from '@/api/event-bridge';
import { useRouter } from 'vue-router';
import { ISaveBridgeSubForm, ISaveSourceSubForm, ISaveTargetsSubForm } from '@/model/modules/event-bridge';
import { fetchEventBridgeDetail } from '@/api/view-no-auth';
import useClipboard from 'vue-clipboard3';
import { EventBridgeSourceTypeEnum } from '@/api/dto/enumeration';
import { IEventBridgeSavingDto, IEventBridgeTargetTransformerVo } from '@/api/dto/event-bridge';
import TransformerFormDrawer from './transformer-form-drawer.vue';
import { HttpError } from '@/utils/rest/error';

export default defineComponent({
  props: {
    id: {
      type: String,
      required: true,
    },
  },
  components: {
    // EditorFormDialog,
    TransformerFormDrawer,
  },
  setup(props) {
    const { proxy } = getCurrentInstance() as any;
    const reloadMain = inject('reloadMain') as () => void;
    const router = useRouter();
    const { toClipboard } = useClipboard();
    const loading = ref<boolean>(false);

    const bridgeSubForm = ref<ISaveBridgeSubForm>({
      name: '',
    });
    const sourceSubForm = ref<ISaveSourceSubForm>({
      name: '',
      type: EventBridgeSourceTypeEnum.WEBHOOK,
    });
    const targetsSubForm = ref<ISaveTargetsSubForm>({
      targets: [{
        // ref: '',
        name: '',
        transformers: [],
      }],
    });

    const webhook = ref<string>();
    const link = computed<string | undefined>(
      () => webhook.value && `${window.location.protocol}//${window.location.host}${webhook.value}`);

    onBeforeMount(async () => {
      try {
        loading.value = true;

        const { bridge, source, targets } = await fetchEventBridgeDetail(props.id);
        bridgeSubForm.value = bridge;
        sourceSubForm.value = source;
        targetsSubForm.value.targets = targets;
      } catch (err) {
        proxy.$throw(err, proxy);
      } finally {
        loading.value = false;
      }

      try {
        loading.value = true;

        const { webhook: webhookUri } = await fetchWebhook(sourceSubForm.value.id as string);
        webhook.value = webhookUri;
      } catch (err) {
        if (!(err instanceof HttpError)) {
          proxy.$throw(err, proxy);
          return;
        }

        const { response: { status } } = err as HttpError;

        if (status !== 401) {
          proxy.$throw(err, proxy);
          return;
        }
      } finally {
        loading.value = false;
      }
    });

    // const editorFormType = ref<SubFormTypeEnum | -1>(-1);
    // const editorFormDialogVisible = ref<boolean>(false);
    // const editorForm = ref<{
    //   name: string;
    //   ref?: string;
    // }>({
    //   // 往子组件值传递
    //   name: '',
    //   //ref: '',
    // });
    const autoSaveMsg = ref<string>('');
    const regeneratingWebhook = ref<boolean>(false);
    const transformerFormDrawerVisible = ref<boolean>(false);
    const transformerForm = ref<{
      targetIndex: number;
      transformers: IEventBridgeTargetTransformerVo[];
    }>({
      targetIndex: -1,
      // 往子组件引用传递
      transformers: [],
    });

    const save = async () => {
      autoSaveMsg.value = '正在保存...';

      try {
        const { bridge, source, targets } = await saveEventBridge({
          bridge: bridgeSubForm.value,
          source: sourceSubForm.value,
          targets: targetsSubForm.value.targets as IEventBridgeSavingDto['targets'],
        });

        bridgeSubForm.value = bridge;
        sourceSubForm.value = source;
        targetsSubForm.value.targets = targets;

        autoSaveMsg.value = '所有更改已自动保存';
      } catch (err) {
        proxy.$throw(err, proxy);

        autoSaveMsg.value = '保存失败';
      }
    };

    return {
      loading,
      bridgeSubForm,
      sourceSubForm,
      targetsSubForm,
      // SubFormTypeEnum,
      // editorFormType,
      // editorFormDialogVisible,
      // editorForm,
      autoSaveMsg,
      regeneratingWebhook,
      webhook,
      link,
      transformerFormDrawerVisible,
      transformerForm,
      close: async () => {
        await router.push({ name: 'event-bridge' });
        // 刷新列表
        reloadMain();
      },
      // showEditorForm: (formType: SubFormTypeEnum, targetIndex?: number) => {
      //   editorFormDialogVisible.value = true;
      //   editorFormType.value = formType;
      //
      //   switch (formType) {
      //     case SubFormTypeEnum.BRIDGE:
      //       editorForm.value = bridgeSubForm.value;
      //       break;
      //     case SubFormTypeEnum.SOURCE:
      //       editorForm.value = sourceSubForm.value;
      //       break;
      //     case SubFormTypeEnum.TARGET:
      //       editorForm.value = targetsSubForm.value.targets[targetIndex || 0];
      //       break;
      //   }
      // },
      save,
      regenerateWebhookUrl: () => {
        if (regeneratingWebhook.value) {
          return;
        }

        proxy.$confirm('确定要重新生成吗?', 'Webhook地址', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }).then(async () => {
          try {
            regeneratingWebhook.value = true;

            const { webhook: webhookUri } = await regenerateWebhook(sourceSubForm.value.id as string);
            webhook.value = webhookUri;
          } catch (err) {
            proxy.$throw(err, proxy);
          } finally {
            regeneratingWebhook.value = false;
          }
        }).catch(() => {
        });
      },
      copy: async (data: string) => {
        if (!data) {
          return;
        }

        try {
          await toClipboard(data);
          proxy.$success('复制成功');
        } catch (err) {
          proxy.$error('复制失败，请手动复制');
          console.error(err);
        }
      },
      showTransformerForm: (targetIndex: number) => {
        transformerFormDrawerVisible.value = true;
        transformerForm.value.targetIndex = targetIndex;
        transformerForm.value.transformers = [...targetsSubForm.value.targets[targetIndex].transformers];
      },
      confirmTransformerForm: async () => {
        targetsSubForm.value.targets[transformerForm.value.targetIndex].transformers = [...transformerForm.value.transformers];

        await save();
      },
    };
  },
});
</script>

<style scoped lang="less">
.event-bridge-editor {
  color: #082340;
  background-color: #EFF4F9;

  .right-top-btn {
    position: fixed;
    right: 20px;
    top: 78px;

    .jm-icon-button-cancel::before {
      font-weight: bold;
    }
  }

  a {
    color: inherit;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  .ellipsis {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .nav {
    padding: 15px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: #FFFFFF;
    height: 25px;

    .title {
      width: 30%;
    }

    .auto-saved-msg {
      font-size: 13px;
      color: #999;

      &.failed {
        color: #f00;
      }
    }
  }

  .content {
    padding: 8% 0;
    margin: 0 auto;
    display: flex;
    justify-content: center;
    align-items: center;

    .sources, .targets {
      .subject {
        color: #385775;
        padding-left: 10px;
        line-height: 40px;
      }

      .item + .item {
        margin-top: 30px;
      }

      .item {
        padding: 15px 0;
        width: 300px;
        background-color: #FFFFFF;
        border: 1px solid transparent;
        box-shadow: 0 0 16px 4px #DCE3EF;
        border-radius: 4px;

        &:hover {
          border-color: #096DD9;
        }

        > div + div {
          margin-top: 15px;
        }

        > div {
          padding: 0 15px;
          height: 25px;
          line-height: 25px;
        }

        .name {
        }

        .ref, .webhook-url {
          padding-top: 5px;
          padding-bottom: 5px;
          background: #F5F8FB;
          border-radius: 2px;
        }

        .webhook-url {
          font-size: 13.5px;
        }

        .ref .setting-forbidden:hover {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .project {
          display: flex;
          justify-content: space-between;
          align-items: center;

          a {
            width: 70%;
          }

          .non-associated {
            cursor: auto;
            color: #ccc;
          }
        }

        .operation {
          display: flex;
          justify-content: flex-end;
          align-items: center;

          button + button {
            margin-left: 15px;
          }

          button {
            width: 25px;
            height: 25px;
            background-color: transparent;
            border: 0;
            background-repeat: no-repeat;
            background-size: contain;
            cursor: pointer;

            &:active {
              background-color: #EFF7FF;
              border-radius: 4px;
            }

            &.edit {
              background-image: url('@/assets/svgs/btn/edit.svg');
            }

            &.copy {
              background-image: url('@/assets/svgs/event-bridge/copy.svg');
            }

            &.refresh {
              background-image: url('@/assets/svgs/event-bridge/refresh.svg');
            }

            &.setting {
              background-image: url('@/assets/svgs/event-bridge/setting.svg');
            }

            &.del {
              background-image: url('@/assets/svgs/btn/del.svg');
            }

            &.doing {
              opacity: 0.5;
              cursor: not-allowed;

              &:active {
                background-color: transparent;
              }
            }

            &.disabled {
              opacity: 0.5;
              cursor: not-allowed;
            }
          }
        }
      }
    }

    .targets .item {
      cursor: pointer;

      > div.project {
        padding-left: 0;

        > :first-child {
          padding-left: 15px;
        }
      }
    }

    .connection {
      margin-top: 40px;
      height: 40px;
      display: flex;
      align-items: center;

      .line {
        position: relative;
        width: 258px;
        margin-right: 2px;
        height: 0;
        border-bottom: 1px #B5BDC6 solid;
        border-top: 1px #B5BDC6 solid;

        &::before {
          position: absolute;
          content: '';
          top: -12px;
          left: 120px;
          width: 25px;
          height: 25px;
          background-image: url('@/assets/svgs/event-bridge/connection.svg');
          background-position: center center;
          background-repeat: no-repeat;
          background-size: contain;
        }

        &::after {
          position: absolute;
          top: -20px;
          right: -22px;
          content: '';
          width: 0;
          height: 0;
          margin: 10px;
          border-bottom: 10px transparent solid;
          border-right: 10px transparent solid;
          border-left: 10px #B5BDC6 solid;
          border-top: 10px transparent solid;
        }
      }
    }
  }
}
</style>